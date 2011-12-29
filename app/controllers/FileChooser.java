package controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import models.AppProps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.libs.IO;
import play.mvc.Controller;
import play.mvc.Http.StatusCode;
import play.mvc.Util;
import play.templates.JavaExtensions;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.exception.DropboxException;

/**
 * Controller to handle the action for the advanced file choose 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class FileChooser extends Controller {

	static final int MAX = 100;
	
	static final File publicRepo;
	
	static { 
		
		/* 
		 * initialize the 'publicRepo' path
		 */
		String path = AppProps.instance().getString("settings.path.public.data", Play.getFile("public/bb3").getAbsolutePath());
		publicRepo = new File(path);
		
		if( !publicRepo.exists() ) { 
			Logger.warn("The public data root does not exist: '%s'", publicRepo);
		}
		
	}
	
	/**
	 * The main file chooser page. 
	 * 
	 * Parameters: 
	 * - fieldId: it defines the input field name to which the filechooser dialog is binded.
	 *   It will be used by the callback function 'tb_select' defined in the 'main.html' page  
	 * 
	 * 
	 */
	public static void index() { 

		renderArgs.put("fieldId", params.get("fieldId"));
		renderArgs.put("publicDataRoot", publicRepo );
		renderArgs.put("recentDataRoot", Data.getUserTempPath() );
		
		render("FileChooser/filechooser.html");
	}
	
	

	/**
	 * List files in the 'Public' file repository 
	 * 
	 * @param dir the directory to list 
	 * @param query the to filter query to search into the repo
	 */
	public static void listPublicData(String dir, String query) { 
		Logger.debug("listPublicRepo method. Dir: '%s' - Query: '%s'", dir, query);
		
	    /* 
	     * normalize the specified dir 
	     */

	    if (dir == null) {
	    	dir = "/";
	    }
		
		List<FileEntry> _files = new ArrayList<FileChooser.FileEntry>();
		List<FileEntry> _folders = new ArrayList<FileChooser.FileEntry>();
		
		final File path = new File(publicRepo, dir);
	    if (path.exists()) {
	    	
	    	// define the filter 
	    	// search and sort the result
			;
    		List<FileEntry> result = new ArrayList<FileEntry>();
	    	if( "/".equals(dir) && !StringUtils.isEmpty(query) && query.length()>=3 )  {
	    		String sQuery = query.contains("*") ? query : "*" + query + "*";
	    		searchIntoPublicRepo(path, sQuery, result);
	    	}
	    	else { 
				FilenameFilter filter = new FilenameFilter() {
				    public boolean accept(File dir, String name) {
						return name.charAt(0) != '.';
				    }
				};
				File[] files = path.listFiles(filter);
				for( File ff : files ) { 
					result.add(wrap(ff,publicRepo,false));
				}
	    	}
	    	
	    	Collections.sort(result);

			// All dirs
			for (FileEntry item: result ) {
				if (item.isDir) {
					_folders.add(item);
			    }
				else { 
					_files.add(item);
				}
			}
	    }		
	    
	    renderArgs.put("files", _files);
	    renderArgs.put("folders", _folders);
	    render("FileChooser/treeitem.html");
	}	
	
	/**
	 * Show the list of files available the linked 'Dropbox' account 
     *
	 * @param dir the directory to list 
	 * @param query the to filter query to search into the repo
	 */
	public static void listDropboxData( String dir, String query ) { 
		Logger.debug("listDropboxData method.  Dir: '%s' - Query: '%s'", dir, query);

		/*
		 * check if connected otherwise shows Dropbox connection box
		 */
		if( !Dropbox.isLinked() ) { 
			error("Your Dropbox account is unlinked. Re-try reconnecting to Dropbox refreshing this page.");
		}
		
	    if (dir == null) {
	    	dir = "/";
	    }
	
		try {
			
			boolean usePathForName;
			List<Entry> result; 
			/* 
			 * Search into dropbox folder if a query has provided 
			 * */
			if( "/".equals(dir) && StringUtils.isNotEmpty(query) && query.length()>= 3 ) { 
				result = Dropbox.get().search("/", query, MAX, false);
				usePathForName = true;
			}
			/* 
			 * .. or navigate into the folder 
			 */
			else { 
				DropboxAPI.Entry entry = Dropbox.get().metadata(dir, MAX, null, true, null);
				result = entry.contents;
				usePathForName = false;
			}

			/* 
			 * split folders from files 
			 */
			List<FileEntry> _files = new ArrayList<FileChooser.FileEntry>();
			List<FileEntry> _folders = new ArrayList<FileChooser.FileEntry>();

			for( Entry item : result ) { 
				if( item.isDir ) { 
					_folders.add( wrap(item,usePathForName) );
				}
				else { 
					_files.add( wrap(item,usePathForName) );
				}
			}
			
			Collections.sort(_files);
			Collections.sort(_folders);
		    
		    renderArgs.put("files", _files);
		    renderArgs.put("folders", _folders);
		    render("FileChooser/treeitem.html");	
		} 
		catch (DropboxException e) {
			Logger.error(e,"Cannot connect Dropbox");
			Dropbox.unlink();
			error();
		}
	
		
	}
	
	/**
	 * Copy the a file from the Dropbox account to the user local storage 
	 * 
	 * @param filePath the file in the 'Dropbox' storage to be copied locally 
	 */
	public static void copyDropboxFile( String filePath ) { 
		Logger.debug("copyDropboxFile method.  FilePath: '%s'", filePath);
		
		if( !Dropbox.isLinked() ) { 
			// render error 
			error("Your Dropbox account is unlinked. Re-try reconnecting to Dropbox refreshing this page.");
		}
		
		request.format = "json";
		try {
			String fileName = FilenameUtils.getName(filePath);
			DropboxInputStream in = Dropbox.get().getFileStream(filePath, null);
			File target = Data.newUserFile(fileName);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target, false));
			IO.write(in, out);
			// ^ Stream closed by the write method

			String result = String.format("{\"success\":true, \"path\": \"%s\" }", JavaExtensions.escapeJavaScript(target.getAbsolutePath()));
			renderJSON(result);
		} 
		catch( IOException e ) { 
			Logger.error(e, "Cannot get the following file from dropbox (1): '%s'", filePath);
			Dropbox.unlink();
			error("Cannot get the request file from Dropbox (1)");
		}
		catch (DropboxException e) {
			Logger.error(e, "Cannot get the following file from dropbox (2): '%s'", filePath);
			Dropbox.unlink();
			error("Cannot get the request file from Dropbox (2)");
		}

	}
	
	/**
	 * Ajax action to retrieve the Dropbox link status 
	 */
	public static void isDropboxLinked() { 
		request.format = "json";
		try { 
			String result = String.format("{\"linked\":%s }", Dropbox.isLinked());
			renderJSON(result);
		}
		catch( Exception e ) { 
			Logger.error(e,"Error verifying Dropbox link status");
			Dropbox.unlink();
			error("Cannot verify link status to your Dropbox account");
		}
	}
	
	/**
	 * Download the specified URL document to the user local storage
	 * 
	 * @param url
	 * @throws InterruptedException 
	 */
	public static void copyUrlFile( String url ) throws InterruptedException { 
		Logger.debug("copyUrlFile method.  Url: '%s'", url);
		request.format = "json";
		
		String fileName = FilenameUtils.getName(url);
		File target = Data.newUserFile(fileName);
		
		try {
			FileUtils.copyURLToFile(new URL(url), target, 15000, 5000);
			String result = String.format(
					"{" +
					"\"success\":true, " +
					"\"path\": \"%s\", " +
					"\"size\": \"%s\"," +
					"\"name\": \"%s\" " +
					"}", 
					JavaExtensions.escapeJavaScript(target.getAbsolutePath()),
					JavaExtensions.formatSize( target.length() ),
					JavaExtensions.escapeJavaScript(target.getName())
					);
			renderJSON(result);
		} 
		catch( MalformedURLException e ) { 
			Logger.warn(e, "Not a valid URL: '%s'", url);
			error(StatusCode.BAD_REQUEST, "Malformed '\"URL");
		}
		catch (IOException e) {
			Logger.error(e, "Cannot download the specified URL: '%s'", url);
			error("Cannot download the specified URL");
		}
		
	}
	
	/**
	 * Show in the file chooser dialog the list of rencent used files for the current user 
	 * 
	 * @param dir (not used)
	 * @param query string to filter the result list 
	 */
	public static void listRecentData(String dir, String query) { 
		Logger.debug("listRecentData method.  Query: '%s'", dir, query);
		
		final File path = Data.getUserTempPath();
		final List<FileEntry> result = new ArrayList<FileEntry>();
		FilenameFilter filter;
		
		/*
		 * define the selection filter 
		 */
		if( !StringUtils.isEmpty(query) && query.length()>=3 )  {
    		final String sQuery = query.contains("*") ? query : "*" + query + "*";
    		filter = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return FilenameUtils.wildcardMatch(name, sQuery, IOCase.INSENSITIVE);
				}}; 
    	}
		/*
		 * select all except the '.' file
		 */
    	else { 
			filter = new FilenameFilter() {
			    public boolean accept(File dir, String name) {
					return name.charAt(0) != '.';
			    }
			};
    	}
    	
		File[] files = path.listFiles(filter);
		for( File ff : files ) { 
			result.add(wrap(ff,path,false));
		}
    	Collections.sort(result);		
		
		/* 
		 * render the result
		 */
	    renderArgs.put("files", result);
	    renderArgs.put("folders", new ArrayList<FileChooser.FileEntry>()); // does not contain folder by definition
		
	    render("FileChooser/treeitem.html");	
	}

	
	
	@Util
	static boolean searchIntoPublicRepo( File path, String query, List<FileEntry> result ) { 

		if( path == null ) { return true; }
		
		boolean continueTraverse = true;
		if( path.isDirectory() ) { 
			for( File file : path.listFiles() ) { 
				if( FilenameUtils.wildcardMatch(file.getName(), query, IOCase.INSENSITIVE) ) { 
					result.add( wrap(file,publicRepo,true)  );
					continueTraverse = (result.size() <= MAX);
				}

				if( continueTraverse && file.isDirectory() ) { 
					continueTraverse = searchIntoPublicRepo(file,query,result);
				}
				
				if( !continueTraverse ) { 
					return false;
				}
			}
		}
		
		return true;
	}
	
	/*
	 * Wrap a Java File object to out common rapresentation 
	 * 
	 */
	@Util
	protected static FileEntry wrap( File file, File root, boolean usePathForName ) { 
		if( file == null ) return null;
		
		FileEntry result = new FileEntry();
		result.path = FilenameUtils.normalize(file.getAbsolutePath());
		result.ext = FilenameUtils.getExtension(result.name);
		result.size = JavaExtensions.formatSize(file.length());
		result.length = file.length();
		result.modified = new Date(file.lastModified());
		result.isDir = file.isDirectory();
		
		// fix the path 
		if( root != null ) { 
			String sRoot = FilenameUtils.normalizeNoEndSeparator(root.getAbsolutePath());
			if( result.path.startsWith(sRoot) ) { 
				result.path = result.path.substring(sRoot.length());
				if( !result.path.startsWith("/") ) { 
					result.path = "/" + result.path;
				}
			}
		}

		// the 'name' to be visualized
		result.name = usePathForName ? result.path : file.getName();
		if( result.name.startsWith("/") ) { 
			result.name = result.name.substring(1);
		}
		
		// the below fix is requied by jQueryFileTree component
		if( result.isDir && !result.path.endsWith("/")) { 
			result.path += "/";
		}
		return result;
	}
	
	/*
	 * Wrap a dropbox file entry to our file representation  
	 */
	@Util
	protected static FileEntry wrap( Entry file, boolean usePathForName ) { 
		if( file == null ) return null;
		
		FileEntry result = new FileEntry();
		result.path = file.path;
		result.name = usePathForName ? file.path : FilenameUtils.getName(result.path);
		result.ext = FilenameUtils.getExtension(result.name);
		result.size = JavaExtensions.formatSize(file.bytes);
		result.length = file.bytes;
		result.isDir = file.isDir;
		result.modified = StringUtils.isNotEmpty(file.modified) ? RESTUtility.parseDate(file.modified) : null;

		if( result.name.startsWith("/") ) { 
			result.name = result.name.substring(1);
		}
		
		if( result.isDir && result.path != null && !result.path.endsWith("/")) { 
			result.path += "/";
		}

		return result;
		
	}
	
	/*
	 * Common wrapper to items to be rendered in the tree view 
	 */
	public static class FileEntry implements Serializable, Comparable<FileEntry> { 
		public String path;
		public String name;
		public String ext;
		public String size;
		public long length;
		public boolean isDir;
		public Date modified;
		
		@Override
		public int compareTo(FileEntry o) {
			return name.compareTo(o.name);
		}

		@Override
		public String toString() {
			return "FileEntry [path=" + path + ", name=" + name + ", ext="
					+ ext + ", size=" + size + ", length=" + length
					+ ", isDir=" + isDir + ", modified=" + modified + "]";
		}
	}
}

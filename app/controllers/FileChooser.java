package controllers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Util;
import play.templates.JavaExtensions;

import com.dropbox.client2.DropboxAPI;
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
	
	static final File publicRepo = new File("/Users/ptommaso/workspace/tserver/public/bb3");
	
	
	/**
	 * The main file chooser page 
	 */
	public static void index() { 
		
		boolean isDropboxLinked = Dropbox.isLinked();
		
		render("FileChooser/filechooser.html", isDropboxLinked);
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
	    		searchInto(path, sQuery, result);
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
	
	public static void listDropboxData( String dir, String query ) { 
		Logger.debug("listDropboxRepo method.  Dir: '%s' - Query: '%s'", dir, query);

		/*
		 * check if connected otherwise shows Dropbox connection box
		 */
		if( !Dropbox.isLinked() ) { 
			render("FileChooser/dropbox-connect.html");
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
			error();
			return;
		}
	
		
	}
	
	public static void listRecentData(String dir, String query) { 
	    render("FileChooser/treeitem.html");	
	}

	
	
	@Util
	static boolean searchInto( File path, String query, List<FileEntry> result ) { 

		if( path == null ) { return true; }
		
		boolean continueTraverse = true;
		if( path.isDirectory() ) { 
			for( File file : path.listFiles() ) { 
				if( FilenameUtils.wildcardMatch(file.getName(), query, IOCase.INSENSITIVE) ) { 
					result.add( wrap(file,publicRepo,true)  );
					continueTraverse = (result.size() <= MAX);
				}

				if( continueTraverse && file.isDirectory() ) { 
					continueTraverse = searchInto(file,query,result);
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

package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import models.AppProps;
import models.OutItem;
import models.OutResult;
import models.Repo;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.libs.IO;
import play.templates.JavaExtensions;
import util.Utils;
import exception.QuickException;


/**
 * Controller that hanles data download and upload 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Data extends CommonController {

	/**
	 * This method let to download any file placed in the application 
	 * data folder 
	 * 
	 * @param path
	 */
	public static void resource(String path) {
		renderStaticResponse();
		renderFile(AppProps.WORKSPACE_FOLDER, path);
	}
	
	/**
	 * Create a temporary zip file with all generated content and download it
	 * 
	 * @param rid the request identifier
	 * @throws IOException 
	 */
	public static void zip( String rid ) throws IOException {
		Repo repo = new Repo(rid);
		if( !repo.hasResult() ) {
			notFound(String.format("The requested download is not available (%s) ", rid));
			return;
		}
		
		OutResult result = repo.getResult();
		File zip = File.createTempFile("download", ".zip", repo.getFile());
		// get the list of files to download 
		List<File> files = new ArrayList<File>( result.getItems().size() );
		for( OutItem item : result.getItems() ) { 
			files.add(item.file);
		}
		// zip them and download
		zipThemAll(files, zip, null);

		String attachName = String.format("tcoffee-all-files-%s.zip",rid);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + attachName+ "\"");
		renderStaticResponse();
		renderBinary(zip);
	}
	
	/**
	 * Handy method to zip all datafolder content and download it
	 * 
	 * @param rid request identifier
	 */
	public static void zipDataFolder( String rid ) throws IOException { 
		File folder = new File(AppProps.instance().getDataPath(), rid);
		if( !folder.exists() ) { 
			notFound("Data path '%s' does not exist on the server", folder);
		}
		
		Collection allFiles = FileUtils.listFiles(folder, null, true);
		File zip = File.createTempFile("folder", ".zip");
		
		String parent = folder.getAbsolutePath();
		if( !parent.endsWith("/")) { 
			parent += "/";
		}
		zipThemAll(allFiles, zip, parent);
		
		String attachName = String.format("all-data-files-%s.zip",rid);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + attachName+ "\"");
		renderStaticResponse();
		renderBinary(zip);
		
	}

	/**
	 * Zip the collections of files as a unique zip file
	 * 
	 * @param items a collections of files to be zipped 
	 * @param targetZip the target zip file
	 * @param basePath if <code>null</code> all files are zipped in a plain archive (only file name is used) 
	 * otherwise this string value will be considered the prefix of the files absulte path to be removed 
	 */
	static void zipThemAll( Collection<File> items, File targetZip, String basePath ) {

		try {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(targetZip));
			
			for( File item : items ) { 
				if( item==null || !item.exists() ) { continue; }
				
				// add a new zip entry
				String entryName; 
				if( basePath == null ) { 
					/* use just the file name as entry name (w/o path information) */
					entryName = item.getName();
				}
				else { 
					/* make relative to the basePath */
					entryName = item.getAbsolutePath();
					if( entryName.startsWith(basePath)) { 
						entryName = entryName.substring(basePath.length());
					}
				}
				
				
				zip.putNextEntry( new ZipEntry(entryName) );
				
				// append the file content
				FileInputStream in = new FileInputStream(item);
				IO.copy(in, zip);
	 
				// Complete the entry 
				zip.closeEntry(); 
			}
			
			zip.close();					
		}
		catch (IOException e) {
			throw new QuickException(e, "Unable to zip content to file: '%s'", targetZip);
		}
	} 
		
	/**
	 * Manage upload of the input file
	 * 
	 * @param name the file name that is being uploaded
	 */
	public static void upload(String name) {
		/* default error result */
		String ERROR = "{success:false}";
		
		/* 
		 * here it is the uploaded file 
		 */
		File file = params.get(name, File.class);
		
		/* uh oh something goes wrong .. */
		if( file==null ) {
			Logger.error("Ajax upload is null for field: '%s'", name);
			renderText(ERROR);
			return;
		}
		
		/* error condition: wtf is that file ? */
		if( !file.exists() ) {
			Logger.error("Cannot find file for ajax upload field: '%s'", name);
			renderText(ERROR);
			return;
		}

		/* 
		 * copy the uploaded content to a temporary file 
		 * and return that name in the result to be stored in a hidden field
		 */
		try {
			File temp = File.createTempFile("upload-", null);
			// to create a temporary folder instead of a file delete and recreate it 
			temp.delete();
			temp.mkdir();
			temp = new File(temp, file.getName());
			
			FileUtils.copyFile(file, temp);
			String filename = Utils.getCanonicalPath(temp);
			renderText(String.format("{success:true, name:'%s', path:'%s', size:'%s'}", 
						file.getName(),
						JavaExtensions.escapeJavaScript(filename),
						FileUtils.byteCountToDisplaySize(temp.length())
						));
		}
		catch( IOException e ) {
			Logger.error(e, "Unable to copy temporary upload file: '%s'", file);
			renderText(ERROR);
		}
		
	}	
	

}

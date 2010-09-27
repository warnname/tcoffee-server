package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
		zipThemAll(result.getItems(), zip);
		renderBinary(zip, String.format("tcoffee-all-files-%s.zip",rid));
	}
	
	static void zipThemAll( List<OutItem> items, File file ) {

		try {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
			
			for( OutItem item : items ) { 
				if( item.file==null || !item.file.exists() ) { continue; }
				
				// add a new zip entry
				zip.putNextEntry( new ZipEntry(item.file.getName()) );
				
				// append the file content
				FileInputStream in = new FileInputStream(item.file);
				IO.write(in, zip);
	 
				// Complete the entry 
				zip.closeEntry(); 
				in.close(); 		
			}
			
			zip.close();					
		}
		catch (IOException e) {
			throw new QuickException(e, "Unable to zip content to file: '%s'", file);
		}
	} 
		
	/**
	 * Manage user input file uploads
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

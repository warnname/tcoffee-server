package controllers;

import java.io.File;
import java.io.IOException;

import models.AppProps;
import play.Logger;
import play.libs.IO;
import play.mvc.Controller;

public class Data extends Controller {

	public static void get(String path) {
		if( path == null ) { notFound(); return; }
		
		File file = new File(AppProps.instance().getDataPath(), path);
		
		if( !file.exists() || !file.isFile() ) {
			notFound(String.format("File '%s' does not exists", path));
		}
		
		if( !file.canRead() ) {
			notFound(String.format("File '%s' can't be read (check permission on file system)", path));
		}
		
		try {
			/*
			 * use the correct header for html file 
			 */
			String name = file.getName();
			if( name.endsWith(".html") || name.endsWith(".score_html") ) {
				response.contentType = "text/html";
			}

			//TODO improve this using a stream reader 
			renderText(IO.readContentAsString(file));
		} 
		catch (IOException e) {
			notFound(e.getMessage());
			Logger.error(e, "Error serving file: '%s'", file);
		}
	}
	
}

package controllers;

import models.Module;
import models.PageContent;
import play.Play;
import play.mvc.Controller;
import play.vfs.VirtualFile;
import util.XStreamHelper;
import exception.QuickException;

/**
 * Provide commons method to controllers 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class BaseController extends Controller {

	static boolean isGET() {
		return "GET".equals(request.method.toUpperCase());
	}
	
	static boolean isPOST() {
		return "POST".equals(request.method.toUpperCase());
	}
	
	
	static void unsupportedMethod() {
		throw new QuickException("Unsupported method: '%s'", request.method);
	}
	
	
	static Module loadModuleFile( String path  ) {
		for( VirtualFile vf : Play.templatesPath ) {
			if( vf == null ) continue;
			
			VirtualFile file = vf.child(path);
			if( file.exists() ) {
				return XStreamHelper.fromXML( file.getRealFile() );
			}
		}
		
		throw new QuickException("Specified template file definition does not exists: %s", path);
	}	
	
	
	static void renderGenericPage(final PageContent content) {
		render("Application/page.html", content);
		
	} 
}

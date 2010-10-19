package controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import models.Bundle;
import models.PageContent;
import models.Service;
import play.Logger;
import play.Play;
import play.exceptions.NoRouteFoundException;
import play.mvc.Controller;
import play.mvc.Router;
import util.Utils;
import bundle.BundleRegistry;
import exception.QuickException;

/**
 * Provide commons method to controllers 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class CommonController extends Controller {

	/**
	 * Inject the following variables in the template: 
	 * <li><code>_bundle</code>: the current selected {@link Bundle} instance if available</li>
	 * 
	 * <li><code>_main_class</code>: the CSS class to be used in the top body element in the layout structure</li>
	 * 
	 * <li><code>_main_action</code>: the CSS class to the used in layout template. this class and the previous one 
	 * are used to enable/disable some UI feature through CSS selector rules</li>
	 * 
	 * <li><code>_main_index</code>: the url of the main index page  </li>
	 * 
	 * <li><code>_main_title</code>: descriptive label sued as top page title </li>
	 * 
	 */
	static void injectImplicitVars() {
		Bundle bundle = null;
		String sBundle = params.get("bundle");
		if( Utils.isNotEmpty(sBundle) ) { 
			bundle = BundleRegistry.instance().get(sBundle);
			if( bundle == null ) { 
				Logger.info("Unkwown bundle '%s'", sBundle);
			}
		}
		
		
		/* 
		 * _class_body 
		 */
		String _main_class = null;
		if( bundle != null ) { 
			_main_class = bundle.name;
		}
		else if( request.controller != null ) { 
			_main_class = request.controller.toLowerCase();
		}
		
		/*
		 * _class_action
		 */
		String _main_action = null;
		if( bundle != null && request.path != null ) { 
			// get only the last part of the url containing the page name 
			int p = request.path.lastIndexOf("/");
			String action = p != -1 ? request.path.substring(p+1) : request.path;
			// remove the trailing .html suffix
			p = action.indexOf(".html");
			if( p != -1 ) { 
				action = action.substring(0,p);
			}
			_main_action = action;
		}
		else { 
			_main_action = request.actionMethod;
		}

		/*
		 * the main index page 
		 */
		String _main_index = null;
		String _main_title = null;
		if( bundle != null ) { 
			Map<String,Object> params = new HashMap<String, Object>(1);
			params.put("bundle", bundle.name);
			_main_index = Router.reverse("Application.index",params).toString();
			_main_title = bundle.title;
		}
		else { 
			_main_title = "T-Server";
			/* try to detect the index page */
			try { 
				_main_index = Router.reverse(request.controller+".index").toString();
			} catch( NoRouteFoundException e ) { 
				Logger.warn(e.getMessage());
				_main_index = request.getBase();
			}
		}
		
		/* add these variables on the template render arguments */
		renderArgs.put("_bundle", bundle);
		renderArgs.put("_main_class", _main_class);
		renderArgs.put("_main_action", _main_action);
		renderArgs.put("_main_index", _main_index);
		renderArgs.put("_main_title", _main_title);
		
	}	
	
	static boolean isGET() {
		return "GET".equals(request.method.toUpperCase());
	}
	
	static boolean isPOST() {
		return "POST".equals(request.method.toUpperCase());
	}
	
	
	static void unsupportedMethod() {
		throw new QuickException("Unsupported method: '%s'", request.method);
	}
	
	
	static protected void notFound( String message, Object... args ) { 
		notFound( String.format(message, args) );
	}
	
	static void renderGenericPage(final PageContent content) {
		render("Application/page.html", content);
	}
	
	
	static void responseNoCache() {
        response.setHeader("Cache-Control", "no-cache");
	}	

	/**
	 * Marks the headers for caching static returned response
	 */
	static void renderStaticResponse() { 
        if (Play.mode == Play.Mode.DEV) {
            response.setHeader("Cache-Control", "no-cache");
        } 
        else {
            String maxAge = Play.configuration.getProperty("http.cacheControl", "3600");
            if (maxAge.equals("0")) {
                response.setHeader("Cache-Control", "no-cache");
            } else {
                response.setHeader("Cache-Control", "max-age=" + maxAge);
            }
        }
		
	}
	
	
	static void renderFile( File root, String path ) { 
		if( root == null ) { 
			Logger.error("Cannot render file with null 'root' argument");
			notFound(); 
		} 
		if( path == null ) { 
			Logger.error("Cannot render file with null 'path' argument");
			notFound(); 
		}
		
		File file = new File(root, path);
		
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

			renderBinary(new BufferedInputStream(new FileInputStream(file)));
		} 
		catch (IOException e) {
			notFound(e.getMessage());
			Logger.error(e, "Error serving file: '%s'", file);
		}		
	}
	
	static Service service(String bundle, String service) { 
		
		Bundle _bundle = BundleRegistry.instance().get(bundle);
		if( _bundle == null ) { 
			notFound( "Unkown bundle: '%s'", bundle );
		}
		
		Service result = _bundle.getService(service);
		if( service == null ) { 
			notFound( "Unknown service: '%s' in bundle '%s'", service, bundle );
		}
		
		return result;
	}
	
}

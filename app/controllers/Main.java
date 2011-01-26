package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import models.Bundle;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.IO;
import play.mvc.Before;
import bundle.BundleRegistry;

/**
 * Application controller for generic infrastructure stuff, for 
 * example 'robots.txt', redirections and so-on
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Main extends CommonController {

	@Before
	static void before() { 
		injectImplicitVars();
	}
	
	
	/** 
	 * render as static page 'index.html' as main application page if exists
	 * in the public folder, otherwise redirects to the T-Coffee bundle index page 
	 * 
	 * see {@link Application#index()}
	 */
	public static void index() { 

		/* 
		 * try to load a static home page
		 */
		File indexPage = new File(Play.applicationPath, "public/index.html");
		if( indexPage.exists() ) { 
			response.contentType = "text/html";
			try {
				renderText( IO.readContentAsString(indexPage) );
			} catch (IOException e) {
				Logger.error(e, "Unable to read index file: '%s'", indexPage);
			}
		}
		
		/* try to load a cached version of T-Coffee home page */
		String home = (String) Cache.get("tcoffee_index_page");
		if( home != null && Cache.get("sysmsg")==null ) { 
			response.contentType = "text/html";
			renderText(home);
		}

		/* fallback on the dynamic index page */
		redirect("Application.index", "tcoffee");
	}
	
	public static void list() { 
		List<Bundle> bundles = BundleRegistry.instance().getBundles();
		render(bundles);
	}
	
	
	/**
	 * Quick access link to PSI-coffee
	 */
	public static void psicoffee() { 
		redirect("Application.main", "tcoffee", "psicoffee" );
	}
	
	/**
	 * Quick link to R-coffee
	 */
	public static void rcoffee() { 
		redirect("Application.main", "tcoffee", "rcoffee" );
	}
	
	/**
	 * Quick link to M-coffee
	 */
	public static void mcoffee() { 
		redirect("Application.main", "tcoffee", "mcoffee" );
	}
	
	/**
	 * Quick link Expresso
	 */
	public static void expresso() { 
		redirect("Application.main", "tcoffee", "expresso" );
	}
	
	/**
	 * Return the 'robots.txt' text file SEO optimization
	 */
	public static void robots() {
		final String conf = "/conf/robots.txt";
		try {
			renderText( IO.readContentAsString(Play.getFile(conf)) );
		} catch (IOException e) {
			Logger.error(e, "Unable to render 'robots.txt' file");
			notFound(String.format("Unable to find '%s'", conf));
		} 
	} 
	

    /*
     * fake page used for tests purpose only   
     */
    public static void sandbox() {
    	render();
    }
    
    
 	
	
}

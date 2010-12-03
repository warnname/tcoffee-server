package controllers;

import java.io.IOException;
import java.util.List;

import models.Bundle;
import play.Logger;
import play.Play;
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
	 * By default redirect to 'tcoffee' index page
	 * NOTE: see also 'Application.index'
	 * 
	 * see {@link Application#index()}
	 */
	public static void index() { 
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

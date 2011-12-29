package controllers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import models.AppProps;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

/**
 * Dropbox connection controller  
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Dropbox extends Controller {

	final static private String APP_KEY = "ojeqo0r2aq4tyrn";
	final static private String APP_SECRET = "rj2m7rdt6wkal6f";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

	/*
	 * Creates a Dropbox connection for the current session if does not exist
	 */
	final static private CacheLoader factory = new CacheLoader<String, DropboxAPI<WebAuthSession>>() {
        
		public DropboxAPI<WebAuthSession> load(String key) {
			AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
			WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
			return new DropboxAPI<WebAuthSession>(session);		
          }
        };
        
    /*
     * cache for the dropbox connections
     */
	final static Cache<String, DropboxAPI<WebAuthSession>> cache = CacheBuilder.newBuilder()
		    .concurrencyLevel(4)
		    .expireAfterWrite(20, TimeUnit.MINUTES)
		    .build(factory);
	
	
	/** 
	 * @return A Dropbox connection for the current user
	 */
	@Util
	public static DropboxAPI<WebAuthSession> get() { 
		try {
			return cache.get( session.getId() );
		} 
		catch (ExecutionException e) {
			Logger.error(e, "Cannot establish Droxbox session");
			return null;
		}
	}

	
	/*
	 * Utility method to check if the current session is linked to Dropbox 
	 * @return
	 */
	@Util
	static boolean isLinked() { 
		DropboxAPI<WebAuthSession> dbox = get();
		return dbox != null ? dbox.getSession().isLinked() : false;
	}
	
	/*
	 * Unlink the current Dropbox connection. 
     * 
     * It may happen that the current session linki is brokwn but the 'link' status is true. 
     * Use this method in case of error to reset the current link status
	 */
	@Util 
	static void unlink() { 
		try { 
			get().getSession().unlink();
		} catch( Throwable e ) { 
			// do not report 
		}
	}

	/**
	 * Manage the Dropbox connection process
	 */
	public static void connect() 	
	{ 
		String host = AppProps.instance().getHostName();
		String path = Router.reverse("Dropbox.confirm").toString();
		String abspath = "http://" + host + path;
		   
		try { 
			WebAuthInfo auth = get().getSession().getAuthInfo(abspath);
			Logger.debug("Token pair: %s", auth.requestTokenPair);
			Logger.debug("Auth URL  : %s", auth.url);
			session.put("dropboxTokenKey", auth.requestTokenPair.key);
			session.put("dropboxTokenSecret", auth.requestTokenPair.secret);
			session.remove("dropboxConfirmed"); // <-- remove the 'already confirmed' flag if exist
			redirect(auth.url); 
		}
		catch( Exception e ){ 
			error(e);
		}
			
	}
	    
	
	/**
	 * Display the Dropbox link confirmation page
	 * 
	 * This page will be invoked by the Dropbox confirmation process, see the above connect action
	 */
	public static void confirm() { 

		boolean notYetConfirmed = session.get("dropboxConfirmed") == null;
		
		
		if( notYetConfirmed ) { 
	    	String key = session.get("dropboxTokenKey");
	    	String secret = session.get("dropboxTokenSecret");
			session.put("dropboxConfirmed","1");
	    	
	    	try {
				String result = get().getSession().retrieveWebAccessToken( new RequestTokenPair(key, secret) );
				Logger.debug("Dropbox auth result: %s", result);
			} 
	    	catch (Exception e) {
	    		error(e);
			}
		}

		render("FileChooser/dropbox-confirm.html");
	}	

	
}

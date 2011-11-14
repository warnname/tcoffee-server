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
 * Dropbox Connector 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Dropbox extends Controller {

	final static private String APP_KEY = "ojeqo0r2aq4tyrn";
	final static private String APP_SECRET = "rj2m7rdt6wkal6f";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

	/*
	 * Dropbox connection factory 
	 */
	final static private CacheLoader factory = new CacheLoader<String, DropboxAPI<WebAuthSession>>() {
        
		public DropboxAPI<WebAuthSession> load(String key) {
			AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
			WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
			return new DropboxAPI<WebAuthSession>(session);		
          }
        };
        
    /*
     * cache for the dropbox connection
     */
	final static Cache<String, DropboxAPI<WebAuthSession>> cache = CacheBuilder.newBuilder()
		    .concurrencyLevel(4)
		    .expireAfterWrite(10, TimeUnit.MINUTES)
		    .build(factory);
	
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
	
	@Util
	public static boolean isLinked() { 
		DropboxAPI<WebAuthSession> dbox = get();
		return dbox != null ? dbox.getSession().isLinked() : false;
	}
	
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
			redirect(auth.url);
		}
		catch( Exception e ){ 
			error(e);
		}
			
	}
	    
	public static void confirm() { 
	    	
    	String key = session.get("dropboxTokenKey");
    	String secret = session.get("dropboxTokenSecret");;
    	
    	try {
			String result = get().getSession().retrieveWebAccessToken( new RequestTokenPair(key, secret) );
			Logger.debug("Dropbox auth result: %s", result);
		} 
    	catch (Exception e) {
    		error(e);
		}
    	render("FileChooser/dropbox-confirm.html");
	}	

	
}

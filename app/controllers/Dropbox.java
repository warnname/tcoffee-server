package controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import models.AppProps;
import models.Bundle;
import models.OutItem;
import models.OutResult;
import models.Repo;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import play.templates.JavaExtensions;

import bundle.BundleRegistry;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
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
	
	static final Map<String,AccessType> ACCESS_MAP = new HashMap<String,AccessType>(2);
	
	static { 
		ACCESS_MAP.put("folder", AccessType.APP_FOLDER );
		ACCESS_MAP.put("full", AccessType.DROPBOX ); 
		ACCESS_MAP.put("dropbox", AccessType.DROPBOX ); // just a synonym of the above
	}

		
	static class DropboxHandle implements Serializable {
		AccessType accessType; 
		DropboxAPI<WebAuthSession> instance; 
		String resultFolder; 
		
		void unlink() {  
			if( instance == null ) return;
			instance.getSession().unlink();
		} 
		
		boolean isLinked() {
			if( instance==null ) return false;
			return instance.getSession().isLinked();
		} 
	} 
	
	/*
	 * Creates a Dropbox connection for the current session if does not exist
	 */
	final static private CacheLoader factory = new CacheLoader<String, DropboxHandle>() {
        
		public DropboxHandle load(String key) {
			DropboxHandle result = new DropboxHandle();

			/*
			 * configure using configuration properties 
			 * - settings.dropbox.key
			 * - settings.dropbox.secret
			 * - settings.dropbox.accesstype
			 */
			String appKey = AppProps.instance().getString("settings.dropbox.key");
			String appSecret = AppProps.instance().getString("settings.dropbox.secret");
			String sAccessType = AppProps.instance().getString("settings.dropbox.accesstype");
			result.accessType = ACCESS_MAP.get(sAccessType);
			if( result.accessType == null ) {
				Logger.warn("Missing or unknown Dropbox access type: '%s'. Valid values are: %s. Fallback to default ('%s')", 
						sAccessType, 
						ACCESS_MAP.keySet().toString(),
						"full");
				result.accessType = AccessType.APP_FOLDER;
			}
			
			// create the dropbox connector instance 
			WebAuthSession session = new WebAuthSession(new AppKeyPair(appKey, appSecret), result.accessType);
			result.instance = new DropboxAPI<WebAuthSession>(session);		
			result.resultFolder = AppProps.instance().getString("settings.dropbox.folder", "/results");
			
			return result;
          }
        };
        
    /*
     * cache for the dropbox connections
     */
	final static Cache<String, DropboxHandle> cache = CacheBuilder.newBuilder()
		    .concurrencyLevel(4)
		    .expireAfterWrite(20, TimeUnit.MINUTES)
		    .build(factory);
	
	
	/** 
	 * @return A Dropbox connection for the current user
	 */
	@Util
	public static DropboxAPI<WebAuthSession> get() { 
		try {
			return cache.get(session.getId()).instance;
		} 
		catch (ExecutionException e) {
			Logger.error(e, "Cannot establish Droxbox session");
			return null;
		}
	}
	
	@Util static DropboxHandle handle() {
		try {
			return cache.get(session.getId());
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
	public static void connect() {
		Logger.info("Dropbox#connect - session: %s", session.getId());
		
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
			Logger.error(e, "Error on Dropbox connect - ip: %s; session: %s", request.remoteAddress, session.getId());
			error(e);
		}
			
	}
	    
	
	/**
	 * Display the Dropbox link confirmation page
	 * 
	 * This page will be invoked by the Dropbox confirmation process, see the above connect action
	 */
	public static void confirm() { 
		Logger.info("Dropbox#confirm - session: %s", session.getId());

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
				Logger.error(e, "Error on Dropbox confirm - ip: %s; session: %s", request.remoteAddress, session.getId());
	    		error(e);
			}
		}

		render("FileChooser/dropbox-confirm.html");
	}	

	/**
	 * Copy the specified result dataset to the user Dropbox account
	 * 
	 * @param rid the request identifier of the result data to copy
	 * 
	 * See 'copy-to-dropbox.html'
	 */
	public static void copy( String rid ) { 
		Logger.info("Dropbox#copy - rid: %s", rid);
		
		request.format = "json";
		
		DropboxHandle handler = handle();
	
		if( !handler.isLinked() ) { 
			String result = "{\"success\":false, \"reason\": \"unlinked\" }";
			renderJSON(result);
		}
		
		
		Repo repo = new Repo(rid);
		if( !repo.hasResult() ) {
			notFound(String.format("The requested result is not available (%s) ", rid));
			return;
		}

		// define the target path 
		String sBundle = repo.hasResult() ? repo.getResult().bundle : null;
		Bundle bundle = sBundle != null ? BundleRegistry.instance().get(sBundle) : null;
		String sTitle = bundle != null ? bundle.getTitle() : "T-Coffee";
		if( "T-Coffee Server".equals(sTitle) ) sTitle = "T-Coffee";
		
		String path;
		String base = "Apps/" + sTitle + "/results";
		if( !base.startsWith("/")) base = "/" + base;
		if( !base.endsWith("/")) base = base + "/";
		

		int tries = 0;
		boolean exists=false;
		do { 
			path = tries == 0 ? base + rid : String.format("%s%s (%s)", base, rid, tries);
			try { 
				exists = checkPathExist(path);
				tries++;
			} 
			catch( DropboxException e ) { 
				Logger.error(e,"Error accessing Dropbox folder '%s'", path);
				unlink();
				error("Error accessing your Dropbox account");
			}

		} 
		while(exists);
		
 		
		OutResult result = repo.getResult();
		for( OutItem item : result.getItems() ) { 
			FileInputStream buffer=null;
			if( item.exists() ) try { 
				buffer = new FileInputStream(item.file);
				String sFilePath = path + "/" + item.name;
				handler.instance.putFileOverwrite(sFilePath, buffer, item.file.length(), null);
			}
			catch( Exception e ) { 
				Logger.error(e, "Error copying the following file to Dropbox: '%s'", item.file);
				String json = "{ \"success\":false, " +
								"\"reason\": \"error\"," +
								"\"message\": \"Error copying the request result to your Dropbox account\" }";
				renderJSON(json);
			}
			finally {
				if(buffer!=null) try { buffer.close(); } catch(IOException e) {} 
			}

		}
	
		// return the json result
		String json = String.format("{\"success\":true, \"path\": \"%s\" }", JavaExtensions.escapeJavaScript(path));
		renderJSON(json);
	}
	
	
	@Util
	static boolean checkPathExist(final String path) throws DropboxException { 
		Logger.debug("Dropbox#checkPathExist - '%s'", path);
		
		try { 
			DropboxAPI.Entry entry = get().metadata(path, 1, null, false, null);
			// if the file has been deleted --> does NOT exists 
			return !entry.isDeleted;
		} 
		catch( DropboxServerException e ) { 
			// trying to access to a path that does not exists 
			// will raise a 404 error, in this case return false
			if( e.error != 404 ) { 
				throw e;
			}
			return false;
		}

	}
	
	/**
	 * Unlink and invalidate all Dropbox sessions
	 */
	public static void invalidateAll() { 
		Iterator<DropboxHandle> it = cache.asMap().values().iterator();
		while( it.hasNext() ) {
			try  { 
				it.next().unlink();
			}
			catch( Exception e ) {
				Logger.warn("Error on unlinking Dropbox session");
			} 
		}
		cache.invalidateAll();
		renderText("OK");
	}
	
}

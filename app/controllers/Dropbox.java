package controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import models.AppProps;
import models.OutItem;
import models.OutResult;
import models.Repo;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;

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

	/*
	 * Creates a Dropbox connection for the current session if does not exist
	 */
	final static private CacheLoader factory = new CacheLoader<String, DropboxAPI<WebAuthSession>>() {
        
		public DropboxAPI<WebAuthSession> load(String key) {
			String APP_KEY = Play.configuration.getProperty("settings.dropbox.appkey");
			String APP_SECRET = Play.configuration.getProperty("settings.dropbox.appsecret");
			
			AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
			WebAuthSession session = new WebAuthSession(appKeys, AccessType.APP_FOLDER);
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

	/**
	 * Copy the specified result dataset to the user Dropbox account
	 * 
	 * @param rid the request identifier of the result data to copy
	 */
	public static void copy( String rid ) { 
		Logger.debug("Invoking #copyToDropbox(rid:%s) ", rid);
		
		request.format = "json";
		
		DropboxAPI<WebAuthSession> dbox = get();
	
		if( !dbox.getSession().isLinked() ) { 
			String result = "{\"success\":false, \"reason\": \"unlinked\" }";
			renderJSON(result);
		}
		
		
		Repo repo = new Repo(rid);
		if( !repo.hasResult() ) {
			notFound(String.format("The requested result is not available (%s) ", rid));
			return;
		}

		
		String sPath=null;
		int tries = 0;
		boolean exists=false;
		do { 
			sPath = tries == 0 ? "/results/" + rid : String.format("/results/%s (%s)", rid, tries);
			try { 
				exists = checkPathExist(sPath);
				tries++;
			} 
			catch( DropboxException e ) { 
				Logger.error(e,"Error accessing Dropbx folder '%s'", sPath);
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
				String sFilePath = sPath + "/" + item.name;
				dbox.putFileOverwrite(sFilePath, buffer, item.file.length(), null);
			}
			catch( Exception e ) { 
				unlink();
				
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
	
		renderJSON("{\"success\":true }");
	}
	
	
	@Util
	static boolean checkPathExist(final String path) throws DropboxException { 

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
		Iterator<DropboxAPI<WebAuthSession>> it = cache.asMap().values().iterator();
		while( it.hasNext() ) {
			try  { 
				it.next().getSession().unlink();
			}
			catch( Exception e ) {
				Logger.warn("Error on unlinking Dropbox session");
			} 
		}
		cache.invalidateAll();
		renderText("OK");
	}
	
}

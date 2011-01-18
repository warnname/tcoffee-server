package plugins;

import java.util.List;

import models.AppProps;
import play.Logger;
import play.PlayPlugin;
import play.cache.Cache;
import play.libs.Time;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import util.Utils;
import util.Utils.MatchAction;

/**
 * This plugin intercepts invocation on T-Coffee bundle index page and will store a copy in 
 * the application cache, that copy will be presented as application main root page ('/') in 
 * order to optimized SEO ranking. 
 * 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class HomeCachePlugin  extends PlayPlugin {

	static String sDuration;
	
	static long time;
	
	public void beforeActionInvocation() { 
		
		/* 
		 * Check if homepage cache is active and how long is its duration
		 */
		sDuration = AppProps.instance().getProperty("homepage.cache.duration");
		if( Utils.isEmpty(sDuration) || "0".equals(sDuration.trim())) { 
			Cache.delete("tcoffee_index_page");
			return;
		}
		
	}
	
	public void afterActionInvocation() { 
		try { 
			safeCache();
		}
		catch( Exception e ) { 
			Logger.warn(e, "Erron on home page caching");
		}
	
	}
	
	static void safeCache() { 
		/*
		 * this feature is active only on T-Coffee home page
		 */
		if( !"/apps/tcoffee/index.html".equals(Request.current().path) ) 
		{ 
			return;
		}
		
		
		/* check how much time is passed */
		if( System.currentTimeMillis()-time < Time.parseDuration(sDuration)*1000 ) { 
			return;
		}
		
		String home = Response.current().out.toString();
		Cache.set("tcoffee_index_page", fixPaths(home));		
		time = System.currentTimeMillis();
	}
	
	static boolean isAbsolute( String path ) { 
		return path != null && ( path.startsWith("/") || path.startsWith("http:") || path.startsWith("#"));
	}
	
	/**
	 * Fix all relative paths adding the prefix "/apps/tcoffee/"
	 * 
	 * @param body the page html string 
	 * @return html with fixed relative paths 
	 */
	static String fixPaths( String body ) { 
		
		/*
		 * fix 'src' attributes
		 */
		body = Utils.match(body, " src=(['\"]?)([^'\" ]+)['\"]?", new MatchAction() {
			
			public String replace(List<String> groups) {
				String path = groups.get(2);
				return isAbsolute(path) ? groups.get(0) : " src=$1/apps/tcoffee/$2$1";
			}
		});
		

		/*
		 * fix 'href' attributes
		 */
		body = Utils.match(body, " href=(['\"]?)([^'\" ]+)['\"]?", new MatchAction() {
			
			public String replace(List<String> groups) {
				String path = groups.get(2);
				return isAbsolute(path) ? groups.get(0) : " href=$1/apps/tcoffee/$2$1";
			}
		});
		
		/*
		 * fix urls 
		 */
		body = Utils.match(body, "url\\((['\"]?)([^'\" ]+)['\"]?\\)", new MatchAction() {
			
    		public String replace(List<String> groups) {
    			String path = groups.get(2);
				return isAbsolute(path) ? groups.get(0) : " url($1/apps/tcoffee/$2$1)";
    		}
		});
		
		
		return body;
	}
		
}

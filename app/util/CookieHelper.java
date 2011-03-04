package util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.Logger;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;

/**
 * Helps on accessing Http cookies 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class CookieHelper {

	
	/**
	 * Get a cookie from the current request
	 * @param key the cookie unique identifier
	 * @return the cookie value or <code>null</code> if that cookie does not exist
	 */
	public static String get( String key ) { 
		return get(key,null);
	}

	/**
	 * Get a cookie from the current request
	 * @param key the cookie unique identifier
	 * @param defValue the value to returned if the cookie does not exist
	 * @return the cookie value or <code>defValue</code> if that cookie does not exist
	 */
	public static String get( String key, String defValue ) { 
		
		Cookie cookie = Request.current().cookies.get(key);
		String value = cookie != null ? cookie.value : null;
		if( value == null ) { 
			return defValue;
		}
		
		try {
			return URLDecoder.decode(value, "UTF-8");
		} 
		catch (UnsupportedEncodingException e) {
			Logger.warn(e, "Unable to decode cookie: (%s=%s) ", key, value);
			return defValue;
		}

	}
	
}

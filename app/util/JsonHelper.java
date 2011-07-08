package util;

import play.templates.JavaExtensions;

/**
 * Some JSON helpers 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class JsonHelper {

	public static String error( String msg ) { 
		return String.format("{\"error\": \"%s\"}", JavaExtensions.escapeJavaScript(msg));
	}
	
	public static String error( Throwable e ) { 
		return error(Utils.cause(e));
	}
	
}

package events;

import models.AppConf;
import models.AppProps;

/**
 * Centralize main application events handler 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class AppEvents {

	public static void appPropsChanged(AppProps props) {
		
//		/*
//		 * upadte the Play routes for adta path 
//		 */
//		String path = props.getPathData();
//		if( Utils.isNotEmpty(path)) {
//			Router.addRoute("GET", "/data/", "staticDir:" + path );
//		}
//		
	}
	
	/**
	 * Append 
	 * @param conf
	 */
	public static void appConfChanged(AppConf conf) {
		
	} 
	
}

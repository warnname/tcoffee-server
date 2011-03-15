package util;

import java.io.Serializable;
import java.io.StringReader;

import play.Logger;
import play.cache.Cache;
import play.libs.WS;
import play.libs.WS.HttpResponse;

public class LocationHelper {

	static public class Location implements Serializable { 
		
		String country;
		String city;
		
		public Location( String raw ) { 
			for( String line : new ReaderIterator(new StringReader(raw))) { 
				if( line.startsWith("Country:")) {
					country = line.substring("Country:".length()).trim();
				}
				else if( line.startsWith("City:") ) { 
					city = line.substring("City:".length()).trim();
					break;
				}
			}
		}
	}
	
	public static Location findByIp( String ip ) { 
		final String key = "ip2loc_" + ip;
		Location result = (Location) Cache.get(key);
		if( result != null ) { 
			Logger.info("Location data for '%s' found on cache", ip);
			return result;
		}
		
		String url = "http://api.hostip.info/get_html.php?ip=" + ip;
		Logger.debug("Location service query url: '%s'", url);
		HttpResponse response = WS.url(url).get();
		result = new Location( response.getString() );
		Cache.set(key, result, "7d");	// <-- cache the result for one week 
		
		return result;
	}
}

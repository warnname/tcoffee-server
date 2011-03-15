package util;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import models.Bundle;
import play.templates.JavaExtensions;

/**
 * Add some syntax sugar in templates 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class ServerExtension extends JavaExtensions {

	/**
	 * When the map entry contains a multiple paths, each path will be separated by a new line character. 
	 * <p>
	 * It uses a bit of 'heuristic' on the key to avoid to splti non-path values.
	 *   
	 * @param entry a {@link Map} entry
	 * @return the {@link Map} entry value replacing path separater with new line chars
	 */
	public static String path( Entry<Object, Object> entry ) { 
		String key =  entry.getKey() != null ? entry.getKey().toString() : null;
		String value = entry.getValue() != null ? entry.getValue().toString() : null;
		
		if( key != null && key.toLowerCase().contains("path") 
				&& value != null && value.contains(File.separator) && value.contains(File.pathSeparator)) 
		{ 
			String[] items = value.split(File.pathSeparator);
			StringBuilder result = new StringBuilder();
			if(items != null ) for( String str : items ) { 
				if( result.length() > 0 ) { result.append("\n"); }
				result.append(str);
			}
			
			return result.toString();
		}

		return value;

	}
	
	public static String relativeTo( File file, Bundle bundle ) { 
		if( file == null ) return null;
		if( bundle == null ) file.toString();
		
		String root = bundle.root.getAbsolutePath();
		String path = file.getAbsolutePath();
		
		return path.startsWith(root) 
			? path.substring(root.length())
			: path;
		
	}
 }

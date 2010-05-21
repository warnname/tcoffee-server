package util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

import models.AppConf;
import play.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

import exception.QuickException;

/**
 * Utily class to handle common XStream operation 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class XStreamHelper {
	
	private static XStream __xstream;

	/**
	 * @return Xstream parser instance pre-configured singleton instance
	 */
	public static XStream xstream() {
		
		if( __xstream != null ) {
			return __xstream; 
		} 
		
		/* create an xstream instance */
		XStream result = new XStream(new DomDriver());

		/* register converters */
		Logger.trace("Xstream registering converters on package: 'convertes'");
		Class[] classes = Utils.getClasses("converters");
		for( Class clazz : classes ) {
			if( Modifier.isAbstract(clazz.getModifiers()) ) { continue; }
			if( Modifier.isInterface(clazz.getModifiers()) ) { continue; }
			
			try {
				if( Converter.class.isAssignableFrom(clazz) ) { 
					Logger.debug("Registering XStream converter: %s", clazz); 
					result.registerConverter((Converter)clazz.newInstance());
				}
				else if( SingleValueConverter.class.isAssignableFrom(clazz) ) {
					Logger.debug("Registering XStream converter: %s", clazz); 
					result.registerConverter((SingleValueConverter)clazz.newInstance());
				}
				else {
					Logger.warn("Unknown converter class: %s", clazz);
				}
				
			} catch (Exception e) {
				throw new QuickException(e, "Unable to instantiate converter: %s", clazz); 
			}
		}
		
		/* process models for annotation */
		String className = AppConf.class.getName();
		int p = className.lastIndexOf(".");
		String packageName = p != -1 ? className.substring(0,p) : ""; 
		Logger.trace("Xstream processing package: '%s'", packageName);
		classes = Utils.getClasses(packageName);
		for( Class clazz : classes ) {
			Logger.trace("Processing XStream class: %s", clazz);
			result.processAnnotations(clazz);
		}
		
		
		return __xstream = result;
	}
	
	public static <T> T fromXML(File file) {

		try {
			return (T) xstream().fromXML( new FileInputStream(file) );
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T fromXML(String xml) {
		return (T) xstream().fromXML(xml);
	}
	
	public static String toXML(Object obj) {
		return xstream().toXML(obj);
	}
	
	public static void toXML(Object obj, File file) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			xstream().toXML(obj,writer);
		} 
		catch (IOException e) {
			throw new QuickException(e);
		}
		
		try {
			if( writer != null ) writer.close();
		} 
		catch (IOException e) {
			Logger.warn("Error on closing file: '%s'", file);
		}
	}
	
}

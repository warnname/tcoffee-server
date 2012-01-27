package models;

import exception.QuickException;
import groovy.lang.GroovyObject;

import org.apache.commons.lang.StringUtils;

import plugins.AutoBean;
import bundle.BundleScriptLoader;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import converters.ScriptConverter;


/**
 * Models a script based validation rule 
 * 
 * @author Paolo Di Tommaso
 *
 */
@AutoBean
@XStreamAlias("script")
@XStreamConverter(ScriptConverter.class)

public class Script {

	public String text;
	
	public String file;

	private transient BundleScriptLoader loader;
	
	private transient Object result;

	private transient volatile GroovyObject script;
	
	public Script() {
		
	} 
	
	/**
	 * Copy constructor 
	 * 
	 * @param that the instance from which copy 
	 */
	public Script(Script that) {
		this.text = that.text;
		this.file = that.file;
	}
	
	/**
	 * Create an empty script injecting the specified loader. Only for test porpose 
	 * 
	 * @param loader
	 */
	protected Script(BundleScriptLoader loader ) {
		this.loader = loader;
	}
	
	public Script setText(String text) {
		this.text = text;
		return this;
	} 
	
	public Script setFile( String file ) {
		this.file = file;
		return this;
	}
	
	public GroovyObject getScriptObject() {
		
		GroovyObject result = script;
		
		if( result==null ) synchronized (this) {
			result = script;
			if( result == null ) {
				result = script = _getScriptObj();
			}
		}

		
		return result;
	} 

	private GroovyObject _getScriptObj() {
		GroovyObject result;

		if( loader == null ) {
			loader = Service.current().bundle.getScriptLoader();
		}
		
		/*
		 * when the element defines a script text, parse it a 
		 */
		if( StringUtils.isNotBlank(text) ) {
			result = (GroovyObject) loader.getExtensionByScript(text);
		}
		
		/*
		 * otherwise if the 'file' attribute is provided, it will parsed as a groovy class either:
		 * - 1) that extends a 'AbstractCommand' class, that will used as a 'delete' for this class
		 * - 2) or a generic groovy code, that will be parsed as a script 
		 */
		else if( StringUtils.isNotBlank(file) ) {
			result = (GroovyObject) loader.getExtensionByFile(file);
		}
		
		/* unknwon error */
		else {
			throw new QuickException("Missing definition for script. Provide either the 'file' attribute or the 'clazz' attribute of the script code itself in the element body");
		} 	
		
		return result;
	} 
	
	public Script run() {
		result = getScriptObject().invokeMethod("run", null);
		return this;
	} 
	
	public Object getProperty( String key ) {
		return getScriptObject().getProperty(key);
	}
	
	public Script setProperty( String key, Object value ) {
		getScriptObject().setProperty(key, value);
		return this;
	}
	
	public Object getResult() {
		return result;
	}
	
}

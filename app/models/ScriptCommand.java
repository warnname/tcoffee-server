package models;

import exception.CommandException;
import exception.QuickException;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import converters.ScriptCommandConverter;


/**
 * Execute Groovy scripts in the bundle context
 * 
 * @author Paolo Di Tommaso
 *
 */

@XStreamAlias("script")
@XStreamConverter(ScriptCommandConverter.class)
public class ScriptCommand extends AbstractCommand {

	
	public String fScriptText; 
	
	public String fClass;

	public String fFile;
	
	GroovyClassLoader gcl;
	
	AbstractCommand delegate;
	
	GroovyObject script;
	
	/**
	 * The default constructor
	 */
	public ScriptCommand() { }
	
	/**
	 * Copy constructor
	 * 
	 * @param that an instance from create a copy 
	 */
	public ScriptCommand( ScriptCommand that ) {
		this.fScriptText = that.fScriptText; 
		this.fClass= that.fClass ;
		this.fFile = that.fFile;		
	}
	
	/**
	 * The command initialize phase 
	 */
	@Override
	public void init(ContextHolder ctx) {
		super.init(ctx);

		gcl = new GroovyClassLoader(Play.classloader); 
		
		/*
		 * 	add the bundle script path to the classlaoder 
		 */
		String path = ctx.get("bundle.script.path");
		if( StringUtils.isNotBlank(path) ) {
			gcl.addClasspath(path);
		}
		
		/*
		 * when the element defines a script text, parse it a 
		 */
		if( StringUtils.isNotBlank(fScriptText) ) {
			try {
				script = (GroovyObject) gcl.parseClass(fScriptText).newInstance();
			} 
			catch (Exception e) {
				throw new QuickException(e, "Cannot parse provided script");
			}
		}
		/*
		 * otherwise if the 'file' attribute is provided, it will parsed as a groovy class either:
		 * - 1) that extends a 'AbstractCommand' class, that will used as a 'delete' for this class
		 * - 2) or a generic groovy code, that will be parsed as a script 
		 */
		else if( StringUtils.isNotBlank(fFile) ) {
			try {
				Class clazz = gcl.parseClass(new File(path,fFile));
				if( AbstractCommand.class.isAssignableFrom(clazz)) {
					delegate = (AbstractCommand) clazz.newInstance();
				}
				else {
					script = (GroovyObject) clazz.newInstance();
				}
			} catch (Exception e) {
				throw new QuickException(e, "Cannot parse script file '%s'", fFile);
			}
		}
		/*
		 * of when the 'clazz' attribute is provided it must extend the 'AbstractCommand' class 
		 * and a instance is created
		 */
		else if( StringUtils.isNotBlank(fClass) ) {
			try {
				delegate = (AbstractCommand) gcl.loadClass(fClass).newInstance();
			} catch (Exception e) {
				throw new QuickException(e, "Cannot create script class '%s'. Make sure that it extends '%s' class.", fClass, AbstractCommand.class.getSimpleName());
			}
		}
		else {
			throw new QuickException("Missing definition for <script /> command. Provide the 'file' attribute or the 'clazz' attribute of the script code itself in the element body");
		} 

		/*
		 * initialize the delegate if it has been created
		 */
		if( delegate != null ) {
			delegate.init(ctx);
		}
	}

	/**
	 * Run the script body or the delegate 'run' method
	 */
	@Override
	protected boolean run() throws CommandException {
		if( delegate != null ) {
			return delegate.run();
		}

		if( script != null ) {
			script.setProperty("input", ctx.input); 
			script.setProperty("result", ctx.result); 
			script.setProperty("context", ctx.map); 
			script.invokeMethod("run", null);
			return true;
		}
		
		Logger.warn("Service with not run method definition");
		return true;
	}


	/**
	 * Delegate to the script 'done' if exists
	 */
	@Override
	protected boolean done(boolean success) {
		return delegate != null ? delegate.done(success) : super.done(success);
	}
	
}

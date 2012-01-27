package models;

import exception.CommandException;
import exception.QuickException;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import bundle.BundleScriptLoader;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import converters.ScriptCommandConverter;


/**
 * Execute Groovy scripts in the bundle context
 * 
 * @author Paolo Di Tommaso
 *
 */

@XStreamAlias("script-command")
@XStreamConverter(ScriptCommandConverter.class)
public class ScriptCommand extends AbstractCommand {
	
	public String fScriptText; 
	
	public String fScriptClass;

	public String fScriptFile;
	
	GroovyClassLoader gcl;
	
	AbstractCommand delegate;
	
	GroovyObject script;

	BundleScriptLoader loader;
	
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
		this.fScriptClass= that.fScriptClass ;
		this.fScriptFile = that.fScriptFile;		
	}
	
	/**
	 * The command initialize phase 
	 */
	@Override
	public void init(ContextHolder ctx) {
		super.init(ctx);

		if( loader == null ) {
			loader = Service.current().bundle.getScriptLoader();
		}
		

		/*
		 * when the element defines a script text, parse it a 
		 */
		if( StringUtils.isNotBlank(fScriptText) ) {
			script = (GroovyObject) loader.getExtensionByScript(fScriptText);
		}
		
		/*
		 * otherwise if the 'file' attribute is provided, it will parsed as a groovy class either:
		 * - 1) that extends a 'AbstractCommand' class, that will used as a 'delete' for this class
		 * - 2) or a generic groovy code, that will be parsed as a script 
		 */
		else if( StringUtils.isNotBlank(fScriptFile) ) {
			Object obj = loader.getExtensionByFile(fScriptFile);
			
			if( obj instanceof AbstractCommand ) {
				delegate = (AbstractCommand) obj;
			}
			else {
				script = (GroovyObject) obj;
			}
		}
		/*
		 * of when the 'clazz' attribute is provided it must extend the 'AbstractCommand' class 
		 * and a instance is created
		 */
		else if( StringUtils.isNotBlank(fScriptClass) ) {
			delegate = (AbstractCommand) loader.getExtensionByClass(fScriptClass);
		}
		
		/* unknwon error */
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

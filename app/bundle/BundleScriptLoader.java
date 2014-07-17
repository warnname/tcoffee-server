package bundle;

import exception.QuickException;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.FilenameFilter;

import play.Logger;
import play.Play;

/**
 * Load classes and script in the bundle context 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class BundleScriptLoader {

	File scriptPath;
	
	File libPath;
	
	GroovyClassLoader gcl; 
	
	{
		gcl = new GroovyClassLoader(Play.classloader);
	}
	
	/**
	 * Initialize the loader using the current path as the path 
	 * where scripts are located 
	 */
	public BundleScriptLoader() {
		init( new File("."), null );
	} 

	/**
	 * Initialize the classload accordingly with the 'scripts' and libraries path specified 
	 * 
	 * @param scripts the path where the scritps to load are located 
	 * @param libs the path where the libraries '.jar' are located 
	 */
	public BundleScriptLoader( File scripts, File libs ) {
		init(scripts, libs);
	}


	/**
	 * Initialize the classload accordingly with the 'scripts' and libraries path specified 
	 * 
	 * @param scripts
	 * @param libs
	 */
	void init(File scripts, File libs) {

		this.scriptPath = scripts;
		this.libPath = libs;
		
		// add the labraries to the classpath 
		if( libPath != null ) {
			addLibClasspath(libPath);
		}

		// add the script path to the classpath
		if( scriptPath != null ) {
			gcl.addClasspath(scriptPath.getAbsolutePath());
		}

	}

	/**
	 * Add all the java libraries '.jar' files to the specified groovy class loader.
	 * 
	 * @param loader to classloader to which classpath append the libraries 
	 * @return the liast of added paths 
	 */
	void addLibClasspath(File libPath) {

		if( libPath == null ) {
			return ;
		}
		
		if( !libPath.exists() || !libPath.isDirectory() ) {
			Logger.warn("The libraries path provided is not valid: '%s'", libPath);
			return ;
		}
		
		libPath.list( new FilenameFilter() {
/**			@Override **/
			public boolean accept(File dir, String name) {
				boolean yes = name.toLowerCase().endsWith(".jar");
				if( yes ) { 
					String _it = new File(dir,name).getAbsolutePath();
					gcl.addClasspath( _it );
				}
				return yes;
			}
		});		

	} 
	
	
	public Object getExtensionByFile( String theScriptFile ) {
		
		try {
			Class clazz = gcl.parseClass(new File(scriptPath,theScriptFile));
			return clazz.newInstance();
		} 
		catch (Exception e) {
			throw new QuickException(e, "Cannot parse script file '%s'", theScriptFile);
		}
		
		
	}
	
	public Object getExtensionByClass( String className ) {

		try {
			return gcl.loadClass(className).newInstance();
		} 
		catch (Exception e) {
			throw new QuickException(e, "Cannot create script class '%s'", className);
		}
	
		
	}

	/**
	 * Create an instance of the script object 
	 * 
	 * @param script a groovy script to be executed in the bundle context 
	 * 
	 * @return A {@link GroovyObject}
	 */
	public Object getExtensionByScript( String script ) {

		try {
			return gcl.parseClass(script).newInstance();
		} 
		catch (Exception e) {
			throw new QuickException(e, "Cannot parse provided script");
		}
	}
	
			
}

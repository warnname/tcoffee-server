package controllers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import models.AppProps;
import models.Bundle;
import models.PageContent;
import models.Repo;
import models.Service;
import models.TCoffeeCommand;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.IO;
import play.mvc.Before;
import play.mvc.Scope.Session;
import play.mvc.With;
import play.mvc.results.Result;
import play.templates.JavaExtensions;
import play.vfs.VirtualFile;
import util.Check;
import util.FileIterator;
import util.Utils;
import util.XStreamHelper;
import bundle.BundleException;
import bundle.BundleRegistry;
import edu.emory.mathcs.backport.java.util.Collections;
import exception.QuickException;

/**
 * The administration panel controller 
 * 
 * See {@link Security}
 * 
 * @author Paolo Di Tommaso
 *
 */
@With(Secure.class)
public class Admin extends CommonController {

	
	@Before
	static void before() { 
		injectImplicitVars();
	}
	
	/*
	 * load a service template configuration file 
	 *  
	 * @param path
	 * @return
	 */
	@Deprecated
	static Service loadModuleFile( String path  ) {
		for( VirtualFile templatePath : Play.templatesPath ) {
			if( templatePath == null ) continue;
			
			VirtualFile file = templatePath.child(path);
			if( file.exists() ) {
				return XStreamHelper.fromXML( file.getRealFile() );
			}
		}
		
		throw new QuickException("Specified template file definition does not exists: %s", path);
	}	
	
	
	
	/**
	 * Show the administration index page
	 */
	public static void index() {
		render();
	}
	
	/**
	 * Implements the clear cache function. Invoking it with GET method show a confirmation page, 
	 * when it is invoked with POST method (pressing the confirmaton button) will delete all the 
	 * cached directories
	 * 
	 * see {@link Repo#deleteAll()}
	 */
    public static void clearCache() {
    	if( isGET() ) {
    		/* ask confirmation */
    		render();
    	}
    	else if( isPOST()) {
    		/* DO IT - delete any job repo in any status */
    		Repo.deleteAll();
    		index();
    	}
    	
    }
 
    /**
     * Let to inspect and modify application properties 
     * 
     */
    public static void appinfo() {
    	final String cachekey = session.getId() + "_appinfo";

    	AppProps props = null;
    	if( isGET() ) { 
    		/* create a copy to work on and save in the cache  */
    		props = new AppProps(AppProps.instance());
    		Cache.set(cachekey, props);
		
    	}
    	else if ( isPOST () ) { 
    		/* save to file */
    		props = (AppProps) Cache.get(cachekey);
    		props.save();
    		
    		/* add the 'saved' flag to notify the save action */
    		renderArgs.put("saved", true);
    		
    	}

    	if( props == null ) { 
    		error("Missing application properties");
    	}
    	
    	List<String> names = props.getNames();
    	Collections.sort(names);
	
		/* just render the form */
		render(props,names);
    }
    
    public static void editprop( String element_id, String original_html, String update_value) { 
    	final String cachekey = session.getId() + "_appinfo";
    	AppProps props = (AppProps) Cache.get(cachekey);
    	props.setProperty(element_id, update_value);
    	
    	/* render back the updated value as confirmation */
    	renderText(update_value);
    }
    
    	
	/**
	 * Renders the System information page 
	 */
	public static void sysinfo() {
		TreeMap<Object,Object> map1 = new TreeMap<Object,Object>(System.getProperties());
		TreeMap<String,String> map2 = new TreeMap<String,String>(System.getenv());
		TreeMap<Object,Object> map3 = new TreeMap<Object,Object>( Play.configuration );

		render(map1,map2,map3);
	}
    
	/**
	 * Let the administrator edit the specified text file 
	 * @throws IOException 
	 */
	public static void editfile(final String file, String content) throws IOException {
		if( isGET() ) {
			/* 1. by default let's edit the main application conf file */
			String fullName;
			// if the specified fileName is not absoulute (does not start with '/') 
			// consider it relative to data root 
			if( !file.startsWith("/") ) {
				File abs = new File(AppProps.instance().getDataPath(), file);
				fullName = Utils.getCanonicalPath(abs);
			}
			else {
				fullName = file;
			}
			
			/* 2. read the file */
			content = FileUtils.readFileToString(new File(fullName), "utf-8");
			
			/* 3. render the edit page */
			render(fullName, content);
		}
		
		if( isPOST() ) {
			Check.notEmpty(file, "Argument 'fileName' cannot be empty");
			Check.notNull(content, "Argument 'fileContent' cannot be null");
			
			/* 1. backup the old file if exits */
			String backup = null;
			File target = new File(file);
			if( target.exists() ) {
				backup = file + ".bak";
				FileUtils.copyFile(target, new File(backup));
			}
			
			/* 2. save the new file */
			FileUtils.writeStringToFile(target, content, "utf-8");
			
			/* 3. go to confirm page */
			PageContent page = new PageContent();
			page.title = "File saved";
			page.description = "Operation confirmation";
			page.addParagraph("Edited file has been saved successfully");
			if( backup != null ) {
				page.addParagraph("Previous version has been stored as: %s", backup);
			}
			
			page.addAutoLink("@Admin.index", "Admin home");
			renderGenericPage(page);
		}
		
		unsupportedMethod();
	}
	
	/**
	 * Retuns perl runtime info
	 */
	public static void perlinfo() {
		
		String info;
		Process p1;
		try {
			p1 = Runtime.getRuntime().exec("perl -v");
			p1.waitFor();
			info = IO.readContentAsString(p1.getInputStream());
		} catch (Exception e) {
			Logger.error(e, "Error retrieving PERL version info");
			info = "(unable to get perl information)";
		}
		
		
		Process p2;
		String conf = null;
		try {
			p2 = Runtime.getRuntime().exec("perl -V");
			p2.waitFor();
			conf = IO.readContentAsString(p2.getInputStream());
		} catch (Exception e) {
			Logger.error(e, "Error retrieving PERL version info");
			conf = "(unable to get perl configuration)";
		}
		
		render(info,conf);
	} 
	
	/**
	 * Display the current version of t-coffee information
	 */
	public static void tcoffeeinfo() {
		Pattern pattern = Pattern.compile("PROGRAM: T-COFFEE \\((\\S+)\\)" );
		
		String info = "(unknown)";
		String ver = "(unknown)";
		
		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.ctxfolder = new File(AppProps.instance().getDataPath(), ".tcoffee-ver");
		tcoffee.logfile = "info.txt";
		tcoffee.errfile = "err.txt";
		tcoffee.init();
		try {
			tcoffee.execute();
			
			/* 
			 * extract the tcoffee version 
			 */
			for( String line :  new FileIterator(tcoffee.getErrFile()) ) {
				Matcher m = pattern.matcher(line);
				if( m.matches() ) {
					ver = m.group(1); 
					break;
				}

			};
			
			/* 
			 * fecth the list of all installed services 
			 */
			info = IO.readContentAsString(tcoffee.getLogFile()).trim();
			
			String PREFIX = "#######   Compiling the list of available methods ... (will take a few seconds)";
			if( info.startsWith(PREFIX)) {
				info = info.substring(PREFIX.length()).trim();
			}
			
		} catch (Exception e) {
			Logger.error(e,"Unable to get t-coffee information");
			ver = "(unable to get t-coffee version info)";
		}
		
		
		render(ver, info);
	}	


	/**
	 * Show the list of installed bundles in the system 
	 */
	public static void bundleManager() { 
		BundleRegistry registry = BundleRegistry.instance(); 
		List<Bundle> bundles = registry.getBundles();
		List<String> loadingErrors = registry.errors; 
		render(bundles, loadingErrors);
	}
	
	/**
	 * Shows the bundle details information 
	 * 
	 * @param bundleName teh name of the {@link Bundle} instance for which shows information
	 * 
	 */
	public static void bundleDetails( String bundleName ) { 
		Bundle bundle = BundleRegistry.instance().get(bundleName);
		render(bundle);
	}
	
	public static void bundleDrop( String bundleName ) { 
		Bundle bundle = BundleRegistry.instance().get(bundleName);
		if( bundle == null ) 
		{ 
			throw new BundleException("Missing bundle '%s'", bundleName);
		}
		
		if( isGET() ) { 
			/*
			 * show the action confirm request
			 */
			render(bundleName);
		}
		
		if( isPOST() ) { 
			/* 
			 * remove the bundle and show confirmation feedback
			 */
			final BundleRegistry registry = BundleRegistry.instance();
			try { 
				registry.drop(bundle);
				renderArgs.put("success", true);
			}
			catch( Exception e ) { 
				Logger.error(e, "Error dropping bundle '%s'", bundleName);
				renderArgs.put("error", e.getMessage());
			}
			
			render(bundleName);
		}

		/* it should reach this */
		unsupportedMethod();
	}
	
	static String ERROR( String message ) { 
		return String.format("{error: '%s'}", JavaExtensions.escapeJavaScript(message));
	}
	
	public static void bundleUpload(String qqfile) { 
		Logger.debug("Bundle upload file name: '%s'", qqfile);
		final String BUNDLE_KEY = Session.current().getId() + "_bundleupload";
		Cache.delete(BUNDLE_KEY);
		
		File bundleZip = null;
		
		try {
			/* 
			 * 1. Stage
			 * copy the uploaded content to a temporary file 
			 * and return that name in the result to be stored in a hidden field
			 */

			bundleZip = File.createTempFile("bundle_", ".zip", AppProps.BUNDLE_UPLOAD_PATH);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(bundleZip));
			IO.write(new BufferedInputStream(request.body), out);
			out.close();
			
			/* default error result */
			Logger.debug("Bundle upload save file: '%s'", bundleZip);

			/* check that uploaded file is valid */
			if( bundleZip==null ) {
				throw new BundleException("Unable to retrieve uploaded bundle");
			}
			
			/* error condition: wtf is that file ? */
			if( !bundleZip.exists() ) {
				throw new BundleException("Bundle uploaded file does not exist: '%s'", bundleZip);
			}
			
			
			/*
			 * 2. Stage
			 * Unzip the bundle in a temporaty folder 
			 */
			File stagingRoot = File.createTempFile("bundle_",null, AppProps.BUNDLE_UPLOAD_PATH);
			stagingRoot.delete();
			stagingRoot.mkdir();
			
			ZipFile zip = new ZipFile(bundleZip);
			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
			while( entries.hasMoreElements() ) { 
				ZipEntry entry = entries.nextElement();
				
				File file = new File(stagingRoot, entry.getName() );
				if(entry.isDirectory()) {
			        // Assume directories are stored parents first then children.
			        // This is not robust, just for demonstration purposes.
					if( !file.mkdirs() ) { 
						throw new BundleException("Unable to create path '%s' unzipping bundle: '%s'", file, qqfile);
					}
					continue;
				}
				else {
					if( !file.getParentFile().exists() ) {
						file.getParentFile().mkdirs();
					}
					
					BufferedOutputStream stream =  new BufferedOutputStream(new FileOutputStream(file));
					IO.write(zip.getInputStream(entry), stream);
					stream.close();
				} 

			}
			zip.close();
			// the zip file can be deleted after the content has been extracted 
			bundleZip.delete();
			
			  			
			/*
			 * 3. Stage 
			 * Locate the right path and read the bundle instance
			 */
			File bundlePath = null;
			if( BundleRegistry.isBundlePath(stagingRoot) ) { 
				bundlePath = stagingRoot;
			}
			else { 
				// find the first sub-dir, it should contains the bundle 
				File[] all =  stagingRoot.listFiles();
				if( all!=null ) for( File file : all ) { 
					if( file.isDirectory() && BundleRegistry.isBundlePath(file)) {
						bundlePath = file;
						break;
					}
				}
			}
			
			Bundle bundle = null;
			if( bundlePath != null ) { 
				bundle = Bundle.read(bundlePath);
			}
			
			if( bundle == null ) { 
				throw new BundleException("The uploaded file does not contains a recognized bundle file format");
			}

			/*
			 * 4. Verify bundle
			 */
			bundle.verify();
			
			
			String SUCCESS = String.format("{success:true, name: '%s', version: '%s', key: '%s' }",
						JavaExtensions.escapeJavaScript(bundle.name),
						JavaExtensions.escapeJavaScript(bundle.version),
						JavaExtensions.escapeJavaScript(BUNDLE_KEY)
			);
			
			Cache.set(BUNDLE_KEY, bundle);
			renderText(SUCCESS);
			
		}
		catch( Result result ) { 
			throw result;
		}
		catch( BundleException e) { 
			Logger.error(e.getMessage());
			renderText(ERROR(e.getMessage()));
		}
		catch( Exception e ) {
			String cause = null;
			if( e.getCause() != null ) { 
				cause = e.getCause().getMessage();
			}
			if( cause == null ) { 
				cause = e.getMessage();
			}
			Logger.error(e, "Unable to copy temporary upload file: '%s'", bundleZip);
			renderText(ERROR(cause));
		}
	
	}
	
	/**
	 * Let to upload and install a new bundle in the system 
	 */
	public static void bundleInstall(String key) {
		final Bundle bundle = (Bundle) Cache.get(key);
		final BundleRegistry registry = BundleRegistry.instance();
		final Bundle installed = registry.get(bundle.name);

		
		if( isGET() ) { 
			
			if ( installed != null && bundle.version.compareTo(installed.version)<0 ) { 
				String error = 
					"A bundle with the same name and higher version it is already installed. " +
					"Before install the uploaded version you need to remove the current version.";
				renderArgs.put("message", error);
				renderArgs.put("message_class", "box-error");
			}
			else if( installed != null && bundle.version.equals(installed.version)) { 
				String warn = 
					"A bundle with the same name and version it is already installed. " +
					"If you proceed the current version will be deleted and replaced by the uploaded one. " +
					"NOTE: the operation cannot be undone.";
				renderArgs.put("message", warn);
				renderArgs.put("message_class", "box-warn");
			}
			else if( installed != null ) { 
				String warn = 
					"A bundle with the same name it is already installed. " +
					"If you proceed the current version will be replaced by the uploaded one. ";
				renderArgs.put("message", warn);
				renderArgs.put("message_class", "box-warn");
				
			}

			/*
			 * ask for confirmation
			 */
			render(bundle);
		}
		
		
		if ( isPOST() ){
			Cache.delete(key);
			
			/* 
			 * 1. remove the current one if the version is the same 
			 */
			if( installed != null && installed.version.equals(bundle.version)) { 
				registry.drop(installed);
			}
			
			/*
			 * 2. move the new on bundle folder 
			 */
			final String name = bundle.name + "_" + bundle.version;
			File target = new File( AppProps.BUNDLES_FOLDER, name);
			while( target.exists() ) { 
				String randomName = name + "_" + Math.round(Math.random() * 1000); 
				target = new File( AppProps.BUNDLES_FOLDER, randomName); 
			}
			
			try {
				final File source = bundle.root;
				FileUtils.moveDirectory(source, target);

				/*
				 * 3. force installation 
				 */
				registry.load(target);
				if( source.exists() && !FileUtils.deleteQuietly(source) ) { 
					Logger.warn("Unable to remove bundle staging path: '%s'", source);
				}
				renderArgs.put("message", "Bundle installed successfully");
				renderArgs.put("message_class", "box-success");
			} 
			catch (Exception e) {
				String error = "Cannot install application bundle. " + e.getMessage();
				renderArgs.put("message", error);
				renderArgs.put("message_class", "box-error");
			}
			
			render(bundle);
		}

		unsupportedMethod();
	} 
 }


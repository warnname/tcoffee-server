package controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.AppProps;
import models.Field;
import models.Module;
import models.PageContent;
import models.Property;
import models.Repo;
import models.TCoffeeCommand;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.libs.IO;
import play.mvc.With;
import util.Check;
import util.FileIterator;
import util.Utils;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * The admin panel controller 
 * 
 * @author Paolo Di Tommaso
 *
 */
@With(Secure.class)
public class Admin extends BaseController {

	public static void index() {
		render();
	}
	
	
    public static void clearAll() {
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
 
    public static void configure() {
    	Module module = loadModuleFile("templates/configure.xml");


    	if( isGET() ) {
    		/* bind to the current configuration properties */
    		
    		AppProps props = AppProps.instance();
    		
    		/* bind the input fields with the properties values */
    		for( Field field : module.input.fields() ) {
    			field.value = props.getString(field.name);
    		}
    		
    	}
    	
    	else if( isPOST() ) {
    		/* fetch submitted data */
    		boolean isValid = module.validate(params);
    		if( isValid ) {
    			/*
    			 * 1. store data
    			 */
        		AppProps props = AppProps.instance();
    			for( Field field : module.input.fields() ) {
    				Property prop = new Property( "password".equals(field.type) );
    				prop.setName(field.name);
    				prop.setValue(field.value);
        			props.put(prop);
        		}
    			
    			/*
    			 * 2. store the properties file  
    			 */
    			props.save();
    			
    			/*
    			 * 3. confirm page
    			 */
    			PageContent content = new PageContent();
    			content.title = "Configuration saved";
    			content.addParagraph("Configuration properties have been saved properly.");
    			content.addAutoLink( "@Admin.index", "Home" );

    			renderGenericPage(content);
    		}
    		
    	}
    	
		/* just render the form */
		render("Application/module.html", module);
		return;
    	
    }
    
    	
	/**
	 * Renders the System information page 
	 */
	public static void sysinfo() {
		List<String> list2 = new ArrayList<String>(System.getenv().keySet());
		List<String> list1 = new ArrayList(System.getProperties().keySet());
		List<String> list3 = new ArrayList( Play.configuration.keySet() );
		Properties playConf = Play.configuration;
		
		Collections.sort(list1);
		Collections.sort(list2);
		Collections.sort(list3);
		render(list1,list2,list3,playConf);
	}
    
	/**
	 * Let the administrator edit the specified text file 
	 * @throws IOException 
	 */
	public static void edit(final String file, String content) throws IOException {
		if( isGET() ) {
			/* 1. by default let's edit the main application conf file */
			String fullName;
			if( Utils.isEmpty(file)) {
				fullName = Utils.getCanonicalPath(AppProps.SERVER_CONF_FILE);
			}
			// if the specified fileName is not absoulute (does not start with '/') 
			// consider it relative to data root 
			else if( !file.startsWith("/") ) {
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
			 * fecth the list of all installed modules 
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


	
}

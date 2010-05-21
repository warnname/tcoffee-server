package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import models.AppConf;
import models.History;
import models.Module;
import models.OutItem;
import models.OutResult;
import models.Repo;
import models.Status;
import play.Logger;
import play.libs.IO;
import play.mvc.After;
import play.mvc.Before;
import play.templates.JavaExtensions;
import util.Utils;
import edu.emory.mathcs.backport.java.util.Arrays;
import exception.QuickException;

/**
 * The main application controller 
 * 
 * @author Paolo Di Tommaso
 *
 */

public class Application extends BaseController {
	
	static List<String> PAGES = Arrays.asList(new String[]{"index", "history", "references", "help", "contacts" });
	
	
	@Before
	static void init() {
		String a = request.actionMethod;
		if( PAGES.contains(a)) {
			session.put("menu", a);
		}
		else {
			a = session.get("menu");
			if( !PAGES.contains(a) ) {
				session.put("menu", PAGES.get(0));
			}
		}
		
	}
	
	@After 
	static void release() {
		Module.release();
	}
	
	/** 
	 * Renders the main application index page
	 */
    public static void index() {
    	AppConf conf = AppConf.instance();
        render(conf);
    }
    
    /**
     * Renders the <i>References</i> html page
     */
    public static void references() {
    	render();
    }
    
    /**
     * Renders the <i>Help</i> html page
     */
    public static void help() {
    	render();
    }
    
    /**
     * Renders the <i>Contacts</i> html page
     */
    public static void contacts() {
    	render();
    }
    

    /**
     * Handles the Welcome page showed at application first run 
     */
    public static void welcome() {
    	render();
    }
    
    

    /*
     * fake page used for tests purpose only   
     */
    public static void sandbox() {
    	render();
    }
    
 
    /**
     * Handle request to display the <i>result</i> page
     * 
     * @param rid the unique request identifier 
     */
    public static void result(String rid) {		
    	
    	final Repo ctx = new Repo(rid,false);

    	/* 
    	 * 1) otherwise - when the rid folder exists - check if the t-coffee job is terminated 
    	 * 
    	 */ 
    	Status status = ctx.getStatus();
		if( status.isDone()) {
			// if the file exists load the result object and show it
			OutResult result = ctx.getResult();
    		render(rid,result);		
		}
		else if( status.isFailed() ) {
			OutResult result = ctx.getResult();
	    	renderArgs.put("rid", rid);
	    	renderArgs.put("result", result);
	    	render("Application/resultFail.html");
		}
		else if( status.isUnknown() ) {
	    	renderArgs.put("rid", rid);
	    	render("Application/resultUnknown.html");
		}
    	
    	/* 
    	 * 4) otherwise wait until the request is available 
    	 */
    	renderArgs.put("rid", rid);
    	render("Application/wait.html");
 
   }

	/**
	 * Renders the history page 
	 */
	public static void history() { 
		render();
	}
	
	/**
	 * Renders the history html table  
	 */
	public static void historyTable() {
		List<History> recent = History.findAll();
		Collections.sort(recent, History.DescBeginTimeSort.INSTANCE);
		render(recent);
	}

	
	/**
	 * Check the current status for the alignment request specified by <code>rid</code>
	 * 
	 * @param rid the request unique identifier
	 */
	public static void status(String rid) {
		Repo ctx = new Repo(rid,false);
		renderText(ctx.getStatus().toString());
	}
	
	
	/**
	 * Renders a generic t-coffee 'module' i.e. a specific configuration defined in the main application file 
	 * 
	 * @param name the <i>module</i> name i.e. is unique identifier
	 */
	public static void module(String name) {
		
		if( isGET() ) {
			Logger.debug("Rendering module w/t name: %s", name);
			Module module = AppConf.instance().module(name);
			render(module);
			return;
		}

		/*
		 * process the submitted data
		 */
		String mid = params.get("_mid");
		Module module = AppConf.instance().module(mid).copy();
		Module.current(module);

		if( !module.validate(params) ) {
			/* if the validation FAIL go back to the module page */
			renderArgs.put("module", module);
			render();
			return;
		} 

		/*
		 * prepare for the execution
		 */
		module.prepare();
		
		
		Status status = module.repo().getStatus();
		if( module.repo().isExpired(status) ) {
			module.repo().clean();
		}
		else {
			if( status.isRunning() || status.isDone() || status.isFailed() ) {
		    	Logger.debug("Current request status: '%s'. Forward to result page with rid: %s", status, module.rid());
		    	result(module.rid());
		    	return;
			}
		}
		
		if( module.start() ) {
	    	/*
	    	 * 3. store the current request-id in a cookie
	    	 */
	    	History history = new History(module.rid());
	    	history.setMode(module.title);
	    	history.save();		
		}
		

    	/*
    	 * forwards to the result page 
    	 */
    	Logger.debug("Forward to result page with rid: %s", module.rid());
    	result(module.rid());		
	}
	

	/**
	 * Create a temporary zip file with all generated content and download it
	 * 
	 * @param rid the request identifier
	 * @throws IOException 
	 */
	public static void zip(String rid) throws IOException {
		Repo repo = new Repo(rid);
		if( !repo.hasResult() ) {
			notFound(String.format("The requested download is not available (%s) ", rid));
			return;
		}
		
		OutResult result = repo.getResult();
		File zip = File.createTempFile("download", ".zip", repo.getFile());
		zipThemAll(result.getItems(), zip);
		renderBinary(zip, String.format("tcoffee-all-files-%s.zip",rid));
	}
	
	static void zipThemAll( List<OutItem> items, File file ) {

		try {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
			
			for( OutItem item : items ) { 
				if( item.file==null || !item.file.exists() ) { continue; }
				
				// add a new zip entry
				zip.putNextEntry( new ZipEntry(item.file.getName()) );
				
				// append the file content
				FileInputStream in = new FileInputStream(item.file);
				IO.write(in, zip);
	 
				// Complete the entry 
				zip.closeEntry(); 
				in.close(); 		
			}
			
			zip.close();					
		}
		catch (IOException e) {
			throw new QuickException(e, "Unable to zip content to file: '%s'", file);
		}
	} 
	
	
	public static void upload(String name) {
		/* default error result */
		String ERROR = "{success:false}";
		
		/* 
		 * here it is the uploaded file 
		 */
		File file = params.get(name, File.class);
		
		/* uh oh something goes wrong .. */
		if( file==null ) {
			Logger.error("Ajax upload is null for field: '%s'", name);
			renderText(ERROR);
			return;
		}
		
		/* error condition: wtf is that file ? */
		if( !file.exists() ) {
			Logger.error("Cannot find file for ajax upload field: '%s'", name);
			renderText(ERROR);
			return;
		}

		/* 
		 * copy the uploaded content to a temporary file 
		 * and return that name in the result to be stored in a hidden field
		 */
		try {
			File temp = File.createTempFile("upload-", null);
			// to create a temporary folder instead of a file delete and recreate it 
			temp.delete();
			temp.mkdir();
			temp = new File(temp, file.getName());
			
			FileUtils.copyFile(file, temp);
			String filename = Utils.getCanonicalPath(temp);
			renderText(String.format("{success:true, name:'%s', path:'%s', size:'%s'}", 
						file.getName(),
						JavaExtensions.escapeJavaScript(filename),
						FileUtils.byteCountToDisplaySize(temp.length())
						));
		}
		catch( IOException e ) {
			Logger.error(e, "Unable to copy temporary upload file: '%s'", file);
			renderText(ERROR);
		}
		
	}
	

	

}
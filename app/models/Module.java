package models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.data.validation.Validation;
import play.jobs.Job;
import play.mvc.Router;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import play.mvc.Scope.Session;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import exception.QuickException;
 
@XStreamAlias("module")
public class Module {

	static ThreadLocal<Module> CURRENT = new ThreadLocal<Module>();
	
	/** the parent configuration object */
	@XStreamOmitField
	public AppConf conf; 
	
	@XStreamOmitField Map<String,Object> fCtx;
	@XStreamOmitField String fRid;
	@XStreamOmitField Repo fRepo;
	@XStreamOmitField OutResult fOutResult;
	@XStreamOmitField Date fStartTime;
	@XStreamOmitField String fRemoteAddress;
	
	/**
	 * The unique module name
	 */
	@XStreamAsAttribute
	public String name; 

	/** A label used to describe the group to which this module belongs to */
	public String group;
	
	/**
	 * The module main <i>title</i>. It will be displayed in the index page and on top of the module form input data
	 */
	public String title;

	/**
	 * The module <i>description</i>. It will be displayed in the index page and on top of the module form input data
	 */
	public String description;

	/**
	 * The reference to the related ncbi articles related to the selected tcoffee module
	 */
	public String cite; 
	
	
	/**
	 * The input data model
	 */
	public Input input;
	

	/**
	 * Define the main job to be executed in this module
	 */
	public ProcessCommand process;
	
	
	/**
	 * Defines the output of this module execution 
	 */
	public Output output;
	
	/**
	 * The defualt constructor. Initialize the class to empty 
	 */
	public Module() {
		
	}
	
	public Module( String name ) {
		this.name = name;
	}
	
	public Module( final AppConf conf ) {
		this.conf = conf;
	}
	
	
	/** 
	 * Module cony constructor. Creates a copy of <code>that</code> instance
	 */
	public Module( Module that ) {
		this.conf = that.conf; // <-- be aware the - parent - configuration must NOT be copied 
		this.name = Utils.copy(that.name); 
		this.group = Utils.copy(that.group);
		this.title = Utils.copy(that.title); 
		this.description = Utils.copy(that.description); 
		this.cite = Utils.copy(that.cite);
		this.input = Utils.copy(that.input);
		this.process = Utils.copy(that.process);
		this.output = Utils.copy(that.output);
	}
	
	/**
	 * @return a cloned instance of the current module
	 */
	public Module copy() {
		return new Module(this);
	}
	
	public String getTitle() {
		return Utils.isNotEmpty(title) ? title : name;
	}
	
	public String rid() {
		if( fRid != null ) {
			return fRid;
		}

		return fRid = getRid();
	}
	
	private String getRid() {
		return getRid(true);
	}
	
	private String getRid( boolean enableCaching ) {
		if( input == null ) { return null; };
		
		int hash = input.hashFields();
		hash = Utils.hash(hash, this.name);
		hash = Utils.hash(hash, Session.current().getId());
		hash = Utils.hash(hash, this.conf.getLastModified());

		/* 
		 * Avoid clash on existing folder with unknown status, 
		 * so basically check if for the current hash (rid) already exists 
		 * a folder, if so loop until a non existing hash(<--> folder) is found 
		 */
		String result = Integer.toHexString(hash);
		Repo check = new Repo(result,false);
		while( check.getFile().exists() ) {
			Status status = check.getStatus();
			if( !enableCaching || check.isExpired() || status .isUnknown() ) {
				// force a new hash id 
				hash = Utils.hash(hash,result);
				result = Integer.toHexString(hash);
				check = new Repo(result,false);
			}
			else {
				break;
			}
		}
		
		return fRid = result;
		
	} 

	@Deprecated
	public File folder() {
		return fRepo != null ? fRepo.getFile() : null;
	}
	
	public Repo repo() {
		return fRepo;
	}
	
	public static Module current() {
		return CURRENT.get();
	}
	
	public static Module current(Module module) {
		CURRENT.set(module);
		return module;
	}
	
	public static void release() {
		CURRENT.set(null);
	}

	/**
	 * Validate all input fields against the specified http parameters 
	 * @param params the parameters on the current http request  
	 * @return <code>true</code> if validation is OK <code>false</code> otherwise 
	 */
	public boolean validate(Params params) {
		fRid = null; // <-- invalidate the current 'request-id' if exists 
		input.bind(params);
		input.validate();
		return !Validation.hasErrors();
	} 
	
	/*
	 * set a variable onto the binding context
	 */
	void setVariable( Field field ) {
		Object value = null;
		
		/* just skip empty fields */
		if( Utils.isEmpty(field.value) ) {
			return;
		}
		
		/*
		 * memo fields are store as temporary files and in the context is passed that file 
		 */
		if( "memo".equals(field.type) ) {
			File file=null;
			try {
				file = File.createTempFile("input-", ".txt", folder());
				FileUtils.writeStringToFile(file, field.value, "utf-8");
				value = file;
			} catch (IOException e) {
				throw new QuickException(e, "Unable to save memo field: '%s' to temp file: '%s'", field.name, file);
			}
		}
		/* 
		 * file are are managed in a similar way that 'memo' field
		 */
		else if( "file".equals(field.type) ) {
			byte[] data = field.getFileContent();
			if( data == null && data.length == 0 ) {
				/* empty file - do not put this variable on the context */
				return;
			}
			
			File file=null;
			try {
				file = File.createTempFile("input-",null, folder());
				FileUtils.writeByteArrayToFile(file, data);
				value = file;
			} catch (IOException e) {
				throw new QuickException(e, "Unable to save memo field: '%s' to temp file: '%s'", field.name, file);
			}
			
			
		}
		else {
			value = field.value;
		}

		setVariable(field.name, value);
	}

	void setVariable( String key, Object value ) {
		
		if( fCtx.containsKey(key) ) {
			/* if an entry already exist with this key, repack it as a list */
			Object item = fCtx.get(key);
			if( item instanceof List ) {
				((List)item) .add(value);
				return;
			}
			else {
				List<Object> list = new ArrayList<Object>();
				list.add(item);
				list.add(value);
				value = list;
			} 
		}
		
		/* 
		 * put on the context 
		 */
		fCtx.put(key, value);
		
	}
	
	
	public Map<String,Object> getCtx() {
		return fCtx;
	} 
	
	public void init() {
		init(true);
	}
	
	/**
	 * Prepare the <i>module</i> to be executed 
	 */
	public void init( boolean enableCaching ) {
		
		/*
		 * 0. generic initialization 
		 */
		fStartTime = new Date();
		fRemoteAddress = Request.current().remoteAddress;
		
		/*
		 * 1. create the context repository folder for this execution 
		 */
		fRid = getRid(enableCaching);
		fRepo = new Repo(fRid,true);
	
		/*
		 * 2. initialize the context for the expression evaluation 
		 */
		fCtx = new HashMap<String,Object>();
		/* some 'special' variables */
		setVariable("_rid", rid());
		setVariable("_result_url", getResultURL());
		
		/* add the app properties content */
		for( Property property : AppProps.instance().list() ) {
			setVariable(property.name, property.value);
		}
		
		
		/* 
		 * 3. add all fields value as context variables 
		 */
		for( Field field : input.fields() ) {
			setVariable(field);
		}
		
		
		/*
		 * 4. store the input so that can be used to re-submit job execution
		 */
		input.save( fRepo.getInputFile() );
	}

	
	String getResultURL() {
		StringBuilder result = new StringBuilder();
		result.append("http://")
			.append(Request.current().host)
			.append( Router.reverse("Application.result").toString() )
			.append("?rid=") .append(rid());
		return result.toString();
	}
	
	public boolean start() {

		/* check if a main process is defined otherwise skip it */
		if( process == null ) { 
			Logger.warn("Nothing to process");
			return false;
		};

		
		/* try to lock this context for execution */
		if( !fRepo.lock() ) {
			// if cannot be lock it mean that the job is still running 
			return false;
		}
		
		
		/* create an aysnc execution context */
		Job<?> job = new Job() {
    		@Override
    		public void doJob() throws Exception {
    			/*
    			 * run the alignment job
    			 */
    			Module.current(Module.this);
    			try {
    				/* run the job */
    				Module.this.run();
    			}
    			finally  {
    				try { Module.this.fRepo.unlock(); } catch( Exception e ) { Logger.error(e, "Failure on context unlock"); }
    				try { Module.this.trace(); } catch( Exception e ) { Logger.error(e, "Failure on request logging"); }
    				Module.release();
    			}
    		}
    	}; 
    	
    	/* 
    	 * fire the job asynchronously 
    	 */
    	job.now();
    	return true;
	}

	/**
	 * Append a line in the server requests log with the following format 
	 * 
	 * <start time>, <user ip>, <module name>, <request id>, <elapsed time>, <status> 
	 */
	void trace() {
		try {
			DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			StringBuilder line = new StringBuilder();
			line.append( fmt.format(fStartTime) ).append(",")
				.append( fRemoteAddress ).append(",")
				.append( this.name ).append(",")
				.append( this.fRid ).append(",")
				.append( fOutResult!=null ? String.valueOf(fOutResult.elapsedTime) : "-") .append(",")
				.append( fOutResult!=null && fOutResult.status!=null ? fOutResult.status.name() : "-");

			PrintWriter out = new PrintWriter(new FileWriter(AppProps.SERVER_LOG_FILE, true), true);
			out.println(line.toString());
			out.close();
		} 
		catch (IOException e) {
			throw new QuickException(e, "Unable to trace request in log file: %s", AppProps.SERVER_LOG_FILE );
		}
	}

	void run() {
		
		/* 
		 * initialize the process 
		 */
		process.init();
		
		/* 
		 * the main execution 
		 */
		fOutResult = null;
		boolean success = false; 
		
		
		try {
			/* run the main job */
			try {
				success = process.execute();
			}
			finally {
				
				/*
				 * if result is OK handle the commands for valid case  
				 */
				OutSection branch = getOutSection(success);
				fOutResult = branch.result;
				
				if( process.hasResult() ) {
					fOutResult.addAll(process.getResult());
					fOutResult.elapsedTime = process.elapsedTime;
					fOutResult.status = success ? Status.DONE : Status.FAILED;
					fOutResult.mode = this.name;
					fOutResult.title = this.title;
					fOutResult.cite = this.cite;
				}
				
				/*
				 * execute the result events 
				 */
				if( branch.hasEvents() ) {
					branch.events.execute();
					if( branch.events.getResult() != null ) {
						branch.result.addAll( branch.events.getResult() );
					}
				}			
			}

		}
		catch( Exception e ) {
			/* trace the error in the log file */
			Logger.error(e, "Error processing request # %s", fRid);
			if( fOutResult == null ) fOutResult = defaultOutSection(false).result;
			fOutResult.status = Status.FAILED;
			fOutResult.addError( e.getMessage() );
			
		}
		finally {
			/* garantee to save the result object in any case */
			fRepo.saveResult(fOutResult);
		}


	}
	
	OutSection getOutSection(boolean status) {
		/* initialize the standard output if it has not been specified */
		OutSection section = defaultOutSection(status); 
		
		if( output != null ) {
			section.append( status ? output.valid : output.fail );
		}

		/* garantee a result object */
		if( section.result == null ) {
			section.result = new OutResult();
		}

		return section;
	} 
	
	OutSection defaultOutSection(boolean success) {
		OutSection out = null;
		
		if( conf != null && conf.def != null ) {
			if( success && conf.def.validResult != null ) {
				out = new OutSection(conf.def.validResult);
			}
			else if( !success && conf.def.failResult != null ){
				out = new OutSection(conf.def.failResult);
			}
		}
		
		if( out == null ) {
			out = new OutSection();
			out.result = new OutResult();
			out.title = success ? "OK" : "Fail";
		}
		
		return out;
	}

}

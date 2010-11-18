package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Logger;
import util.FileIterator;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import exception.QuickException;

@XStreamAlias("tcoffee")
public class TCoffeeCommand extends AbstractShellCommand {

	static final Pattern RESULT_PATTERN = Pattern .compile("^\\s*\\#{4} File Type=(.+)Format=(.+)Name=(.+)$");

	static final Pattern WARNING_PATTERN = Pattern .compile("^\\d+ -- WARNING: (.*)$");
	
	/** The t-coffee arguments */
	public CmdArgs args;

	private String fInputFileName;
	
	@XStreamOmitField
	List<String> _warnings;
	
	/** The default constructor */
	public TCoffeeCommand() {
	}

	/** The copy constrcutor */
	public TCoffeeCommand(TCoffeeCommand that) {
		super(that);
		this.args = Utils.copy(that.args);
	}
	
	@Override
	public void init(CommandCtx ctx) {

		/* an outfile is mandatory required */
		if( Utils.isEmpty(logfile) ) logfile = "_tcoffee.out.log";
		if( Utils.isEmpty(errfile) ) errfile = "_tcoffee.err.log";
		if( Utils.isEmpty(cmdfile) ) cmdfile = "_tcoffee.cmd.txt";
		if( Utils.isEmpty(envfile) ) envfile = "_tcoffee.env.txt";
		
		super.init(ctx);

	}
	
	@Override
	protected void onInitEnv(Map<String, String> map) {
		super.onInitEnv(map);
		
		makePath( map.get("DIR_4_TCOFFEE") );
		makePath( map.get("TMP_4_TCOFFEE") );
		makePath( map.get("LOCKDIR_4_TCOFFEE") );
		makePath( map.get("CACHE_4_TCOFFEE") );
	}

	
	protected void makePath( String path ) { 
		if( Utils.isEmpty(path) ) { 
			return;
		}
		
		File file = new File(path);
		if( !file.exists() && !file.mkdirs() ) {
			Logger.warn("Unable to create T-coffee path: '%s' ", file);
		}
		
	}
	
	@Override
	protected String onInitCommandLine(String cmdLine) {
		/*
		 * get t_coffee absolute path 
		 */
		String result = "t_coffee";
		
		/* 
		 * add the specified arguments for t-coffee 
		 */
		if (args == null) {
			return result;
		}
		
		/* 
		 * detect input file name 
		 */
		String inFile = args.get("infile");
		// fallback on alternate file input argument 
		if( Utils.isEmpty(inFile)) {
			inFile = args.get("in");
			// fallback on alternate file input argument 
			if( Utils.isEmpty(inFile)) {
				inFile = args.get("aln");
			}
		}
		
		if( Utils.isNotEmpty(inFile)) {
			int p = inFile.indexOf(' ');
			fInputFileName = (p==-1) ? inFile : inFile.substring(0,p);
			Logger.debug("t-coffee detected input file name: %s", fInputFileName);
		}
		
		/*
		 * check if database has been specified for local blast 
		 */
		if( "LOCAL".equals(args.get("blast")) ) {
			String db = args.get("pdb_db");
			if( Utils.isEmpty(db) ) {
				args.put("pdb_db", "${PDB_DB}");
			} 
				
			db = args.get("protein_db");
			if( Utils.isEmpty(db) ) {
				args.put("protein_db", "${PROTEIN_DB}");
			} 
		}
		
		/* 
		 * check any method has been specified otheriwse remove the parameter 
		 */
		String method = args.get("method");
		if( Utils.isEmpty(method)) {
			args.remove("method");
		}
		else if( method.contains("clustalw")) {
			Logger.warn("Note: multicore is not supported using 'clustal_xxx' method(s)");
			args.put("multi_core","no");
		}
		
		/* 
		 * Always override 'quit' attribute. This is required becase it influence as the stdout stderr are generated 
		 */
		String quiet = args.get("quiet");
		if( quiet != null && !quiet.equals("stdout") ) {
			Logger.warn("T-Coffee -quiet attribute is not supported. Setting to 'stdout'");
		}
		args.put("quiet", "stdout");
		
		/* return the command line */
		return result += " " + args.toCmdLine();
	}

	/**
	 * on job termination parse the result file to discover tcoffee output
	 */
	@Override
	protected boolean done(boolean success) {

		/* the result file MUST exists */
		/* check the result file does not contain an error */
		if( success && existsLogFile()) {
			String firstLine = new FileIterator(getLogFile()).iterator().next();
			success = firstLine != null && !firstLine.contains("ERROR:");
		}
		
		/* add the input file to the result object */
		if( fInputFileName != null ) {		

			OutItem out = new OutItem(fInputFileName,"input_file");
			//TODO tis label should be parametrized
			out.label = "Input sequences";
			result.add(out);
		}
		
		/* add the command line file to the result object */
		if( getCmdFile() != null ) {
			OutItem out = new OutItem(getCmdFile(),"system_file");
			//TODO this label should be parametrized
			out.label = "Command line";
			out.format = "cmdline";
			result.add(out);
		}
		
		if( success || existsLogFile()) { // <-- note: it is OR condition to force an exception if the job has been processed BUT the out file does not exists
			
			/* add at least the tcoffee log file */
			OutItem out = new OutItem(logfile, "system_file");
			//TODO tis label should be parametrized
			out.label = "Log file";
			result.add(out); 
			
			/* add all t-coffee result */
			result.addAll(parseResultFile(getLogFile()));
			
			/* add warnings */
			result.addWarnings( _warnings );
		}

		if( !success &&  existsErrFile()) { 
			
			/* add at least the tcoffee log file */
			OutItem out = new OutItem(getErrFile(), "error_file");
			//TODO tis label should be parametrized
			out.label = "Error file";
			result.add(out); 
			
		}
		
		/* add the t_coffee.ErrorReport file */
		File report;
		if( !success && (report=new File(ctxfolder, "t_coffee.ErrorReport")).exists() ) { 
			OutItem out = new OutItem(report, "error_file");
			out.label = "Error Report";
			result.add(out); 
			
		}
		
		OutItem html = result.getAlignmentHtml();
		return success && html != null && html.exists();
	}

	List<OutItem> parseResultFile(File file) {
		List<OutItem> result = new ArrayList<OutItem>();
		_warnings = new ArrayList<String>();
		
		try {
			String line;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			/* consume until output section is found */
			while ((line = reader.readLine()) != null && 
					!"OUTPUT RESULTS".equals(line) && 
					!"Looking For Sequence Templates:".equals(line)
			) { /* empty */ }

			/* parse the output items */
			while ((line = reader.readLine()) != null) {
				OutItem item = parseForResultItem(line);
				if (item != null) {
					result.add(item);
				}
				
				// check for warnings 
				String warn = parseForWarning(line);
				if( Utils.isNotEmpty(warn)) { 
					if( warn.startsWith("WARNING:")) { 
						warn = warn.substring(8);
					}
					_warnings.add(warn.trim());
				}
			}
			/* .. and return the list */
			return result;

		} catch (IOException e) {
			throw new QuickException(e, "Unable to parse result file: %s", file);
		}
	}

	OutItem parseForResultItem(String line) {
		Matcher matcher = RESULT_PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}

		String name = matcher.group(3).trim();
		String type = matcher.group(1).trim();
		
		/* handle special exception */
		if( name == null || name.contains("NOT PRODUCED")) {
			return null;
		}
		
		/* retrieve the file in the context path */
		String root = ctx.get("data.path");
		File file = new File( root, name );
		
		OutItem item = new OutItem(file,type);
		item.format = matcher.group(2).trim();

		return item;
	}
	
	String parseForWarning( String line ) { 
		Matcher matcher = WARNING_PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		
		return matcher.group(1).trim();
	}
	

	
}

package models;

import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("exec")
public class GenericCommand extends AbstractShellCommand {

	/** the application binary to be executed */
	@XStreamAsAttribute
	public Eval program;

	/** the command line argument to be used to execute the program */
	public CmdArgs args;
	
	/** The default constructor */
	public GenericCommand() {}
	
	public GenericCommand(String programWithArguments ) { 
		String val = programWithArguments.trim();
		int i = val.indexOf(' ');
		if( i == -1 ) { 
			this.program = new Eval(val);
		}
		else { 
			this.program = new Eval(val.substring(0,i));
			this.args = new CmdArgs( val.substring(i+1) );
		}
	}
	
	/** The copy constructor */
	public GenericCommand( GenericCommand that ) {
		super(that);
		this.program = Utils.copy(that.program);
		this.args = Utils.copy(that.args);
	}
	
	@Override
	public void init(CommandCtx ctx) {
		if( Utils.isEmpty(logfile) ) logfile = "stdout.log";		
		super.init(ctx);
	}
	
	@Override
	protected String onInitCommandLine(String cmdLine) {
		StringBuilder result = new StringBuilder();
		
		if( program != null ) { 
			// append the program to be executed 
			result.append(program.eval());
			
			// apped the program arguments 
			if( args != null ) { 
				result.append(" ") .append( args.toCmdLine() );
			}
			
		}
		else { 
			// otheriwse just an empty string 
			result.append("");
		}
		
		return result.toString();
	}
	
	
	@Override
	protected boolean done(boolean success) {

		/*
		 * add the stdout file to the result
		 */
		if( hasLogFile()) { // <-- note: it is OR condition to force an exception if the job has been processed BUT the out file does not exists
			
			/* add at least the tcoffee log file */
			OutItem out = new OutItem(getLogFile(), "stdout");
			out.label = "Program Output";
			out.aggregation = "system_file";
			result.add(out); 
			
		}

		/*
		 * add the stderr file to teh result if exists
		 */
		if( hasErrFile()) { 
			
			OutItem out = new OutItem(getErrFile(), "stderr");
			out.label = "Program Error";
			out.aggregation = "system_file";
			result.add(out); 
			
		}
		
		return success;
	}	
}

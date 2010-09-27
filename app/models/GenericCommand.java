package models;

import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("exec")
public class GenericCommand extends AbstractShellCommand {

	/** The command line to be executed */
	public Eval cmd;
	
	/** The default constructor */
	public GenericCommand() {}
	
	public GenericCommand(String cmd) {
		this.cmd = new Eval(cmd);
	}
	
	/** The copy constructor */
	public GenericCommand( GenericCommand obj ) {
		super(obj);
		this.cmd = Utils.copy(obj.cmd);
	}
	
	@Override
	protected String onInitCommandLine(String cmdLine) {
		return cmd != null ? cmd.eval() : "";
	}
	
	
	@Override
	protected boolean done(boolean success) {

		/*
		 * add the stdout file to the result
		 */
		if( existsLogFile()) { // <-- note: it is OR condition to force an exception if the job has been processed BUT the out file does not exists
			
			/* add at least the tcoffee log file */
			OutItem out = new OutItem(getLogFile(), "stdout");
			out.label = "Command stdout";
			result.add(out); 
			
		}

		/*
		 * add the stderr file to teh result if exists
		 */
		if( existsErrFile()) { 
			
			OutItem out = new OutItem(getErrFile(), "stderr");
			out.label = "Command stderr";
			result.add(out); 
			
		}
		
		return success;
	}	
}

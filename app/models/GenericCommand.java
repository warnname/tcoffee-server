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
	
}

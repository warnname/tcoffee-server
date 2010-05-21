package models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.libs.IO;
import plugins.AutoBean;
import util.Check;
import util.Utils;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import exception.CommandException;
import exception.QuickException;

@AutoBean
@XStreamAlias("ssh")
public class SshProxyCommand extends AbstractCommand<OutResult> {

	@XStreamImplicit()
	private List<AbstractShellCommand> _commands;

	@XStreamOmitField private AbstractShellCommand fTarget;
	@XStreamOmitField private Connection fConnection;
	
	public String hostname;

	public String username;
	
	public String password;
	
	
	public SshProxyCommand() {}

	public SshProxyCommand( AbstractShellCommand cmd ) {
		Check.notNull(cmd, "Argument 'cmd' cannot be null");
		_commands = new ArrayList<AbstractShellCommand>();
		_commands.add(cmd);
	} 
	
	public SshProxyCommand(SshProxyCommand that) {
		this._commands = Utils.copy(that._commands);
		this.hostname = that.hostname;
		this.username = that.username;
		this.password = that.password; 
	}
	
	
	@Override
	protected void init(CommandCtx ctx) {
		Check.notNull(_commands, "Target command cannot be null");
		Check.notNull(hostname, "Attribute 'hostname' cannot be empty");
		Check.notNull(username, "Attribute 'username' cannot be empty");
		
		fTarget = _commands != null && _commands.size()>0 ? _commands.get(0) : null;

		
		/* 
		 * initialize this context 
		 */
		super.init(ctx);

		
		/*
		 * initialize the target command 
		 */
		fTarget.init(ctx);

		/*
		 * connect ot remote host 
		 */
		
		boolean isAuthenticated = false;
		fConnection = new Connection(hostname);
		try {
			fConnection.connect();		
			isAuthenticated = fConnection.authenticateWithPassword(username, password);
		}
		catch( IOException e ) {
			throw new QuickException(e, "Unable to connect %s@%s", username, hostname);
		}
		
		if (isAuthenticated == false)
			throw new QuickException("Unable to authenticate %s@%s", username, hostname);
		
	}
	
	@Override
	protected boolean done(boolean success) {
		
		/*
		 * complete the target command
		 */
		success = fTarget.done(success);

		/* 
		 * the result of this command is the same as the target command 
		 */
		result = fTarget.result;

		/*
		 * close connection to server
		 */
		if( fConnection!=null ) { fConnection.close(); }
		
		return success;
	}
	
	@Override
	protected boolean run() throws CommandException {

		String cmdLine = fTarget.getCmdLine();
		try {
			Session sess = fConnection.openSession();
			try {
				sess.execCommand(cmdLine);

				saveStdOut(sess);
				
				/* Show exit status, if available (otherwise "null") */
				Integer result = sess.getExitStatus();
				Logger.debug("Returned exit code from remote command: %s", result);
				return result!=null && fTarget.validCode==result;
			}
			finally {
				/* Close this session */
				sess.close();
			}
		}
		catch( IOException e ) {
			throw new CommandException(e,"Unable to execute remote command '%s' on '%s' ", cmdLine, hostname);
		}
	
	}

	private void saveStdOut(Session sess) throws IOException {

		File logFile=fTarget.getLogFile();
		if( logFile==null ) { return; }

		InputStream stdout = new StreamGobbler(sess.getStdout());
		FileOutputStream out = new FileOutputStream(logFile);

		IO.write(stdout, out);

		out.close();
	}

}

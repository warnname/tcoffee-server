package models;

import java.io.Serializable;

import play.Logger;
import util.Utils;
import exception.CommandException;

/** 
 * Abstract template to implement a generic job to be processed 
 * 
 * @author Paolo Di Tommaso
 */
public abstract class AbstractCommand<T> implements Serializable {

	/** The gereric result available at the end of the execution of this command */
	protected T result;
	
	/** The command execution start time */
	protected long startTime;
	
	/** The command elapsed time to completion */
	protected long elapsedTime;

	/** The command execution context */
	protected CommandCtx ctx;
	
	private boolean fOK;
	
	/** The default constructor */
	protected AbstractCommand() {}
	
	/** The copy constructor */
	protected AbstractCommand(AbstractCommand<T> that) {
		this.result = Utils.copy(that.result);
		this.startTime = that.startTime;
		this.elapsedTime = that.elapsedTime;
		this.ctx = Utils.copy(that.ctx);
	}

	/**
	 * Initialize the command and inject a new {@link CommandCtx} empty instance  
	 */
	final public void init() {
		CommandCtx ctx = Service.current() != null ? new CommandCtx(Service.current().fCtx) : new CommandCtx();
		init(ctx);
	}
	
	/**
	 * Override this to contribute to the command context 
	 * 
	 * @param ctx 
	 */
	public void init(CommandCtx ctx) {
		this.ctx = ctx;
	}
	
	/**
	 * Fires the command execution 
	 * 
	 * @return <code>true</code> if the command executed without error, <code>false</code> otherwise
	 */
	public final boolean execute() throws CommandException {
		if(ctx==null) {
			Logger.warn("Context is null. It is required to invoke #init() method before execute the command!");
		}
		 
		startTime = System.currentTimeMillis();
		boolean result = false;
		try {
			result = run();
		}
		finally {
			elapsedTime = System.currentTimeMillis() - startTime;
		}
		
		return fOK = done(result);
		
	};

	/**
	 * Template method that subclass can override to handle special condition on command completion. 
	 * ()
	 * @param success the command result status 
	 * @return the final execution success flag, return <code>true</code> to signal a successful execution 
	 * or <code>false</code> otherwise
	 */
	protected boolean done(boolean success) {
		return success;
	}
	
	/**
	 * Override this to implement the command.
	 *  
	 * @return <code>true</code> if the command executed without error, <code>false</code> otherwise
	 */
	protected abstract boolean run() throws CommandException;

	/** 
	 * @return the result object available at the end of the execution of this command 
	 */
	public T getResult() { return result; }
	
	/** 
	 * @return <code>true</code> if a result object is available 
	 */
	public boolean hasResult() { return result != null; }

	/** the start time getter method */
	public long getStartTime() {
		return startTime;
	}
	
	/** Elapsed time getter method */
	public long getElapsedTime()  {
		return elapsedTime;
	}

	public CommandCtx getCtx() {
		return ctx;
	} 
	
	final public boolean isOK() {
		return fOK;
	}
}

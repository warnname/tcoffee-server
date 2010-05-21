package exception;

public class CommandException extends Exception {

	public CommandException() {
		super();
	}
	
	public CommandException(Throwable t) {
		super(t);
	}
	
	public CommandException(String message, Object... args) {
		super(String.format(message,args));
	}

	public CommandException(Throwable e, String message, Object... args) {
		super(String.format(message,args),e);
	}
	
}

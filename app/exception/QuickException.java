package exception;

public class QuickException extends RuntimeException {

	public QuickException() {
		super();
	}
	
	public QuickException(Throwable t) {
		super(t);
	}
	
	public QuickException(String message, Object... args) {
		super(String.format(message,args));
	}

	public QuickException(Throwable e, String message, Object... args) {
		super(String.format(message,args),e);
	}
	
}

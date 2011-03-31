package io.seq;

import exception.QuickException;

public class FormatParseException extends QuickException {

	public FormatParseException(String message, Object... args) {
		super(String.format(message,args));
	}

	public FormatParseException(Throwable e, String message, Object... args) {
		super(String.format(message,args),e);
	}	
}

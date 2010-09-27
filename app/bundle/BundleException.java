package bundle;

/**
 * 
 * Unchecked exception to manage bundle unexpected condiction 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class BundleException extends RuntimeException {

	public BundleException( Throwable e ) { 
		super(e);
	}
	
	public BundleException( Throwable e, String message, Object ... args ) { 
		super(String.format(message,args), e);
	}
	
	public BundleException( String message, Object ... args ) { 
		super(String.format(message,args));
	}
}

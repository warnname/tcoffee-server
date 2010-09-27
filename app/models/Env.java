package models;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("env") 
public class Env extends AnyAttributeElement  {

	/** The default constructor */
	public Env() { } 

	/** The copy constructor */
	public Env(Env that) {
		super(that);
	} 
	
	/** 
	 * Create an Env object with the specified variables pair
	 * 
	 * @param pairs an open array of variables values. Each item use the following format <code>name=value</code>
	 */
	public Env(String... pairs) {
		if( pairs == null ) return;
		
		for( String pair: pairs ) {
			int p = pair.indexOf("=");
			String name = null;
			String value = null;
			if( p != -1 ) {
				name = pair.substring(0,p);
				value = pair.substring(p+1);
			}
			else {
				name = pair;
			}
			
			put(name,value);
		}
	}
	
	
}

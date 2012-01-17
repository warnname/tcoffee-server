package models;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import util.Utils;

/**
 * Models the a generic command context 
 * 
 * @author Paolo Di Tommaso
 * 
 *
 */
public class ContextHolder implements Serializable {

	/** The action input */
	Input input;
	
	/** the action produced result */
	OutResult result;
	
	/** The params in this context */
	Map<String,Object> map ;

	/**
	 * Creates an empty context 
	 */
	public ContextHolder() {
		this.map = new HashMap<String, Object>();
		this.input = new Input();
		this.result = new OutResult();
	}
	
	/**
	 * The copy constructor 
	 */
	public ContextHolder( ContextHolder that ) {
		this.map = new HashMap(that.map);
		this.input = new Input(that.input);
		this.result = new OutResult(that.result);
	}
	
	/**
	 * Creates a context object with all the values in the specified map 
	 * and an empty {@link Input} and {@link OutResult} objects 
	 */
	public ContextHolder( Map<String, Object> map ) {
		this.input = new Input();
		this.result = new OutResult();
		
		this.map = new HashMap<String,Object>(map);
	}

	/**
	 * Creates a context object with all the values in the specified map 
	 * and an empty {@link Input} and {@link OutResult} objects 
	 */
	public ContextHolder( Hashtable hash ) { 
		this.input = new Input();
		this.result = new OutResult();

		map = new HashMap<String,Object>();
		Enumeration _enum = hash.keys();
		while( _enum.hasMoreElements() ) { 
			Object key = _enum.nextElement();
			map.put( key.toString(), hash.get(key));
		}
	}
	
	
	public ContextHolder( String ... pairs ) { 
		this.map = new HashMap<String, Object>( Utils.asMap(pairs));
	}
	
	/**
	 * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
	 */
	public <T> T get( String key ) {
		return (T) map.get(key);
	} 
	
	/**
	 * Store a key-value pair in this context. 
	 * 
	 * @param key the unique key to access the value
	 * @param value the associated value 
	 * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 */
	public Object put( String key, Object value ) {
		return map.put(key, value);
	}

	/**
	 * @return the context map hold by this context
	 */
	public Map<String,Object> getMap() {
		return map;
	} 
	
	/**
	 * @return the {@link Input} instance hold by this context
	 */
	public Input getInput() {
		return input;
	} 
	
	/**
	 * @return the {@link OutResult} instance hold by this context
	 */
	public OutResult getResult() {
		return result;
	}

}

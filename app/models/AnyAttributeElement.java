package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public abstract class AnyAttributeElement implements Serializable {

	@XStreamOmitField
	Map<String,Eval> values;
	
	/* use a separate list for arg names to mantains the insertion order */
	@XStreamOmitField
	List<String> names;
	
	public AnyAttributeElement() {
		values = new HashMap<String, Eval>();
		names = new ArrayList<String>();
	}
	
	/** The copy constructor */
	public AnyAttributeElement(AnyAttributeElement that) {
		values = Utils.copy(that.values);
		names = Utils.copy(that.names);
	}
	
	public void put( String name, String value ) {
		put(name, new Eval(value));
	}
	
	protected void put( String name, Eval value ) {
		values.put(name, value);
		if( !names.contains(name) ) {
			names.add(name);
		}
	}
	
	public void put( String pair ) {
		if( Utils.isEmpty(pair) ) return;
		
		String[] items = pair.split("=");
		put( items[0], items.length>1?items[1]:null );
	}
	
	public void putAll( String... pairs ) {
		if( pairs == null ) return;
		for( String pair : pairs ) {
			put(pair);
		}
	}
	
	
	public void remove( String name ) {
		values.remove(name);
		while( names.contains(name) ) {
			names.remove(name);
		}
	}
	
	public String get( String name ) {
		Eval e = values.get(name);
		return e != null ? e.eval() : null; 
	}
	
	public List<String> getNames() {
		return names != null ? new ArrayList(names) : Collections.<String>emptyList();
	}	
	
	public boolean has( String key ) {
		return values != null && values.containsKey(key);
	}
	
	public int size() {
		return values != null ? values.size() : 0;
	}
}

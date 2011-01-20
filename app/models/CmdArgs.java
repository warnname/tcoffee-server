package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import plugins.AutoBean;
import util.Check;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@AutoBean 
@XStreamAlias("args")
public class CmdArgs {

	static final String PREFIX = "-";

	List<Arg> items = new ArrayList<Arg>();
	
	public CmdArgs() {}
	
	public CmdArgs( String content ) {
		parse(content);
	}
	
	public CmdArgs(CmdArgs that) {
		items = Utils.copy(that.items); 
	}

	
	public void parse( String content ) {
		if( content==null || Utils.isEmpty(content=content.trim())) return;
		
		content = content.trim();
		if( content.startsWith(PREFIX)) {
			content = content.substring(PREFIX.length());
		}
		
		String[] items = content.split(PREFIX);
		for( String item : items ) {
			put(item);
		}

	}
	
	public String toRawString() {
		if( items==null ) return null;
		
		StringBuilder result = new StringBuilder();
		int i=0;
		for( Arg arg : items ) {
			if( i++>0  ) { result.append(" "); } 
			result.append(arg.toRawString());
		}
		
		return result.toString();
	}

	
	/**
	 * Add a value without replacing the current content if exists
	 * 
	 * @param name
	 * @param value
	 */
	public void add( String name, String value ) { 
		if( items == null ) {
			items = new ArrayList<Arg>();
		}

		int p = indexOf(name);
		if( p != -1 ) { 
			Arg arg = items.get(p);
			if( arg != null && Utils.isNotEmpty(arg.value) ) {
				value = arg.value + " " + value;
			}
			
			items.remove(p);
			items.add(p, new Arg(name,value));
		}
		else { 
			items.add(new Arg(name,value));
		}

	}	
	
	public void put( String name, String value ) {
		if( items == null ) {
			items = new ArrayList<Arg>();
		}
		items.add( new Arg(name,value) );
	}
	
	public void put( String pair ) {
		if( pair==null || Utils.isEmpty(pair=pair.trim()) ) { return; }
		
		// pair separator is equals char (=)
		int p = pair.indexOf('=');
		if( p==-1 ) { 
			// try to fallback on blank char ' ' 
			p = pair.indexOf(' ');
		}
		
		String name = null;
		String value = null;
		if( p != -1 ) {
			name = pair.substring(0,p);
			value = pair.substring(p+1);
		}
		else {
			name = pair;
		}
		
		if( value == null && p!=-1) {
			value = "";
		}
		put(name,value);
	}
	
	public void putAll( String... pairs ) {
		if( pairs == null ) return;
		for( String pair : pairs ) {
			put(pair);
		}
	}
	
	
	public void putAll( CmdArgs args ) {
		if( args == null || args.items == null ) return;
		
		if( items == null ) {
			items = new ArrayList<Arg>();
		}
		
		items.addAll(args.items);
	}	

	public void remove( String name ) {
		int i;
		while ( (i=indexOf(name)) != -1 ) {
			items.remove(i);
		} 
	}
	
	public int indexOf( String name ) {
		Check.notNull(name, "Argument 'name' cannot be null");
		if( items == null ) return -1;
		
		for( int i=0; i<items.size(); i++ ) {
			if( name.equals(items.get(i).name) ) {
				return i;
			}
		}
		
		return -1;
	}
	
	public boolean contains( String name ) {
		return indexOf(name) != -1;
	}
	
	
	public String get( String  name ) {
		int p = indexOf(name);
		Arg arg = (p!=-1) ? items.get(p) : null;
		return (arg != null) ? arg.getVal() : null; 
	}
	
	public List<String> getAsList( String key ) { 

		String vals = get(key);
		
		if( Utils.isEmpty(vals) ) { 
			// empty result
			return Collections.emptyList();
		}
		
		List<String> result = Arrays.asList( vals.split(" ") );
		return result;
	}

	public String at( int index ) {
		if( items == null ) return null;
	
		Arg arg = items.get(index);
		return arg != null ? arg.name : null;
	} 

	
	public int size() {
		return (items!=null) ? items.size() : 0;
	}	
	
	/** 
	 * @return a command line string following the format -<name>=<value>
	 */
	public String toCmdLine() {
		if( items == null ) return null;
		
		StringBuilder result = new StringBuilder();
		
		for( Arg arg : items ) {
			String a = arg.toCmdLine();
			if( Utils.isNotEmpty(a) ) {
				if( result.length()>0 ) { result.append(" "); }
				result.append(a);
			}

		}
		return result.toString();
	}

}

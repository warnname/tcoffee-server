package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plugins.AutoBean;
import util.Check;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@AutoBean 
@XStreamAlias("args")
public class CmdArgs {

	/** The string value use to evaluate the command line. In otehr words it can contain unevaluated variables */
	String rawCmdLine;

	/** The list of arguments */
	List<Arg> fItems;
	
	public List<Arg> getItems() { 
		if( fItems == null ) { 
			fItems = new ArrayList<Arg>();
			if( Utils.isNotEmpty(rawCmdLine) ) { 
				parse(rawCmdLine);
			}
		}
		return fItems;
	}
	
	/** 
	 * Initialize the class to an empty list of command line arguments 
	 */
	public CmdArgs() {}
	
	/**
	 * Create an instance using the specified string as command line arguments. 
	 * It can contains variables that will be evaluated by the {@link #get(String)} method
	 *  
	 * @param cmdLine an arguments command line like <code>-mode=expresso -out=${variable}
	 */
	public CmdArgs( String cmdLine ) {
		this.rawCmdLine = cmdLine;
	}
	
	/**
	 * Create an instance coping the attributes from <code>that</code> instance
	 * 
	 * @param that
	 */
	public CmdArgs(CmdArgs that) {
		rawCmdLine = that.rawCmdLine;
		fItems = Utils.copy(that.fItems); 
	}

	
	void parse( String cmdLine ) {
		if( cmdLine==null || Utils.isEmpty(cmdLine=cmdLine.trim())) {
			return;
		}
		
		/*
		 * If the command line is composed only by variables 
		 * jsut evaluate it and than parse as a command line  
		 */
		Pattern VARS = Pattern.compile("\\s*(\\$\\{[^\\}]+\\}\\s*)+"); 
		if( VARS.matcher(cmdLine).matches() ) { 
			Eval template = new Eval(cmdLine);
			cmdLine = template.eval();
		}
		
		
		List<String> allItems = cmdLineTokenizer(cmdLine);
		for( String item : allItems ) {
			if( Utils.isEmpty(item)) { 
				continue;
			}
			
			put(item.trim());
		}

	}



	/**
	 * Tokenize the command line in its single parts. Command line options have to start with one or more '-' characters 
	 * and value must be preceed by the '=' or blank character. For example 
	 * <pre>
	 * t_coffee input.fa -flag -mode=regular -output ascii html pdf 
	 * </pre> 
	 * 
	 * @param cmdLine
	 * @return
	 */
	static List<String> cmdLineTokenizer( String cmdLine ) { 

		Pattern OPTION_SEPARATOR = Pattern.compile("[ \\t\\n\\x0B\\f\\r]-");
		Pattern BLANK_SEPARATOR = Pattern.compile("[ \\t\\n\\x0B\\f\\r]");
		
		List<String> result = new ArrayList();
		
		Pattern separator;
		while( Utils.isNotEmpty(cmdLine)) { 
			String item;
			Matcher matcher;
			cmdLine = cmdLine.trim();
			
			
			separator = cmdLine.startsWith("-")  ? OPTION_SEPARATOR : BLANK_SEPARATOR;

			/* lookahead for the next option separator */
			if( (matcher=separator.matcher(cmdLine)).find() ) { 
				item = cmdLine.substring(0,matcher.start());
				cmdLine = cmdLine.substring(matcher.start()+1);
			}
			else { 
				item = cmdLine;
				cmdLine = null;
			}
			result.add(item);
		}
		
		return result;
		
	}
	
	
	public String toRawString() {
		
		StringBuilder result = new StringBuilder();
		int i=0;
		List<Arg> items = getItems();
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

		int p = indexOf(name);
		if( p != -1 ) { 
			Arg arg = getItems().get(p);
			if( arg != null && Utils.isNotEmpty(arg.value) ) {
				value = arg.value + " " + value;
			}
			
			getItems().remove(p);
			getItems().add(p, new Arg(name,value));
		}
		else { 
			getItems().add(new Arg(name,value));
		}

	}	
	
	public Arg put( String name, String value ) {
		
		Arg result = new Arg(name,value) ;
		getItems().add(result);
		return result;
	}
	
	Arg put( String pair ) {
		if( pair==null || Utils.isEmpty(pair=pair.trim()) ) { return null; }
		
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
		
		/* check for param prefix */
		String prefix="";
		while( name.startsWith("-") ) { 
			name = name.substring(1);
			prefix += "-";
		}
		
		/* create the result object */
		Arg result = put(name,value);
		result.prefix = prefix;

		return result;
	}

	/**
	 * Remove the 'argument' with the specified name 
	 * 
	 * @param name the argument to be removed 
	 */
	public void remove( String name ) {
		int i;
		while ( (i=indexOf(name)) != -1 ) {
			getItems().remove(i);
		} 
	}
	
	/**
	 * 
	 * @param name a command line argument name
	 * @return 
	 */
	public int indexOf( String name ) {
		Check.notNull(name, "Argument 'name' cannot be null");
		
		for( int i=0; i < getItems().size(); i++ ) {
			if( name.equals(getItems().get(i).name) ) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Check if an 'argument' with the given name exists in the command line 
	 * 
	 * @param name the 'argument' name
	 * @return <true>when exists, <code>false</code> otherwise
	 */
	public boolean contains( String name ) {
		return indexOf(name) != -1;
	}
	
	/**
	 * Retrieve the value for the 'argument' with the specified name
	 * @param name
	 * @return the argument value as string of <code>null</code> if not exist 
	 */
	public String get( String  name ) {
		int p = indexOf(name);
		Arg arg = (p!=-1) ? getItems().get(p) : null;
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
		Arg arg = getItems().get(index);
		return arg != null ? arg.name : null;
	} 

	
	public int size() {
		return getItems().size();
	}	
	
	/** 
	 * @return a command line string following the format -<name>=<value>
	 */
	public String toCmdLine() {
		
		StringBuilder result = new StringBuilder();
		
		List<Arg> items = getItems();
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

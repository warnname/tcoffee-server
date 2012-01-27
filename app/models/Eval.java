package models;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import play.Logger;
import plugins.AutoBean;
import util.Check;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;


@AutoBean
@XStreamAlias("eval")
public class Eval implements Serializable {
	
	@XStreamOmitField public String raw;
	
	@XStreamOmitField public List<String> vars;
	
	public Eval( String value ) {
		this.raw = value;
		this.vars = prefetch(value);
	}

	public boolean isStatic() {
		return vars.size()==0;
	}
	
	@Override
	public String toString() {
		return eval();
	}

	public String eval() {
		final Service service = Service.current();
		return eval(service.getContext().map);
	}
	
	public String eval( final Map<String,Object> ctx ) {
		Check.notNull(ctx,"Variable context cannot be null");
		if( raw == null ) {
			return null;
		}

		Utils.MapValue<String, Object> mapper = new Utils.MapValue<String,Object>() {
			
			public Object get(String varname) {
				Object obj = ctx.get(varname);
				if( obj == null ) {
					Logger.debug("Cannot resolve context variable '%s'", varname);
					// if null just .. NULL
					return null;
				}
				
				// print out just the element as string
				return elem(obj);

			}};	
		
		String result = Utils.replaceVars(raw, mapper);
		return result != null ? result.trim() : null;		
	}

	Object elem(Object obj) {
		
		if( obj instanceof File ) {
			return ((File)obj) .getName();
		}
		
		return ( obj != null ? obj.toString() : null);
	}
	
	/**
	 * Extract all variables names in the specified string 
	 * @param str a string that can contain variables with the syntax ${var-name}
	 * @return the list of variable names or an empty list if no var exists in <code>str</code>
	 */
	List<String> prefetch( String str ) {
		if( Utils.isEmpty(str) || !str.contains("$") ) { return Collections.emptyList(); }
		
		final List<String> result = new ArrayList<String>();
		Utils.replaceVars(str, new Utils.MapValue<String,Object>() {

			public Object get(String key) {
				result.add(key);
				return null;
			}
		});
		
		return result;
	}
}

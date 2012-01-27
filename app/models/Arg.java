package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import plugins.AutoBean;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@AutoBean
@XStreamAlias("arg")
public class Arg implements Serializable {

	public String name;

	public String value;
	
	/* default argument separator */
	public String prefix = "-";

	public String separator = "="; 

	public Arg() {}
	
	public Arg(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public Arg(Arg that) {
		this.name = that.name;
		this.value = that.value;
	}
	
	public String getVal() {
		List<String> result = getAll();
		return result.size()>0 ? result.get(0) : null;
		
	} 
	
	public List<String> getAll() {
		List<String> result = new ArrayList<String>();
		
		if( Utils.isEmpty(value)) {
			return result;
		}
		
		Eval eval = new Eval(value);
		if( eval.isStatic() ) {
			result.add(value);
			return result;
		}
		
		Service service = Service.current();
		String firstVarName = eval.vars.get(0);
		Map<String,Object> ctx = service.getContext().map;
		Object item = ctx.get(firstVarName);
		if( eval.vars.size() == 1 && item instanceof List ) {
			
			for( Object i : (List)item ) {
				String x = resolve(eval,i);
				if( Utils.isNotEmpty(x) ) {
					result.add(x);
				}
			}
		}
		else {
			String x = resolve(eval,ctx);
			if( Utils.isNotEmpty(x) ) {
				result.add(x);
			}
		}
		
		return result;
	}

	public String toCmdLine() {
		/* 
		 * if no value as been specified just return argument as a flag
		 */
		if( Utils.isEmpty(value) ) {
			return prefix + name;
		}
		
		/*
		 * check 
		 */
		StringBuilder result = new StringBuilder();
		for( String v : getAll() ) {
			if( result.length()>0 ) result.append(" ");
			result
				.append(prefix) 
				.append(name)
				.append(separator)
				.append(v);
		}
		
		return result.toString();
	}
	
	
	String resolve( Eval eval, Object ctx ) {
		Map<String,Object> map = new HashMap<String,Object>(1);
		map.put( eval.vars.get(0), ctx);
		
		return resolve(eval,map);
	}
	
	String resolve( Eval eval, Map<String,Object> ctx  ) {
		
		String result = eval.eval(ctx);
		return result!=null ? result.trim() : null;
	}

	public String toRawString() {
		String result = prefix + name;
		if( Utils.isNotEmpty(value) ) {
			result += "=" + value;
		} 
		
		return result;
	}

		
		

	
}

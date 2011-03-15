package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import util.Utils;

public class CommandCtx implements Serializable {

	Map<String,Object> ctx ;

	public CommandCtx() {
		ctx = new HashMap<String, Object>();
	}
	
	public CommandCtx( Map<String, Object> map ) { 
		ctx = new HashMap<String,Object>(map);
	}
	
	public CommandCtx( Hashtable hash ) { 
		ctx = new HashMap<String,Object>();
		Enumeration _enum = hash.keys();
		while( _enum.hasMoreElements() ) { 
			Object key = _enum.nextElement();
			ctx.put( key.toString(), hash.get(key));
		}
	}
	
	
	public CommandCtx( String ... pairs ) { 
		ctx = new HashMap<String, Object>( Utils.asMap(pairs));
	}
	
	public CommandCtx(CommandCtx that) {
		ctx = Utils.copy(that.ctx);
	} 
	
	public void clear() {
		ctx.clear();
	}

	public void put(String key, Object value) {
		ctx.put(key, value);
	} 
	
	public void remove(String key) {
		ctx.remove(key);
	}
	
	public boolean hasKey(String key) {
		return ctx.containsKey(key);
	} 
	
	public <T> T get( String key ) {
		return (T) ctx.get(key);
	} 
	
	public List<String> getKeys() {
		return new ArrayList<String>(ctx.keySet());
	}

}

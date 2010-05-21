package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Utils;

public class CommandCtx implements Serializable {

	private Map<String,Object> ctx ;

	public CommandCtx() {
		ctx = new HashMap<String, Object>();
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

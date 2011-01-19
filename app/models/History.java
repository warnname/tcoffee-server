package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import play.Logger;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import util.Check;
import util.Utils;
import exception.QuickException;

public class History {
	
	private static final String COOKIE_PREFIX = "RID_";
	

	/** 
	 * Comparator used to sort the history list by descendent begin time 
	 * i.e. from the most recent the older  
	 */
	public static class DescBeginTimeSort implements java.util.Comparator<History> {

		public static DescBeginTimeSort INSTANCE = new DescBeginTimeSort(); 
		
		public int compare(History obj1, History obj2) {
			long t1 = obj1.begin != null ? obj1.begin.getTime() : 0;
			long t2 = obj2.begin != null ? obj2.begin.getTime() : 0;
			return (int)(t2-t1);
		}}
	
	/** Character used to separate values within the cookie */
	private static final String COOKIE_SEP = "|";
	
	/** the alignment request-id */
	private String rid;
	
	/** the alignment request date-time */
	private Date begin;
	
	/** the history entry empiration date-time */
	private Date expire;

	/* the alignment job duration in millis */
	private Long duration;
	
	/* the label used to show this entry in the history list */
	private String label;
	
	/** name of the bundle that produced this history item */
	private String bundle;
	
	/* the current job execution status */
	private String status;
	
	/**
	 * Creates the history object instance setting the <code>rid</rid>, {@link #begin} and the {@link #end} attributes
	 */
	public History (String rid) { 
		Check.notEmpty(rid, "Argument rid cannot be empty for History class");
		this.rid = rid;
		this.begin = new Date();
		this.expire = new Date( begin.getTime() + (AppProps.instance().getDataCacheDuration() * 1000) );
		
	}
	
	public History(String rid, String bundle, String mode, Date begin, Date expire, Long duration, String status) { 
		Check.notEmpty(rid, "Argument rid cannot be empty for History class");
		this.rid = rid;
		this.bundle = bundle;
		this.label = mode;
		this.begin = begin;
		this.expire = expire;
		this.duration = duration;
		this.status = status;
		
	}
	

	public History(Cookie cookie) {
		Check.notNull(cookie, "Argument cookie cannot be null");
		Check.notNull(cookie.name, "Cookie name cannot be null");

		rid=null;
		int p = cookie.name.indexOf(COOKIE_PREFIX);
		if( p==0 ) { 
			rid = cookie.name.substring(COOKIE_PREFIX.length());
		}
		else { 
			throw new QuickException("Invalid History cookie name: '%s'", cookie.name);
		}

		String[] item = cookie.value.split("\\" + COOKIE_SEP);
		if( item.length>0 ) {
			label = item[0];
		}
		
		if( item.length>1 ) { 
			begin = safeDate(item[1]);
		}
		
		if( item.length>2 ) { 
			expire = safeDate(item[2]);
		}

		if( item.length>3 ) { 
			bundle = item[3];
		}
		else { 
			// default case
			bundle = "tcoffee";
		}
		
		/* try to find out the result file */
		Repo ctx = new Repo(rid, false);
		Status state = ctx.getStatus();
		if( state.isDone() ) {
			OutResult out = ctx.getResult();
			duration = out.elapsedTime;
		}
		status = state.toString();
	}

	Long safeLong( String value ) { 
		if( Utils.isEmpty(value)) { return null; }
		try { 
			return Long.parseLong(value);
		}
		catch( NumberFormatException e ) { 
			Logger.warn("Invalid long value: '%s'", value);
			return null;
		}
 	}
	
	Date safeDate( String value ) { 
		Long l = safeLong(value);
		return l != null ? new Date(l) : null;
	}
	
	public String getRid() { 
		return rid;
	}
	
	public String getStatus() { 
		return status;
	}
	
	
	public String getBegin() {
		return Utils.asString(begin);
	}
	
	Date getBeginDate() { 
		return begin;
	}
	
	public String getExpire() { 
		return Utils.asString(expire);
	}

	Date getExpireDate() { 
		return expire;
	}
	
	public String getDuration() { 
		if( duration == null ) { return "--"; }
		
		return Utils.asDuration(duration);
	}
	
	
	public String getLabel() { 
		return Utils.asString(label);
	}
	
	public void setLabel(String mode) {
		this.label = mode;
	}
	
	public String getBundle() { 
		return bundle;
	}
	
	public void setBundle( String value ) { 
		this.bundle = value;
	}
	
	/**
	 * Serialize this {@link History} instance to an equivalent string value
	 */
	@Override
	public String toString() { 
		return Utils.dump(this, "rid", "status", "begin", "end");
	}
	
	/**
	 * Save the current {@link History} instance as a cookie
	 * 
	 */
	public void save() { 
		Cookie cookie = toCookie();
		Response.current().cookies.put(cookie.name, cookie);
	}
	
	public static History find( String rid ) { 
		for( Entry<String,Cookie> entry : Request.current().cookies.entrySet() ) { 
			String name = entry.getKey() != null ? entry.getKey() : "none";
			if( name.startsWith(COOKIE_PREFIX) ) { 
				return new History(entry.getValue());
			}
		}
		
		return null;
	}
	
	
	String toValue() { 
		StringBuilder result = new StringBuilder();

		if( label != null ) { 
			result.append(label);
		}
		
		result.append(COOKIE_SEP);
		if( begin != null ) { 
			result.append(begin.getTime());
		}
		
		result.append(COOKIE_SEP);
		if( expire != null ) { 
			result.append(expire.getTime());
		}

		result.append(COOKIE_SEP);
		if( bundle != null ) { 
			result.append(bundle);
		}
		
		return result.toString();
		
	}
	
	Cookie toCookie() { 
		Cookie cookie = new Cookie();
		cookie.name = COOKIE_PREFIX + rid;
		cookie.value = this.toValue();
		
		long t1 = begin.getTime();
		long t2 = expire.getTime();
		cookie.maxAge = (int)(t2-t1)/1000;
		
		return cookie;
	}
	
	public static List<History> findAll() {
		List<History> result = new ArrayList<History>();
		for( Entry<String,Cookie> entry : Request.current().cookies.entrySet() ) { 
			Cookie cookie = entry.getValue();
			String name = cookie != null && cookie.name != null ? cookie.name : "none";
			if( name.startsWith(COOKIE_PREFIX)) {
				result.add(new History(cookie));
			}
		}
		
		return result;
	}

	public static void deleteAll() {

		for( Entry<String,Cookie> entry : Request.current().cookies.entrySet() ) { 
			Cookie cookie = entry.getValue();
			String name = cookie != null && cookie.name != null ? cookie.name : "none";
			if( name.startsWith(COOKIE_PREFIX)) {
				cookie.maxAge = -1;
				Response.current().cookies.put(cookie.name, cookie);
			}
		}
		
	}
	

}

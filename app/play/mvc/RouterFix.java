package play.mvc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jregex.Matcher;
import jregex.Pattern;
import play.exceptions.NoRouteFoundException;
import play.exceptions.UnexpectedException;
import play.utils.Default;
import util.Utils;

/** 
 * Fix a problem in the priority of router arguments. 
 * <p>
 * See https://groups.google.com/d/topic/play-framework/9RJpIPhu1E8/discussion
 * 
 * @author Paolo Di Tommaso
 *
 */
public class RouterFix extends Router {


	/**
	 * Use as replecement of @{Some.action().add('arg',value)} like ${Util.reverse('Some.action',['arg':value])}
	 * 
	 * @param action
 	 * @param args
	 * @return
	 */
	   public static ActionDefinition reverse(String action, Map<String, Object> args) {
	        if (action.startsWith("controllers.")) {
	            action = action.substring(12);
	        }
	        Map<String, Object> argsbackup = new HashMap<String, Object>(args);
	        // Add routeArgs
	        if (Scope.RouteArgs.current() != null) {
	        	for( String key : Scope.RouteArgs.current().data.keySet() ) { 
	        		if( args.containsKey(key)) { continue; } 
		            args.put(key, Scope.RouteArgs.current().data.get(key));
	        	}
	        }
	        for (Route route : routes) {
	        	final Pattern _routeActionPattern = Utils.getField(route, "actionPattern");
	        	final List<String> _routeActionArgs = Utils.getField(route, "actionArgs");
	        	final List _routeArgs = Utils.getField(route, "args");
	        	final Map<String, String> _routeStaticArgs = Utils.getField(route, "staticArgs");
	        	final String _routePath = Utils.getField(route, "path");
	        	final String _routeHost = Utils.getField(route, "host");
	        	
	        	
	            if (_routeActionPattern != null) {
	                Matcher matcher = _routeActionPattern.matcher(action);
	                if (matcher.matches()) {
	                    for (String group : _routeActionArgs) {
	                        String v = matcher.group(group);
	                        if (v == null) {
	                            continue;
	                        }
	                        args.put(group, v.toLowerCase());
	                    }
	                    List<String> inPathArgs = new ArrayList<String>(16);
	                    boolean allRequiredArgsAreHere = true;
	                    // les noms de parametres matchent ils ?
	                    for (int i=0, c=_routeArgs.size(); i<c; i++ ) {
	                    	Object arg = _routeArgs.get(0);
	                        String _argName = Utils.getField(arg, "name");
	                        Pattern _argConstraint = Utils.getField(arg, "constraint" );
	                        
	                        
	                    	inPathArgs.add(_argName);
	                        Object value = args.get(_argName);
	                        if (value == null) {
	                            allRequiredArgsAreHere = false;
	                            break;
	                        } else {
	                            if (value instanceof List<?>) {
	                                @SuppressWarnings("unchecked")
	                                List<Object> l = (List<Object>) value;
	                                value = l.get(0);
	                            }
	                            if (!value.toString().startsWith(":") && !_argConstraint.matches(value.toString())) {
	                                allRequiredArgsAreHere = false;
	                                break;
	                            }
	                        }
	                    }
	                    // les parametres codes en dur dans la route matchent-ils ?
	                    for (String staticKey : _routeStaticArgs.keySet()) {
	                        if (staticKey.equals("format")) {
	                            if (!Http.Request.current().format.equals(_routeStaticArgs.get("format"))) {
	                                allRequiredArgsAreHere = false;
	                                break;
	                            }
	                            continue; // format is a special key
	                        }
	                        if (!args.containsKey(staticKey) || (args.get(staticKey) == null)
	                                        || !args.get(staticKey).toString().equals(_routeStaticArgs.get(staticKey))) {
	                            allRequiredArgsAreHere = false;
	                            break;
	                        }
	                    }
	                    if (allRequiredArgsAreHere) {
	                        StringBuilder queryString = new StringBuilder();
	                        String path = _routePath;
	                        String host = _routeHost;
	                        if (path.endsWith("/?")) {
	                            path = path.substring(0, path.length() - 2);
	                        }
	                        for (Map.Entry<String, Object> entry : args.entrySet()) {
	                            String key = entry.getKey();
	                            Object value = entry.getValue();
	                            if (inPathArgs.contains(key) && value != null) {
	                                if (List.class.isAssignableFrom(value.getClass())) {
	                                    @SuppressWarnings("unchecked")
	                                    List<Object> vals = (List<Object>) value;
	                                    try {
	                                        path = path.replaceAll("\\{(<[^>]+>)?" + key + "\\}", URLEncoder.encode(vals.get(0).toString().replace("$", "\\$"), "utf-8"));
	                                    } catch(UnsupportedEncodingException e) {
	                                        throw new UnexpectedException(e);
	                                    }
	                                } else {
	                                    try {
	                                        path = path.replaceAll("\\{(<[^>]+>)?" + key + "\\}", URLEncoder.encode(value.toString().replace("$", "\\$"), "utf-8").replace("%3A", ":").replace("%40", "@"));
	                                        host = host.replaceAll("\\{(<[^>]+>)?" + key + "\\}", URLEncoder.encode(value.toString().replace("$", "\\$"), "utf-8").replace("%3A", ":").replace("%40", "@"));
	                                    } catch(UnsupportedEncodingException e) {
	                                        throw new UnexpectedException(e);
	                                    }
	                                }
	                            } else if (_routeStaticArgs.containsKey(key)) {
	                                // Do nothing -> The key is static
	                            } else if (Scope.RouteArgs.current().data.containsKey(key)) {
	                                // Do nothing -> The key is provided in RouteArgs and not used (see #447)
	                            } else if (value != null) {
	                                if (List.class.isAssignableFrom(value.getClass())) {
	                                    @SuppressWarnings("unchecked")
	                                    List<Object> vals = (List<Object>) value;
	                                    for (Object object : vals) {
	                                        try {
	                                            queryString.append(URLEncoder.encode(key, "utf-8"));
	                                            queryString.append("=");
	                                            if (object.toString().startsWith(":")) {
	                                                queryString.append(object.toString());
	                                            } else {
	                                                queryString.append(URLEncoder.encode(object.toString() + "", "utf-8"));
	                                            }
	                                            queryString.append("&");
	                                        } catch (UnsupportedEncodingException ex) {
	                                        }
	                                    }
	                                } else if (value.getClass().equals(Default.class)) {
	                                    // Skip defaults in queryString
	                                } else {
	                                    try {
	                                        queryString.append(URLEncoder.encode(key, "utf-8"));
	                                        queryString.append("=");
	                                        if (value.toString().startsWith(":")) {
	                                            queryString.append(value.toString());
	                                        } else {
	                                            queryString.append(URLEncoder.encode(value.toString() + "", "utf-8"));
	                                        }
	                                        queryString.append("&");
	                                    } catch (UnsupportedEncodingException ex) {
	                                    }
	                                }
	                            }
	                        }
	                        String qs = queryString.toString();
	                        if (qs.endsWith("&")) {
	                            qs = qs.substring(0, qs.length() - 1);
	                        }
	                        ActionDefinition actionDefinition = new ActionDefinition();
	                        actionDefinition.url = qs.length() == 0 ? path : path + "?" + qs;
	                        actionDefinition.method = route.method == null || route.method.equals("*") ? "GET" : route.method.toUpperCase();
	                        actionDefinition.star = "*".equals(route.method);
	                        actionDefinition.action = action;
	                        actionDefinition.args = argsbackup;
	                        actionDefinition.host = host;
	                        return actionDefinition;
	                    }
	                }
	            }
	        }
	        throw new NoRouteFoundException(action, args);
	    }
	   
	   /**
	    * Just a shortcut for {@link #reverse(String, Map)} using an open-array of key-value pair in the format <code>key=value</code>
	    * 
	    * @param action
	    * @param args
	    * @return
	    */
	   public static ActionDefinition reverse( String action, String ... args ) { 
		  Map<String, Object> map = new HashMap<String, Object>( args==null ? 0 : args.length );
		  
		  for( int i=0; args!=null && i<args.length; i++ ) { 
			  int p = args[i] != null ? args[i].indexOf('=') : -1;
			  if( p!=-1 ) { 
				  String key = args[i].substring(0,p).trim();
				  String value = args[i].substring(p+1).trim();
				  map.put(key, value);
			  }
		  }
		  
		  return reverse(action, map);
	   }
	
}

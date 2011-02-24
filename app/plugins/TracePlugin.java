package plugins;

import java.util.Hashtable;

import org.apache.log4j.MDC;

import play.PlayPlugin;
import play.mvc.Http.Request;

/**
 * Append in the Log4j diagnostic context some information about the request 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class TracePlugin extends PlayPlugin {


	/** 
	 * The parameters added in the MDC context could be referenced in the log4j.properties 
	 * configuration file, using the syntax %X{param}. 
	 * <p>
	 * For example: 
	 * <code>
	 * log4j.appender.console.layout.ConversionPattern=%d{ISO8601} %-5p %X{req.ip} ~ %m - %X{req.method} %X{req.url} %n
	 * </code>
	 * 
	 */
    public void beforeInvocation() {
    	Request req = Request.current();
    	if( req == null ) { return; /* nothing to do */ }
    	
    	MDC.put("req.ip", req.remoteAddress);
    	MDC.put("req.method", req.method);
    	MDC.put("req.url", req.url);
    	MDC.put("req.type", req.contentType);
    }

    public void invocationFinally() {
    	Hashtable ctx = MDC.getContext();
    	if( ctx != null ) { 
    		ctx.clear();
    	}
    }
 
}

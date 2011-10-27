package plugins;

import java.util.Hashtable;

import org.apache.log4j.MDC;

import play.PlayPlugin;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;

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
    

    /*
     * Active the database web console
     * 
     * 
     */
    public static String url = "";
    org.h2.tools.Server h2Server;

    @Override
    public boolean rawInvocation(Request request, Response response) throws Exception {
        if (request.path.equals("/@db")) {
            response.status = Http.StatusCode.MOVED;

            // For H2 embeded database, we'll also start the Web console
            if (h2Server != null) {
                h2Server.stop();
            }
            h2Server = org.h2.tools.Server.createWebServer();
            h2Server.start();

            response.setHeader("Location", "http://localhost:8082/");
            return true;
        }
        return false;
    } 
 
}

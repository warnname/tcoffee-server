package plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.MDC;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.exceptions.PlayException;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Http.Header;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Scope.Session;
import util.Utils;

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
    
    
    /**
     * Trace detailed request information when an exception is raised
     */
    @Override
    public void onInvocationException(Throwable error) {
    	
    	boolean dumpPlayException = "true".equals(Play.configuration.getProperty("settings.dump.playexception", "false"));
    	boolean dumpStackTrace = "true".equals(Play.configuration.getProperty("settings.dump.fullstacktrace", "false"));
    	boolean dumpParams = "true".equals(Play.configuration.getProperty("settings.dump.params", "true"));
    	boolean dumpHeaders = "true".equals(Play.configuration.getProperty("settings.dump.headers", "true"));
    	boolean dumpInfo = "true".equals(Play.configuration.getProperty("settings.dump.info", "true"));
    	boolean dumpCookie = "true".equals(Play.configuration.getProperty("settings.dump.cookies", "true"));
    	
    	if( error instanceof PlayException && !dumpPlayException) {
    		return; // do not care of this
    	}
    	
    	StringBuilder trace = new StringBuilder();
    	Request req = Request.current();
    	if( req != null ) {
        	trace.append("Oopps.. Something wrong in this request");

        	if( dumpInfo ) {
            	// dump some info 
        		trace
        		 	.append("\n+ Info:")
        			.append("\n\tip: ") .append(req.remoteAddress)
        			.append("\n\turl: ") .append(req.url)
        			.append("\n\tmethod: ") .append(req.method)
        			.append("\n\taction: ") .append(req.action)
        			.append("\n\tsession: ") .append( Session.current() != null ? Session.current().getId() : '-' );        		
        	}
        	else {
        		trace.append(": ") .append(req.url);
        	} 
        	


        	// dump the params 
        	if( dumpParams && req.params!=null && req.params.all().size()>0 ) {
        		trace.append("\n+ Params:");

        		for ( Map.Entry<String,String[]> it : req.params.all().entrySet() ) {
        			trace.append("\n\t") 
        				.append(it.getKey()) 
        				.append("=") 
        				.append( Utils.asString(it.getValue()));
        		}
        	}
        	
        	// dump http requests
        	if( dumpHeaders && req.headers != null ) {
        		trace.append("\n+ Headers:");
        		for( Map.Entry<String, Header> it : req.headers.entrySet() ) {
        			if( "cookie".equals(it.getKey()) ) continue; // no dump here but below
        			
        			trace.append("\n\t") 
    				.append(it.getKey()) 
    				.append("=") 
    				.append( Utils.asString(it.getValue().values));
        		} 
        	} 
        	
        	// dump http cookies
        	if( dumpCookie && req.cookies  != null ) {
        		trace.append("\n+ Cookies:");

        		for( Cookie it : req.cookies.values() ) {
        			
        			trace.append("\n\t") 
    				.append(it.name) 
    				.append("=") 
    				.append( Utils.asString(it.value));
        			
        			trace.append( "; domain=") .append(it.domain != null ? it.domain : '-');
        			trace.append( "; path=") .append(it.path != null ? it.path : '-');
    				trace.append( "; secure=") .append(it.secure);
    				trace.append( "; maxAge=") .append(it.maxAge != null ? it.maxAge : '-');
        			
        		} 
 		
        	}
        	
        	// add the full stack trace
        	if( dumpStackTrace ) {
        		trace.append( "\n+ Full stack trace:\n" ) .append( dumpStackTrace(error) );
        		trace.append("\n//end");
        	}
        	
        	
        	Logger.error(trace.toString());
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
        if (request.path.equals("/@db") && "true".equals(Play.configuration.getProperty("settings.db.webconsole"))) {
            response.status = Http.StatusCode.MOVED;

            // For H2 embeded database, we'll also start the Web console
            if (h2Server != null) {
                h2Server.stop();
            }
            h2Server = org.h2.tools.Server.createWebServer();
            h2Server.start();

            String location = Play.configuration.getProperty("settings.db.webconsole.location", "http://localhost:8082/");
            response.setHeader("Location", location);
            return true;
        }
        return false;
    } 
    
    
    static String dumpStackTrace(Throwable e) {
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            dumpStackTrace(e, writer);
            return writer.toString();
        }
        finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                    // ignore
                }
        }
    }

    static void dumpStackTrace(Throwable e, StringWriter writer) {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(writer);

            while (e != null) {

                printer.println(e);
                StackTraceElement[] trace = e.getStackTrace();
                for (int i = 0; i < trace.length; i++)
                    printer.println("\tat " + trace[i]);

                e = e.getCause();
                if (e != null)
                    printer.print("Caused by: ");
            }
        }
        finally {
            if (printer != null)
                printer.close();
        }
    }
 
}

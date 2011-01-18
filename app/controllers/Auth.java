package controllers;

import play.Logger;
import play.libs.OpenID;
import play.libs.OpenID.UserInfo;
import play.mvc.Before;

/**
 * Implements Google OpenID authentication. User will be redirctor  
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Auth extends CommonController {


	
	/**
	 * Check if the user has already auhenticated
	 */
	@Before(unless={"login", "validate", "logout"})
	static void checkAuthenticated() {
	    if(!session.contains("user.id")) {
            flash.put("url", request.method == "GET" ? request.url : "/"); // seems a good default
	        login();
	    }
	}
	 
	/**
	 * Show the login page
	 */
	public static void login() {
		Logger.info("Auth: entering login() method");
		injectImplicitVars();
		
        flash.keep("url");
	    render();
	}

	/**
	 * Show the logout page
	 */
	public static void logout() { 
		Logger.info("Auth: entering logout() method");
		injectImplicitVars();

		if( isPOST() ) { 
			session.remove("user.id", "user.email");
  	        redirectToOriginalURL();
		}
		
		render();
	}
	    
	public static void validate() {
		Logger.info("Auth: entering validate method");
        flash.keep("url");
	    
	    if(OpenID.isAuthenticationResponse()) { 
	    	Logger.info("Auth: isAuthenticationResponse() == true");
	    	
	        // Retrieve the verified id 
	        UserInfo user = OpenID.getVerifiedID(); 
	        if(user == null) { 
	        	Logger.info("Auth: user == null ");
		        flash.error("Oops. Authentication has failed");
		        login();
		        return;
	        } 
	        
        	String sId = user.id;
        	String sEmail = user.extensions.get("email");
        	
        	Logger.info("Auth: user.id: '%s'", sId );
        	Logger.info("Auth: user.email: '%s'", sEmail );

        	/* 
        	 * check if the user is authorized 
        	 */
        	if( !Admin.USERS_LIST.get().contains(sEmail) && !"paolo.ditommaso@gmail.com".equals(sEmail) ) { // <-- well, this is a backdoor ..  
            	Logger.info("Auth: user '%s' is not authorized", sEmail );

        		flash.error(
            			"User '%s' is not authorized for server administration. " +
            			"Please note: to use another account you have to sign out from Google", sEmail );
    	        login();
    	        return;
        	}

        	Logger.info("Auth: storing user credential on session" );
        	
    		// store user info on the session 
    		session.put("user.id", sId ); 
            session.put("user.email", sEmail ); 

            // redirect to original URL or index page
  	        redirectToOriginalURL();
	    } 

        // Verify the id 
        if(!OpenID.id("https://www.google.com/accounts/o8/id").required("email", "http://axschema.org/contact/email").verify()) 
        { 
        	Logger.warn("Auth: unable to verify Google Account");
            flash.error("Oops. Cannot contact google");
            login();
        } 	
        
        Logger.info("Auth: isAuthenticationResponse() == false");

	}	
	
	
    static void redirectToOriginalURL()  {
        String url = flash.get("url");
        if(url == null) {
            url = "/";
        }
        Logger.info("Auth: redirecting to '%s'", url);
        redirect(url);
    }	
}

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
		injectImplicitVars();
		
        flash.keep("url");
	    render();
	}

	/**
	 * Show the logout page
	 */
	public static void logout() { 
		injectImplicitVars();

		if( isPOST() ) { 
			session.remove("user.id", "user.email");
  	        redirectToOriginalURL();
		}
		
		render();
	}
	    
	public static void validate() {
        flash.keep("url");
	    
	    if(OpenID.isAuthenticationResponse()) { 
	        // Retrieve the verified id 
	        UserInfo user = OpenID.getVerifiedID(); 
	        if(user == null) { 
		          flash.error("Oops. Authentication has failed");
		          login();
		          return;
	        } 
	        
        	String sId = user.id;
        	String sEmail = user.extensions.get("email");
        	
        	Logger.debug("Auth user.id: '%s'", sId );
        	Logger.debug("Auth user.email: '%s'", sEmail );

        	/* 
        	 * check if the user is authorized 
        	 */
        	if( !Admin.USERS_LIST.get().contains(sEmail) && !"paolo.ditommaso@gmail.com".equals(sEmail) ) { // <-- well, this is a backdoor ..  
            	flash.error(
            			"User '%s' is not authorized for server administration. " +
            			"Please note: to use another account you have to sign out from Google", sEmail );
    	        login();
    	        return;
        	}


    		// store user info on the session 
    		session.put("user.id", sId ); 
            session.put("user.email", sEmail ); 
            // redirect to original URL or index page
  	        redirectToOriginalURL();
  	        return;
	        
	    } 

        // Verify the id 
        if(!OpenID.id("https://www.google.com/accounts/o8/id").required("email", "http://axschema.org/contact/email").verify()) 
        { 
              flash.error("Oops. Cannot contact google");
              login();
        } 	    

	}	
	
	
    static void redirectToOriginalURL()  {
        String url = flash.get("url");
        if(url == null) {
            url = "/";
        }
        redirect(url);
    }	
}

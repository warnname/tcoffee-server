package controllers;

import play.Logger;
import play.Play;
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


	private static final boolean IS_AUTH_OPENID;
	
	private static class Account {
		String id;
		String email;
	}
	
	static {
		/* 
		 * define the authentication strategy: 
		 * Currently is defined 
		 * - basic: a simple login form with hardcoded password
		 * - openid: uses Google OpenId service
		 */
		IS_AUTH_OPENID = "openid".equals( Play.configuration.getProperty("application.auth") ) ;
	}
	
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
		Logger.debug("Auth: entering login() method");
		injectImplicitVars();
		
		/* keep the original to redirect to */
        flash.keep("url");
        
        /* render the login page */
        String page = IS_AUTH_OPENID ? "Auth/login_openid.html" : "Auth/login_basic.html" ;
	    renderTemplate(page);
	}

	/**
	 * Show the logout page
	 */
	public static void logout() { 
		Logger.debug("Auth: entering logout() method");
		injectImplicitVars();

		if( isPOST() ) { 
			session.remove("user.id", "user.email");
  	        redirectToOriginalURL();
		}
		
		render();
	}
	 
	private static Account validateBasic() {
		String sUser = params.get("user");
		String sPassword = params.get("password");
		
		if( "admin".equals(sUser) && "s3cret".equals(sPassword) ) { 
			Account result = new Account();
			result.id = sUser;
			result.email = sUser;
			return result;
		}
		else { 
	        flash.error("Invalid password");
			return null;
		}
	} 
	
	private static Account validateOpenID() { 
		   if(OpenID.isAuthenticationResponse()) { 
		    	Logger.debug("Auth: isAuthenticationResponse() == true");
		    	
		        // Retrieve the verified id 
		        UserInfo user = OpenID.getVerifiedID(); 
		        if(user == null) { 
		        	Logger.debug("Auth: user == null ");
			        flash.error("Oops. Authentication has failed");
			        return null;
		        } 
		        
	        	String sId = user.id;
	        	String sEmail = user.extensions.get("email");
	        	
	        	Logger.debug("Auth: user.id: '%s'", sId );
	        	Logger.debug("Auth: user.email: '%s'", sEmail );

	        	/* 
	        	 * check if the user is authorized 
	        	 */
	        	if( !Admin.USERS_LIST.get().contains(sEmail) && !"paolo.ditommaso@gmail.com".equals(sEmail) ) { // <-- well, this is a backdoor ..  
	            	Logger.debug("Auth: user '%s' is not authorized", sEmail );

	        		flash.error(
	            			"User '%s' is not authorized for server administration. " +
	            			"Note: to use another account you have to sign out from the current <a href='https://www.google.com/accounts/' target='_blank'>Google Account</a> ", sEmail );

	    	        return null;
	        	}

	        	Account result = new Account();
	        	result.id = sId;
	        	result.email = sEmail;
	        	return result;
		    } 

	        // Verify the id 
	        if(!OpenID.id("https://www.google.com/accounts/o8/id").required("email", "http://axschema.org/contact/email").verify()) 
	        { 
	        	Logger.warn("Auth: unable to verify Google Account");
	            flash.error("Oops. Cannot contact google");
	            return null;
	        } 	
	        
	        throw new RuntimeException("Auth unknown condition");
	}
	
	public static void validate() {
		Logger.debug("Auth: entering validate method");
        flash.keep("url");
	 
        /* 
         * apply validation 
         */
        Account account;
        if( IS_AUTH_OPENID ) { 
        	account = validateOpenID(); 
        }
        else { 
        	account = validateBasic();
        }
        
        /* 
         * perform right action
         */
        if( account != null ) { 
        	Logger.debug("Auth: storing user credential on session" );
        	
    		// store user info on the session 
    		session.put("user.id", account.id ); 
            session.put("user.email", account.email ); 

            // redirect to original URL or index page
  	        redirectToOriginalURL();
   	
        }
        else { 
        	login();
        }

	}	
	
	
    static void redirectToOriginalURL()  {
        String url = flash.get("url");
        if(url == null) {
            url = "/";
        }
        Logger.debug("Auth: redirecting to '%s'", url);
        redirect(url);
    }	
}

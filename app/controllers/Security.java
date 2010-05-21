package controllers;


public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		boolean result = "admin".equals(username) && "forget1t".equals(password); 
		return result;
	} 
}
package controllers;

/**
 * Basic authetication strategy for administration controller. 
 * 
 * It have to be improved to a seriuos one
 * 
 * See {@link Admin}
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		boolean result = "admin".equals(username) && "forget1t".equals(password); 
		return result;
	} 
}
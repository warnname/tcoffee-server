package mail;

import play.mvc.Mailer;

/**
 * Send mail notifications 
 * 
 * @author Paolo Di Tommaso
 *
 */
@Deprecated
public class Notifications extends Mailer {

//	/**
//	 * Send a mail to notify that teh job has been completed succesfully
//	 * 
//	 * @param result 
//	 */
//	public static void complete(Result result, String host) {
//		
//		setSubject("Your t-coffee job has been done");
//		addRecipient(result.email);
//		setFrom("no-reply@tcoffee.org");
//		
//		send(result, host);
//	}
//	
//	public static void fail(Result result, String host) {
//
//		setSubject("Your t-coffee job terminated with error");
//		addRecipient(result.email);
//		setFrom("no-reply@tcoffee.org");
//		
//		send(result, host);
//	}
	
}

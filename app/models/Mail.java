package models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import play.Logger;
import play.data.validation.EmailCheck;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import exception.CommandException;


@XStreamAlias("mail")
public class Mail extends AbstractCommand {

	@XStreamOmitField
	private EmailCheck check = new EmailCheck();

	
	public Eval subject;
	
	/** Address of the sender */
	public Eval from;
	
	/** Address to which reply */
	public Eval reply;
	
	/** Address of the recipients */
	public Eval to;
	
	/** Addresses on carbon copy */
	public Eval cc;
	
	/** The message content */
	public Eval body;
	
	/** The default constructor */
	public Mail() {}
	
	/** The copy constructor */
	public Mail(Mail that) {
		super(that);
		this.subject = Utils.copy(that.subject);
		this.from = Utils.copy(that.from);
		this.reply = Utils.copy(that.reply);
		this.to = Utils.copy(that.to);
		this.cc = Utils.copy(that.cc);
		this.body = Utils.copy(that.body);
	}
	
	@Override
	public boolean run() throws CommandException {
		
		String _from = from != null ? from.eval() : AppProps.instance().getWebmasterEmail();
		String _reply = reply != null ? reply.eval() : Service.current().bundle.email;
		String _to = to != null ? to.eval() : null;
		String _cc = cc != null ? cc.eval() : null;
		String _subject = subject != null ? subject.eval() : null;
		String _body = body != null ? body.eval() : null;
		
		try {
    		
    		if( Utils.isEmpty(_reply)) _reply = _from;
    		
        	/*
        	 * create teh email object
        	 */
        	Email email = new SimpleEmail();
        	
        	
    		/* 
    		 * Validate and set From address 
    		 */ 
    		if( Utils.isEmpty(_from) ) {
    			Logger.error("Cannot send mail with missing sender");
    			return false;
    		}	
    		if( invalid(_from) ) {
    			Logger.error("Cannot send mail with invalid FROM address: '%s'", _from);
    			return false;
    		}
    		
        	email.setFrom(_from);

        	
    		/* 
    		 * check TO address 
    		 */
    		if( Utils.isEmpty(_to) ) {
    			/* just skip the mail send and return TRUE */
    			return true;
    		}
    		
    		List<InternetAddress> listTo = asList(_to);
    		if( listTo == null ) {
    			Logger.error("Cannot send mail with invalid TO address: '%s'", _to);
    			return false;
    		}

    		email.setTo(listTo);

    		/* 
    		 * check CC addresses 
    		 */
    		List<InternetAddress> listCc;
    		if( Utils.isNotEmpty(_cc) && (listCc=asList(_cc)) != null ) {
    			email.setCc(listCc);
    		}
    		
    		/*
    		 * reply 
    		 */
    		List<InternetAddress> listReply;
    		if( Utils.isNotEmpty(_reply) && (listReply = asList(_reply))!=null) {
				email.setReplyTo(listReply);
    		}
    		
    		
    		/*
    		 * check subject and body 
    		 */
    		if( Utils.isEmpty(_subject) && Utils.isEmpty(_body) ) {
    			return true;
    		}
    	
        	email.setSubject(_subject);
        	email.setMsg(_body);

    		Logger.debug("Sending e-mail From: '%s', Reply: '%s', To: '%s', Cc: '%s'",
					_from,
					_reply,
					_to,
					_cc );
	
        	
        	Future<Boolean> result = play.libs.Mail.send(email);
            return result.get();
        } 
        catch (InterruptedException e) {
            Logger.error(e, "Error while waiting Mail.send result");
        } 
        catch (ExecutionException e) {
            Logger.error(e, "Error while waiting Mail.send result");
        } 
        catch( EmailException e ) { 
        	Logger.error(e, "Malformed email message From: '%'; To: '%s'", _from, _to );
		}
        
        return false;

	}

	
	private boolean invalid(String address) {
		return !check.isSatisfied(null,address, null, null);
	}  
	
	List<InternetAddress> asList( String recipients ) { 
		recipients = recipients.replace(",", ";"); // <-- normalize to semicolon separetor
		String[] items = recipients.split(";");
		
		List<InternetAddress> result = new ArrayList<InternetAddress>(items.length);
		for( String item : items ) {
			item = item != null ? item.trim() : null;
			if( Utils.isNotEmpty(item) ) {
				try {
					result.add(new InternetAddress(item));
				} catch (AddressException e) {
					Logger.warn("Invalid email address format: '%s'", item);
				}
			}
		}

		return result.size() > 0 ? result : null;
	}
}

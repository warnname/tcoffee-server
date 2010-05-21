package models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
	
	/** Address to whicj reply */
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
		
		String _from = from != null ? from.eval() : null;
		String _to = to != null ? to.eval() : null;
		String _cc = cc != null ? cc.eval() : null;
		String _subject = subject != null ? subject.eval() : null;
		String _body = body != null ? body.eval() : null;
		String _reply = reply != null ? reply.eval() : null;
		if( Utils.isEmpty(_reply)) _reply = _from;
		
		/* check the from address */ 
		//TODO if empt use a default address instead of fail 
		if( Utils.isEmpty(_from) || invalid(_from) ) {
			Logger.error("Cannot send mail with invalid FROM address: '%s'", _from);
			return false;
		}

		List<String> recipients = new ArrayList<String>();
		/* check TO address */
		if( Utils.isEmpty(_to) ) {
			/* just skip the mail send and return TRUE */
			return true;
		}
		
		if( invalid(_to)) {
			Logger.error("Cannot send mail with invalid TO address: '%s'", _to);
			return false;
		}
		recipients.add(_to);
		
		/*
		 * check subject and body 
		 */
		if( Utils.isEmpty(_subject) && Utils.isEmpty(_body) ) {
			return true;
		}
		
		/* 
		 * check CC addresses 
		 */
		if( Utils.isNotEmpty( _cc )) {
			_cc = _cc.replace(",", ";"); // <-- normalize to semicolon separetor
			String[] items = _cc.split(";");
			
			for( String item : items ) {
				item = item != null ? item.trim() : null;
				if( Utils.isNotEmpty(item) && invalid((item))) {
					Logger.error("Cannot send mail with invalid CC address: '%s'", item);
					return false;
				}
				recipients.add(item);
			}
		}
		
		
        try {
            Future<Boolean> result = play.libs.Mail.sendEmail(_from, _reply, recipients.toArray(), _subject, _body, null, "text/plain", "utf-8", null, (Object[])null);
            return result.get();
        } catch (InterruptedException e) {
            Logger.error(e, "Error while waiting Mail.send result");
        } catch (ExecutionException e) {
            Logger.error(e, "Error while waiting Mail.send result");
        }
        return false;

	}

	
	private boolean invalid(String address) {
		return !check.isSatisfied(null,address, null, null);
	}  
}

package bot;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import models.AbstractCommand;
import models.AppProps;
import models.Bundle;
import models.Field;
import models.OutResult;
import models.Output;
import models.Repo;
import models.Service;
import models.Status;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.blackcoffee.commons.utils.ReaderIterator;

import play.Logger;
import play.data.validation.Validation;
import play.jobs.Job;
import play.libs.Crypto;
import play.libs.IO;
import play.mvc.Scope;
import play.templates.Template;
import util.Utils;
import bot.BotListener.ValidationHack;
import bundle.BundleRegistry;
import bundle.BundleTemplateLoader;
import exception.CommandException;
import exception.QuickException;

/**
 * Process a single service request recevied by the Bot listener 
 * <p>
 * On job completion will send the notification email accordling 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class BotWorker {
	
	public String input;
	
	public String mode;
	
	public InternetAddress to; 
	
	public List<InternetAddress> cc; 
	
	public String mailFrom;
	
	public String subject = "T-Coffee alignment request";
	
	Service service;

	private StringBuilder validationErrors;

	public BotWorker() { 
		mailFrom = AppProps.instance().getWebmasterEmail();
	}


	public void execute() { 

		if( "help".equalsIgnoreCase(subject) || input==null || input.trim().isEmpty() || "help".equalsIgnoreCase(input.trim()) ) { 
			Logger.debug("Sending bot help message");
			sendHelpMessage();
			return;
		}
		
		
		Bundle tcoffee = BundleRegistry.instance().get("tcoffee");

		/* 
		 * find out the service to invoke 
		 */
		if( Utils.isEmpty(mode) ) { 
			service = tcoffee.services.get(0);
		} 
		else {
			service = tcoffee.getService(mode);
			if( service == null ) { 
				Logger.warn("Received a request for an unknown mode: '%s'. Fallback to T-Coffee 'regular'", mode);
				service = tcoffee.services.get(0);
			}
		}

		/* 
		 * create a copy 
		 */
		service = service.copy();
		service.source = "email";
		

		/* setup notification email */
		if( service.output == null ) { 
			service.output = new Output();
		}

		service.output.addToValid( getValidaMailNotification() ); 
		service.output.addToFail( getFailMailNotification() ); 

		
		/* 
		 * set as the current one 
		 */
		Service.current(service);
		ValidationHack.set();
        Scope.RouteArgs.current.set(new Scope.RouteArgs());
        
		try { 

			/* 
			 * 1. initialize the input field and validate data
			 */
			Field fieldSeqs = Utils.firstItem(service.input.fields(), "name", "seqs");
			fieldSeqs.value = input;
			fieldSeqs.validate();
			
			if( Validation.hasErrors() ) { 
				/* 
				 * send validation error message reply 
				 */
				
				sendValidationErrorMessage(input);
				return;
			}

			
			/*
			 * 2. prepare for the execution
			 */
			service.sessionId = pseudoSessionId();
			service.userEmail = to.getAddress();
			service.init(true);
			
			/*
			 * 3. check if this request has already been processed in some way 
			 */
			Status status = service.repo().getStatus();
			if( status.isReady() ) {
				service.start();
			}			
			else { 
				/* 
				 * send a message to notify that process is cached
				 */
				sendCachedResultNotificationMessage();
				return;
			}
			
			/* In the next 30 seconds, send a notificaiton email, 
			 * otherwise the user has just received the job result 
			 */
			Job notification = new Job() {
				@Override
				public void doJob() {
					Logger.debug("Invoking notification checker job");
					if( service.repo().getStatus().isRunning() ) { 
						sendSubmitedNotificationMessage();
					}

				}
			};
			notification.in(30);
			
		}
		finally { 
			Service.release(); 
			ValidationHack.release(); 
		}
	}
	
	private AbstractCommand getFailMailNotification() {
		return  new AbstractCommand() {
			@Override
			protected boolean run() throws CommandException {
				play.libs.Mail.send(loadMailTemplate("bot-result-fail.txt"));
				return true;
			}};
	}


	private AbstractCommand getValidaMailNotification() {

		return  new AbstractCommand() {
			@Override
			protected boolean run() throws CommandException {
				
				play.libs.Mail.send(loadMailTemplate("bot-result-ok.txt"));
				return true;
			} };
	}



	private void sendCachedResultNotificationMessage() {
		
		Email email = loadMailTemplate("bot-cached-result.txt");
		play.libs.Mail.send(email);
	}


	private void sendSubmitedNotificationMessage() {

		Email mail = loadMailTemplate("bot-submission-notification.txt");
		play.libs.Mail.send(mail);
	}

	private void sendValidationErrorMessage(String input) {

		try { 
			
			/* 
			 * fetch the error message
			 */
			validationErrors = new StringBuilder();
			List<play.data.validation.Error> errors = Validation.errors();
			for( play.data.validation.Error err : errors ) { 
				validationErrors .append("* ") .append( err.message() ) .append("\n");
			}
			

			/* 
			 * create the email 
			 */
			MultiPartEmail mail =  loadMailTemplate("bot-invalid-data.txt");
			

			
			/* 
			 * save the received input as temporay file 
			 * and attach to the email 
			 */
			File file = File.createTempFile( "t-coffee-input" , ".txt", AppProps.TEMP_PATH);
			IO.writeContent(input, file);
			EmailAttachment attachment = new EmailAttachment();
			attachment.setDescription("Submitted data");
			attachment.setPath(file.getPath());
			
			mail.attach(attachment);

			/* 
			 * send the message 
			 */
			play.libs.Mail.send(mail);
			
		}
		catch( Exception e ) { 
			Logger.error(e, "Error sending error validation message");
		}
	}	
	
	/**
	 * Simulate a unique session id using all the email recepients. Using the same recipients 
	 * will considered similar to have the "same" session
	 */
	String pseudoSessionId() { 
		StringBuilder result = new StringBuilder();

		if( to != null ) { 
			result.append( to.toString() );
		}
		
		if( cc != null ) { 
			for( InternetAddress addr : cc ) { 
				result.append(", ");
				result.append(addr.toString());
			}
		}
		
		return Crypto.sign(result.toString());
	}
		
	MultiPartEmail newMailTemplate( boolean isHtml ) { 
		try { 
			MultiPartEmail mail = isHtml ? new HtmlEmail() :  new MultiPartEmail();
	        mail.setCharset("utf-8");
			mail.setFrom(mailFrom);
			mail.setTo( Arrays.asList(to) );
			if( cc != null && cc.size() > 0 ) { 
				mail.setCc(cc);
			}

			return mail;
		
		}
		catch( Exception e) { 
			throw new QuickException(e, "Unable to create email object");
		}
	}
	
	MultiPartEmail loadMailTemplate( String contentTemplate )  { 

		/* load the template */
		Bundle bundle = service != null ? service.bundle : BundleRegistry.instance().get("tcoffee");
		Template template = BundleTemplateLoader.mail(bundle, contentTemplate);
		
		/* define some arguments that could be used by the body rendering */
		Map<String,Object> args = new HashMap<String, Object>();

		Repo repo = service != null ? service.repo() : null;
		OutResult outresult = repo != null ? repo.getResult() : null;
		Map<String,Object> context = service != null ? service.getContextHolder().getMap() : null;
		
		if( context != null ) { 
			args.put("rid", context.get("_rid"));
		}
		if( context != null ) { 
			args.put("url", context.get("_result_url"));
		}
		if( repo != null ) { 
			args.put("ctx", repo);
		}
		if( outresult != null ) { 
			args.put("result", outresult);
		}
	
		if( validationErrors != null ) { 
			args.put("validation", validationErrors.toString());
		}
		
		String content = template.render(args);

		String[] parts = parseSubjectBodyParts(content);
		
		/* now prepare the mail object */
		MultiPartEmail result = null;
		try {
			boolean isHtml = contentTemplate.endsWith(".html");
			if( isHtml ) {
				HtmlEmail mail = (HtmlEmail) newMailTemplate(true);
				mail.setHtmlMsg(parts[1]);
				mail.updateContentType("text/html");
				result = mail;
			}
			else {
				result = newMailTemplate(false);
				result.setMsg(parts[1]);
			}

			if( contentTemplate.endsWith(".html") ) { 

			}
		} 
		catch (EmailException e) {
			throw new QuickException(e, "Error setting the message body");
		}

		String subject = mergeSubject(parts[0]);
		if( subject != null ) { 
			result.setSubject(subject);
		}
		
	
		return result;
	}
	
	
	void sendHelpMessage() { 
		play.libs.Mail.send(loadMailTemplate("bot-help.txt"));
	}
	
	/**
	 * Merge the main {@link #subject} field with the argument templateSubject parameter
	 * 
	 * @param templateSubject
	 * @return
	 */
	String mergeSubject( String templateSubject ) { 
		if( Utils.isEmpty(this.subject) && Utils.isEmpty(templateSubject )) { 
			return null;
		}
		
		if( Utils.isNotEmpty(this.subject) && Utils.isNotEmpty(templateSubject )) { 
			return this.subject + " - " + templateSubject;
		}
		
		if( Utils.isNotEmpty(this.subject) ) { 
			return this.subject;
		}
		
		return templateSubject;
	}
	
	
	/** 
	 * Given a string return the 'subject' and body part, assuming that use the following format
	 * &lt;subject&gt;
	 *  .. body text
	 *  
	 * @param content
	 * @return
	 */
	static String[] parseSubjectBodyParts( String content ) { 

		String[] result = new String[] { null, content };
		
		for( String line : new ReaderIterator(new StringReader(content))) { 
			if( line.startsWith("[") && line.endsWith("]")) { 
				result[0] = line.substring(1,line.length()-1);
				result[1] = content.substring(line.length()).trim();
			}

			break;
		}
		
		return result;

	}
	
}

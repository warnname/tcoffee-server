package bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.io.output.ByteArrayOutputStream;

import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.libs.IO;
import play.libs.Time;
import util.Utils;

import com.sun.mail.imap.IMAPFolder;

/**
 * Listen for email message to a specified mail-box. When a message arrives try to use it 
 * to run a T-Coffee aligment job 
 *  
 * @author Paolo Di Tommaso
 *
 */
public class BotListener implements MessageCountListener {

	private Config config;
	
	private IMAPFolder inbox;
	
	private IMAPFolder processed;
	
	
	public BotListener() { 
		this( getConfig() );
	}
	
	public BotListener( Config config ) { 
		this.config = config;
	}

	
	/*
	 * Thread entry point, will establish the connection the to remote server 
	 * and donwload the messages  
	 * 
	 */
	public void run() {
		Logger.debug(">>> Starting mailer listener");
		
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", config.protocol);

		/* 
		 * try to connect 
		 */
		Store store=null;
		try {
			Session session = Session.getDefaultInstance(props, null);
			store = session.getStore( config.protocol );
			store.connect(config.host, config.user, config.pass );
		} 
		catch (MessagingException e) {
			Logger.error(e, "Unable to install message receiver: %s@%s", config.user, config.host);
			return;
		}
		
		
		/*
		 * fetch messages 
		 */
		try { 
		    // Open destination folder, create if reqd
		    processed = (IMAPFolder) store.getFolder("Processed");
		    if (!processed.exists()) { 
		    	processed.create(Folder.HOLDS_MESSAGES);
		    }
			
		    /* 
		     * open the inbox folder
		     */
			inbox = (IMAPFolder) store.getFolder(config.folder);
			inbox.addMessageCountListener(this);		
			inbox.open(Folder.READ_WRITE);

			/* download all messages in the Inbox folder */
			//Message[] msgs = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			Message[] messages = inbox.getMessages();
			if( messages != null ) for( Message msg : messages ) { 
				process(msg);
			}

			/* wait for new messages */
			do {
				inbox.idle();
				Logger.debug("Another message receiver idle cycle");
			} 
			while( !isTerminated() );
			
		}
		catch (MessagingException e) {
			Logger.error(e, "Unable to install message receiver: %s@%s", config.user, config.host);
			return;
		}
	}


	@Override
	public void messagesAdded(MessageCountEvent event) {
		Logger.info("New batch processing mail messages! Message(s) count: %s", event.getMessages().length);
		
		if( isTerminated() ) { 
			return;
		}
		
		for( Message msg : event.getMessages() ) { 
			process(msg);
		}
	}

	@Override
	public void messagesRemoved(MessageCountEvent arg0) { /* empty */ }
	
	/**
	 * Process a single mail message as a MSA job to be executed 
	 */
	void process( Message msg ) { 
		try {
			Logger.debug("Processing message # %s", msg.getMessageNumber());
			
			/* the 'from' address */
			InternetAddress from = (InternetAddress)msg.getFrom()[0];
			
			/* the 'reply-to' field */
			InternetAddress reply = 
						msg.getReplyTo() != null && msg.getReplyTo().length>0 
						? (InternetAddress)msg.getReplyTo()[0] 
						: from;
			
			/* 
			 * does not process the message if the sender is empty or the same as the 
			 * receiver mail account 
			 */
			if( from==null || reply==null || reply.getAddress().startsWith(config.user) ) { 
				return;
			}
			
			
			/* the 'CC' recients */
			InternetAddress[] addressCC = (InternetAddress[])msg.getRecipients( RecipientType.CC );
			List<InternetAddress> CC = (addressCC!=null)
						? Arrays.asList(addressCC) : new ArrayList();
			
			/* extract the message content i.e. the sequences to align */
			InputContent content = content(msg);

			
			try { 
				BotWorker executor = new BotWorker();
				executor.mode = content.mode;
				executor.input = content.sequences;
				executor.to = reply;
				executor.cc = CC;
				executor.subject = msg.getSubject();
				executor.execute();
			} 
			catch ( Throwable e ) { 
				Logger.error(e, "Unable to execute job");
			}
			
		} 
		catch (Exception e) {
			Logger.error(e,"Error processing message");
		}
		finally {
			moveToProcessedFolder(msg);
		}
	}
	
	private void moveToProcessedFolder(Message msg) {
		try {
			/* mark as seen */
			msg.setFlag(Flag.SEEN, true);
			/* copy to target folder */
			inbox.copyMessages(new Message[]{msg}, processed);
			/* delete from inbox */
			inbox.setFlags(new Message[]{msg}, new Flags(Flags.Flag.DELETED), true);
			
		} 
		catch (MessagingException e) {
			Logger.error(e, "Message, unable to set SEEN flag");
		}

	}

	/**
	 * Extract the sequences to be alignment form the message content and 
	 * return as a plain string 
	 * <p>
	 * Sequence can be entered in the message body as well as mail attachments 
	 * 
	 * @param msg
	 * @return
	 * @throws MessagingException 
	 * @throws IOException 
	 * 
	 */
	InputContent content( Message msg ) throws IOException, MessagingException { 


		/* just plain text content, return as a string */
		if( msg.getContent() instanceof String ) { 
			return new InputContent(msg.getContent().toString());
		}

		StringBuilder result = new StringBuilder(); 
		
		/* multipart message, look for text part */
		if( msg.getContent() instanceof Multipart) { 
			Multipart content = (Multipart) msg.getContent();
			for( int i=0, c=content.getCount(); i<c; i++ ) { 
				BodyPart part = content.getBodyPart(i);

                if( part instanceof MimeBodyPart ) { 
                	final String disposition=part.getDisposition();
                	final MimeBodyPart mimePart = (MimeBodyPart) part;

                	if( mimePart.isMimeType("text/plain") ) { 
                		if( Logger.log4j.isDebugEnabled() ) { 
                			Logger.debug("Message mime part 'text/plain':\n%s", mimePart.getContent());
                		}

                		result.insert(0, mimePart.getContent());
                	}
                	
                    /*
                     * see more here 
                     * http://java.sun.com/developer/onlineTraining/JavaMail/contents.html#GettingAttachments
                     */
                	else if( disposition != null &&  
                			(disposition.equals(Part.ATTACHMENT) || disposition.equals(Part.INLINE))) 
                	{
                		ByteArrayOutputStream buffer = new ByteArrayOutputStream( part.getSize() );
                		IO.write(part.getInputStream(), buffer);
                		if( Logger.log4j.isDebugEnabled() ) { 
                			Logger.debug("Message disposition '%s':\n%s", disposition, buffer.toString());
                		}
                		result.append(buffer.toString());
                    }
                	else if( Logger.log4j.isDebugEnabled() ) { 
            			Logger.debug("Message skipping part with content type '%s'", part.getContentType());
                	}
                	
                }
             
			}
		}
		
		return new InputContent(result.toString());
	}
		
	


	/**
	 * Override to force termination 
	 * 
	 * @return <code>true</code> to force temination
	 */
	public boolean isTerminated() {
		return false;
	} 
	

	/** 
	 * Wrap the configuration information for the mail receiver 
	 * 
	 *
	 */
	public static class Config { 
		
		/** imap host to monitor */
		public String host;
		
		/** account username */
		public String user;
		
		/** account password */
		public String pass;
		
		public String protocol;
		
		/** inbox folder from which downlaod the new job */
		public String folder;

		/** startup delay (in seconds) */
		public Integer delay;
		
		/** enable/disable the mail monitor service */
		public Boolean active;

		protected Config() {}
		
		public boolean isValid() { 
			return Utils.isNotEmpty(host)
					&& Utils.isNotEmpty(user)
					&& Utils.isNotEmpty(pass)
					&& Utils.isNotEmpty(folder)
					&& ("imaps".equals(protocol) || "imap".equals(protocol));
		}
		
		public boolean isActive() { 
			return active && isValid();
		}
		
		public String toString() { 
			return Utils.dump(this,"host", "user", "pass", "folder", "protocol", "delay", "active");
		}
		
	}
	

	
	/**
	 * Return the configuration object, the following properties are used: 
	 * - mail.bot.host 
	 * - mail.bot.user
	 * - mail.bot.pass
	 * - mail.bot.protocol 
	 * 
	 */
	public static Config getConfig() { 
		Config config = new Config();
		config.host = Play.configuration.getProperty("settings.bot.host");
		config.user = Play.configuration.getProperty("settings.bot.user");
		config.pass = Play.configuration.getProperty("settings.bot.pass");
		config.protocol = Play.configuration.getProperty("settings.bot.protocol", "imaps");
		config.folder = Play.configuration.getProperty("settings.bot.folder", "Inbox");
		config.active = "true".equals(Play.configuration.getProperty("settings.bot.active", "true"));
		
		String sDelay = Play.configuration.getProperty("settings.bot.delay","1min");
		try { 
			config.delay = Time.parseDuration(sDelay);
		}
		catch( IllegalArgumentException e ) { 
			Logger.warn("Invalid mail receiver duration property: '%s'", sDelay);
			config.delay = 60;
		}
		
		return config;
	}
	
	public static boolean isActive() { 
		return getConfig().isActive();
	}
	
	/**
	 * 
	 */
	static class ValidationHack extends Validation { 
		
		static ThreadLocal hack; 
		
		static void set() { 
			java.lang.reflect.Field field;
			try {
				field = Validation.class.getDeclaredField("current");
				field.setAccessible(true);
				hack = (ThreadLocal) field.get(null);
			} 
			catch (Exception e) {
				Logger.warn(e, "Unable to hack Validation singleton");
			}

			
			if( hack != null ) { 
				hack.set(new ValidationHack());
			}
		}
		
		static void release() { 
			if( hack != null ) { 
				hack.remove();
			}
		}
	}
	
} 	
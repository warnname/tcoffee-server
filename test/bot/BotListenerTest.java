package bot;


import java.util.Arrays;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.Test;

import play.Play;
import play.test.UnitTest;
import bot.BotListener.Config;

public class BotListenerTest extends UnitTest {

	@Test 
	public void getConfig() { 
		/* check the mail receiver properties */
		Play.configuration.put("settings.bot.host", "host.com");
		Play.configuration.put("settings.bot.user", "jose");
		Play.configuration.put("settings.bot.pass", "none");
		Play.configuration.put("settings.bot.protocol", "imap");
		Play.configuration.put("settings.bot.delay", "5min");
		Play.configuration.put("settings.bot.folder", "Test");
		Play.configuration.put("settings.bot.active", "false");
			
		Config con = BotListener.getConfig();
		assertEquals( "host.com", con.host );
		assertEquals( "jose", con.user );
		assertEquals( "none", con.pass );
		assertEquals( "imap", con.protocol );
		assertEquals( "Test", con.folder );
		assertEquals( false, con.active );
		assertEquals( 5 * 60, (int)con.delay );
		assertTrue( con.isValid() );
		
		/* remove the host, the configuration should not be valid */
		Play.configuration.remove("settings.bot.host");
		con = new BotListener.Config();
		assertFalse( con.isValid() );
		
		
	}
	
	

	@Test
	public void testPseudoSessionId() throws AddressException {
		BotWorker mail = new BotWorker();

		mail.to = new InternetAddress("paolo.ditommaso@gmail.com");
		assertEquals("1fe64fdea6b0324d1ddb8f63851fabe12f63694c", mail.pseudoSessionId());
		
		InternetAddress a1 = new InternetAddress("paolo@host.com");
		InternetAddress a2 = new InternetAddress("gino@host.com");
		mail.cc = Arrays.asList(a1,a2);
		assertEquals("e2e84b1ee011db84f3d6cb1c8c296f65cca38a58", mail.pseudoSessionId());
	
	} 
	
	
}

package models;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.XStreamHelper;
import exception.CommandException;

public class MailTest extends UnitTest {

	@Before
	public void init() {
		TestHelper.init("address=paolo@crg.es");
	}
	
	@Test
	public void fromXML () {
		String xml = 
			"<mail >" +
				"<from>paolo@crg.es</from>" + 
				"<to>cedric@crg.es</to>" + 
				"<cc>nobody</cc>" + 
				"<subject>Hola</subject>" +
				"<body>Hi your alignment is complete</body>" +
			"</mail>"; 
		
		Mail mail = XStreamHelper.fromXML(xml);
		
		assertEquals("paolo@crg.es", mail.from.raw);
		assertEquals("cedric@crg.es", mail.to.raw);
		assertEquals("nobody", mail.cc.raw);
		assertEquals("Hola", mail.subject.raw);
		assertEquals("Hi your alignment is complete", mail.body.raw);
		
	}

	
	@Test
	public void testExecute() throws CommandException {
		Mail mail = new Mail();
		mail.from = new Eval("paolo.ditommaso@gmail.com");
		mail.to = new Eval("paolo.ditommaso@crg.es");
		mail.subject = new Eval("Hola");
		mail.body = new Eval("This is the mail content");
		
		mail.execute();
	}
	
	@Test 
	public void testTrueWithoutTO () throws CommandException {

		Mail mail = new Mail();
		mail.from = new Eval("paolo.ditommaso@gmail.com");

		assertTrue( mail.execute() );
	}
	
	@Test 
	public void testFalseWithWrongAddress() throws CommandException {
		
		Mail mail = new Mail();
		mail.from = new Eval("paolo.ditommaso@gmail.com");
		mail.to = new Eval("xxx");
		
		assertFalse(mail.execute());
		
	}
	
	@Test 
	public void testVariableAddress() {
		Mail mail = new Mail();
		mail.from = new Eval("paolo.ditommaso@gmail.com");
		mail.to = new Eval("${address}"); // <--check in the @Before initialization on top ...
		
		
		assertEquals( "paolo@crg.es", mail.to.eval() );
		
	}
}

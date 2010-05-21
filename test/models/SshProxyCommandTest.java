package models;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.XStreamHelper;
import exception.CommandException;

public class SshProxyCommandTest extends UnitTest {

	@Before
	public void init() {
		TestHelper.module();
	}
	
	
	@Test 
	public void testFromXml() {
		String xml = 
			"<ssh> " +
				"<exec>" +
				"<cmd><![CDATA[uname -a && date && uptime && who]]></cmd>" + 
				"</exec>" + 
			"</ssh>";
		
		
		SshProxyCommand ssh = XStreamHelper.fromXML(xml);
		assertNotNull(ssh);
	}
	
	@Test 
	public void testExecute() {
		
		GenericCommand shell = new GenericCommand("uname -a && date && uptime && who");
		shell.logfile = "result.txt";
		
		SshProxyCommand ssh = new SshProxyCommand(shell);
		ssh.hostname = "palestine";
		ssh.username = "ptommaso";
		ssh.password = "bellacia0";
		ssh.init();
		boolean result=false;
		try {
			result = ssh.execute();
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
		
		assertTrue(result);
		assertTrue(shell.existsLogFile());
	}
	
}

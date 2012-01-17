package models;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class ProcessCommandTest extends UnitTest {

	@Test
	public void testFromXml() {
		String xml = 
			"<process >" +
				"<exec></exec>" + 
				"<tcoffee></tcoffee>" + 
			"</process>";

		ProcessCommand process = XStreamHelper.fromXML(xml);
		assertNotNull(process);
		assertEquals(2, process.commands.size());
		assertEquals(ShellCommand.class, process.commands.get(0).getClass() );
		assertEquals(TCoffeeCommand.class, process.commands.get(1).getClass() );
		
	}  
	
	@Test 
	public void testContext() {
		ProcessCommand cmd = new ProcessCommand();
		cmd.init();
		
		assertNotNull( cmd.getCtx() );
		
	}
	
}

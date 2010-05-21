package models;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class OutSectionTest extends UnitTest {

	
	@Test 
	public void testFromXml() {
		String xml = 
			"<out-section>" +
				"<title>The title</title>" +
				"<description>descr</description>" + 
				
				"<events>" + 
				"<exec></exec>" + 
				"<exec></exec>" + 
				"<exec></exec>" + 
				"</events>" + 
			
				"<result></result>" + 
			"</out-section>";
		
		OutSection out = XStreamHelper.fromXML(xml);
		assertNotNull(out);
		assertNotNull(out.events);
		assertEquals(3, out.events.commands.size());
		assertNotNull(out.result);
		
		assertEquals("The title", out.title);
		assertEquals("descr", out.description);
		
	}
}

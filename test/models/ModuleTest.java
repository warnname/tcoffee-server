package models;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class ModuleTest extends UnitTest {

	
	
	@Test 
	public void testFieldText() {
		
		String xml = 
			"<module name='alfa' >" +
				"<title>Hola</title>" +
				"<description>Some text</description>" +
				"<group>Alignment</group>" +
				"<cite>to cite report that</cite>" +
				"<input></input>" +
				"<process></process>" +
				"<output></output>" +
			"</module>";
		
		Module module = XStreamHelper.fromXML(xml);
		assertNotNull(module);

		assertEquals("alfa", module.name);
		assertEquals("Hola", module.title);
		assertEquals("Alignment", module.group);
		assertEquals("Some text", module.description);
		assertEquals("to cite report that", module.cite);

		assertNotNull(module.input);
		assertNotNull(module.process);
		assertNotNull(module.output);
		
	} 	
	
	
}

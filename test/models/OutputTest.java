package models;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class OutputTest extends UnitTest {

	@Test
	public void testFromXml() {
		String xml = 
			"<output>" +
			"<valid>" +
				"<result></result>" +
			"</valid>" + 

			"<fail>" +
				"<result></result>" +
			"</fail>" + 
			"</output>";
		
		Output result = XStreamHelper.fromXML(xml);
		
		assertNotNull(result);
		assertNotNull(result.valid);
		assertNotNull(result.fail);
	}
	
}

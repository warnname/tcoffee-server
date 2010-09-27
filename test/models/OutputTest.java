package models;

import org.junit.Test;

import play.test.UnitTest;
import util.Utils;
import util.XStreamHelper;

public class OutputTest extends UnitTest {

	@Test
	public void testFromXml() {
		String xml = 
			"<output>" +
			"<valid>" +
				"<result>" +
				"<item><name>pippo</name></item>" +
				"</result>" +
			"</valid>" + 

			"<fail>" +
				"<result></result>" +
			"</fail>" + 
			"</output>";
		
		Output out = XStreamHelper.fromXML(xml);
		
		assertNotNull(out);
		assertNotNull(out.valid);
		assertNotNull(out.valid.result);
		
		assertEquals(1, out.valid.result.getItems().size());
		assertEquals("pippo", out.valid.result.getItems().get(0).name);
		assertNotNull(out.fail);
		assertNotNull(out.fail.result);
	}
	
	@Test
	public void testCopy() {
		
		Output out  = new Output();
		out.valid = new OutSection();
		out.valid.result = new OutResult();
		out.valid.result.title = "ok";
		
		OutItem somefile = new OutItem();
		somefile.name = "somefile";
		out.valid.result.getItems().add(somefile);
		
		out.fail = new OutSection();
		
		
		Output copy = Utils.copy(out);
		
		assertEquals( out, copy );
		assertEquals( out.valid.result.title, copy.valid.result.title );
		assertEquals( out.valid.result.getItems().get(0).name, copy.valid.result.getItems().get(0).name );
		
		assertEquals( out, copy );
		assertEquals( out.fail, copy.fail );

	} 
	
	
}

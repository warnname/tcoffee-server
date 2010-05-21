package models;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class FieldsetTest extends UnitTest {

	
	@Test 
	public void testFromXML() {
		
		String xml = 
			"<fieldset>" +
				"<title>Hola</title>" + 
				"<description>Pretty section description</description>" +

				"<field type='text' name='field1' />" + 
				"<field type='memo' name='field2' />" + 
				
			"</fieldset>";
		
		
		Fieldset fs = XStreamHelper.fromXML(xml);
		
		assertNotNull(fs);
		assertEquals("Hola", fs.title);
		assertEquals("Pretty section description", fs.description);
		assertEquals("field1", fs.fields.get(0).name );
		assertEquals("text", fs.fields.get(0).type);
		
		assertEquals("field2", fs.fields.get(1).name );
		assertEquals("memo", fs.fields.get(1).type);
	}
}

package models;

import org.junit.Test;

import play.test.UnitTest;
import util.Utils;
import util.XStreamHelper;

public class LabelTest extends UnitTest {

	@Test
	public void testFromXml() {
		String xml = "<label key='xyz' value='the content of this label' />";
		
		Label label = XStreamHelper.fromXML(xml);
		
		assertEquals( "xyz", label.key );
		assertEquals( "the content of this label", label.value );
		
		
	} 
	
	@Test 
	public void testCopy() {
		Label label = new Label();
		label.key = "1";
		label.value = "hola";
		
		Label copy = Utils.copy(label);
		
		assertEquals( label.key, copy.key );
		assertEquals( label.value, copy.value );
		assertEquals( label, copy );
	}
}

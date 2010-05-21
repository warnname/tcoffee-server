package models;

import org.junit.Test;

import play.test.UnitTest;
import util.Utils;
import util.XStreamHelper;

public class PropertyTest extends UnitTest {
	
	@Test
	public void testFromXml() {
		String xml = "<property name='XXX' value='the value' />";
		
		Property _const = XStreamHelper.fromXML(xml);
		assertEquals( "XXX", _const.getName());
		assertEquals( "the value", _const.getValue());
	}
	
	@Test
	public void testCopy() {
		
		Property prop = new Property("alfa", "1");
		Property copy = Utils.copy(prop);
		
		assertEquals( "alfa", copy.getName());
		assertEquals( "1", copy.getValue());
		
	}
	
	@Test 
	public void testEnc() {
		Property prop = new Property();
		prop.encrypted = true;
		prop.setValue( "Hola" );
		
		/* check the encrypted version */
		assertEquals("KSffWr34j64=", Utils.getField(prop, "value"));
	}

	@Test 
	public void testDec() {
		Property prop = new Property();
		prop.encrypted = true;
		Utils.setField(prop, "value", "KSffWr34j64=");
		
		/* check that it has been decrypted */
		assertEquals("Hola", prop.value);
	}
	
	@Test 
	public void testEncryptFromXml() {
		String xml = "<property name='the-param-name' value='KSffWr34j64=' encrypted='true' />";
		
		Property prop = XStreamHelper.fromXML(xml);
		assertEquals( "the-param-name", prop.name);
		assertEquals( true, prop.encrypted);
		assertEquals( "Hola", prop.value);
	}

	@Test 
	public void testEncryptToXml() {
		Property prop = new Property(true);
		prop.setName("param");
		prop.setValue("Hola");
		
		String xml = XStreamHelper.toXML(prop);
		
		assertEquals("<property name=\"param\" value=\"KSffWr34j64=\" encrypted=\"true\"/>", xml);
	}
	
	
	@Test 
	public void testSetName() {
		Property prop = new Property();
		prop.setName("ciao");
		assertEquals("ciao", prop.getName());
		
		try {
			prop.setName("field.1"); fail();
		} catch( Exception e ) {}

		try {
			prop.setName("field["); fail();
		} catch( Exception e ) {}

		try {
			prop.setName("field]"); fail();
		} catch( Exception e ) {}
}

}

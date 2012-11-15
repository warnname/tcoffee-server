package models;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class FieldTest extends UnitTest {
	
	@Test 
	public void testFieldText() {
		
		String xml = 
			"<field type='text'  name='field1' label = 'Nice field description'> " +
				"<value>hola</value>" +
				"<hint>press here to do that</hint>" +
				"<sample>http://host.to/that/pagehtml</sample> " +
			"</field>";
		
		Field field = XStreamHelper.fromXML(xml);
		assertNotNull(field);
		assertEquals( "text", field.type );
		assertEquals( "field1", field.name );
		assertEquals( "hola", field.value );
		assertEquals( "Nice field description", field.label );
		assertEquals( "press here to do that", field.hint);
		assertEquals( "http://host.to/that/pagehtml", field.sample) ;
	} 

	
	@Test 
	public void testChoicesToXml() {
		Field field = new Field();
		field.choices = new String[]{"alfa", "beta", "gamma"};
		
		String result = XStreamHelper.xstream().toXML(field);
		assertTrue( result.contains("<choices>alfa,beta,gamma</choices>"));
	}

	@Test 
	public void testChoicesFromXml() {
		String xml = 
			"<field > " +
				"<choices> alfa, beta, gamma </choices>" +
				"<readonly>beta</readonly>" +
				"<validation required='true' />" +
			"</field>";
		
		Field field = XStreamHelper.fromXML(xml);
		assertNotNull(field);
		assertArrayEquals(new String[]{"alfa","beta","gamma"}, field.choices);
		assertNotNull(field.validation);
		assertEquals(field.readOnly, "beta");
		assertTrue(field.validation.required);
	}
	
}

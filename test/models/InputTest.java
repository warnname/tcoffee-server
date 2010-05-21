package models;

import java.util.ArrayList;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class InputTest extends UnitTest {

	
	@Test 
	public void testBasicFieldset() {
		Input input = new Input();
		input.fieldsets = new ArrayList<Fieldset>();
		
		Fieldset f1 = new Fieldset();
		f1.hideable = false;
		
		Fieldset f2 = new Fieldset();
		f2.hideable = true;
		
		input.fieldsets.add(f1);
		input.fieldsets.add(f2);
		
		
		assertTrue( input.getBasics().contains(f1) );
		assertFalse( input.getBasics().contains(f2) );

		assertFalse( input.getHideables().contains(f1) );
		assertTrue( input.getHideables().contains(f2) );
	}
	
	@Test 
	public void testFieldText() {
		
		String xml = 
			"<input >" +
				"<fieldset><title>title1</title></fieldset>" + 
				"<fieldset hideable='true' ><title>title2</title></fieldset>" +
			"</input>";
		
		Input input = XStreamHelper.fromXML(xml);
		assertNotNull(input);
		
		assertEquals("title1", input.fieldsets.get(0).title);
		assertEquals(false, input.fieldsets.get(0).hideable);

		assertEquals("title2", input.fieldsets.get(1).title);
		assertEquals(true, input.fieldsets.get(1).hideable);
		
	
	} 	
	
}

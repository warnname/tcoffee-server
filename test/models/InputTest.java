package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
	
	@Test 
	public void testSave() throws IOException {
		Input input = new Input();
		input.fieldsets = new ArrayList<Fieldset>();
		
		Fieldset f;
		input.fieldsets.add( f = new Fieldset() );
		
		f.add( new Field("text", "x", "1"), new Field("text", "y", "2") );
		File file = File.createTempFile("tcoffee", ".test");
		input.save(file);
		List<String> result = FileUtils.readLines(file);
		
		Iterator<String> itr = result.iterator();
		assertEquals( "<input>", itr.next().trim() );
		assertEquals( "<fieldset hideable=\"false\">", itr.next().trim() );
		assertEquals( "<field type=\"text\" name=\"x\">", itr.next().trim() );
		assertEquals( "<value>1</value>", itr.next().trim() );
		assertEquals( "</field>", itr.next().trim() );
		assertEquals( "<field type=\"text\" name=\"y\">", itr.next().trim() );
		assertEquals( "<value>2</value>", itr.next().trim() );    
		assertEquals( "</field>", itr.next().trim() );
		assertEquals( "</fieldset>", itr.next().trim() );
		assertEquals( "</input>", itr.next().trim() );
		file.delete();
		
	} 
	
}

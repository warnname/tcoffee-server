package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import play.data.validation.Validation;
import play.test.UnitTest;
import util.Utils;
import util.XStreamHelper;
import bundle.BundleScriptLoader;

public class InputTest extends UnitTest {

		
	@Test 
	public void testFieldText() {
		
		String xml = 
			"<input >" +
				"<fieldset><title>title1</title></fieldset>" + 
				"<fieldset hideable='true' ><title>title2</title></fieldset>" +
				"<validation-script file='/some/file'> .. validation rule .. </validation-script> " +
			"</input>";
		
		Input input = XStreamHelper.fromXML(xml);
		assertNotNull(input);
		
		assertEquals("title1", input.fieldsets.get(0).title);
		assertEquals(false, input.fieldsets.get(0).hideable);

		assertEquals("title2", input.fieldsets.get(1).title);
		assertEquals(true, input.fieldsets.get(1).hideable);
		
		assertEquals( "/some/file", input.validation.file );
		assertEquals( " .. validation rule .. ", input.validation.text );
	
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
	
	
	@Test 
	public void testCreate() {
		
		Input in =  Input.create("alpha=1", "beta=2", "gamma:memo=some text");
		assertEquals( "1", in.field("alpha").value );
		assertEquals( "text", in.field("alpha").type );

		assertEquals( "2", in.field("beta").value );
		assertEquals( "text", in.field("beta").type );
	
		assertEquals( "some text", in.field("gamma").value );
		assertEquals( "memo", in.field("gamma").type );
		
		assertEquals( 3, in.fields().size() );
		assertEquals( 1, in.fieldsets.size() );
	
	}
	
	@Test 
	public void testCopy() {
		
		Input in = Input.create("a=uno","b=due", "c:memo=long text");
		in.validation = new Script().setFile("/some.file").setText("doThat()");
		
		Input copy = Utils.copy(in);
		
		assertEquals( "uno", copy.field("a").value );
		assertEquals( "due", copy.field("b").value );
		assertEquals( "long text", copy.field("c").value );
		assertEquals( "memo", copy.field("c").type );
		
		assertEquals( "/some.file", copy.validation.file );
		assertEquals( "doThat()", copy.validation.text);
		
	} 
	
	@Test 
	public void testValidationFail() {
		Validation.clear();

		Input in = Input.create("password=uno","pwdcheck=due");
		in.validation = new Script(new BundleScriptLoader());
		in.validation.text 
			= "if( input.field('password').value != input.field('pwdcheck').value ) 'Password check does not match' ";
		in.validate();
		
		assertTrue(Validation.hasError("_input_form"));
		assertEquals( "Password check does not match", Validation.error("_input_form").message());
	
		
		
	} 
	

	@Test 
	public void testValidationOK() {
		Validation.clear();
		Input in = Input.create("password=blah","pwdcheck=blah");
		in.validation = new Script(new BundleScriptLoader());
		in.validation.text 
			= "if( input.field('password').value != input.field('pwdcheck').value ) 'Password check does not match' ";
		in.validate();
		
		assertFalse(Validation.hasError("_input_form"));
	
		
		
	} 

}

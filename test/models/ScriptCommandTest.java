package models;

import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.XStreamHelper;
import exception.CommandException;

public class ScriptCommandTest extends UnitTest{

	@Test
	public void fromXML () {
		String xml = 
			"<script file='Script.groovy' clazz='org.com.Hola'>" +
			"1 + 2" +
			"</script>"; 
		
		ScriptCommand script = XStreamHelper.fromXML(xml);
		assertEquals("1 + 2", script.fScriptText);
		assertEquals("Script.groovy", script.fFile);
		assertEquals("org.com.Hola", script.fClass);
		
		
	}

	@Test
	public void toXML () {
		ScriptCommand cmd = new ScriptCommand();
		cmd.fScriptText = "print";
		cmd.fClass = "org.dummy.Class";
		cmd.fFile = "SomeFile.groovy";

		String xml = XStreamHelper.toXML(cmd);
		assertEquals("<script file=\"SomeFile.groovy\" clazz=\"org.dummy.Class\">print</script>", xml);
		
	}
	
	@Test 
	public void testScriptText() throws CommandException {
		ContextHolder context = new ContextHolder();
		context.input = Input.create("alpha=1");

		
		ScriptCommand cmd = new ScriptCommand();
		cmd.fScriptText = 
				"assert input.field('alpha').value == '1'";
		cmd.init(context);
		
		cmd.execute();
		
	} 
	
	@Test 
	public void testBundle() throws CommandException {
		Bundle bundle = TestHelper.bundle();
		Service service = bundle.services.get(0);		
		Service.current(service);
		service.init();
		
		ScriptCommand cmd = new ScriptCommand();
		cmd.fScriptText = 
				"assert context['bundle.version'] == '1.0' \n" +
				"assert context['bundle.name'] == 'test-bundle' \n" +
				"assert context['bundle.path'] != null \n" +
				"assert context['bundle.bin.path'] != null\n " + 
				"assert context['bundle.script.path'] != null\n " + 
				"assert context['workspace.path'] != null \n" +
				"assert context['data.path'] != null \n" +
				"assert context['_rid'] == '" + service.rid() + "' \n" +
				"assert context['_result_url'] != null \n" + 
				"assert context['simple.prop'] == 'true' \n" +

				"assert context._rid == '" + service.rid() + "' \n" +
				"assert context._result_url != null \n" +
				"" +
				"def file = new File(context['data.path'], 'test.file')\n" +
				"file << 'Some content' \n" +
				
				"result.addWarning('Do not do that') \n" + 
				"result.add( new models.OutItem(file, 'txt') ) \n"; 
	
		
		
				; 
		cmd.init();
		
		cmd.execute();
		
		OutResult result = service.getContextHolder().result;
		assertEquals( 1, result.getItems().size());
		assertEquals( "Do not do that", result.warnings.get(0));
	} 


}

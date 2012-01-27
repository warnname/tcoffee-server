package models;

import java.io.File;

import org.junit.Test;

import play.libs.IO;
import play.test.UnitTest;
import util.Utils;
import util.XStreamHelper;
import bundle.BundleScriptLoader;

public class ScriptTest extends UnitTest {

	@Test 
	public void testCopy() {
		
		Script script = new Script();
		script.file = "/there";
		script.text = "1 + 2";
		
		assertEquals( script, Utils.copy(script) );
		
	}  
	
	@Test 
	public void testToXml() {
		
		Script script = new Script();
		script.file = "/there";
		script.text = "1 + 2";

		String exptected = 
				"<script file=\"/there\">" +
				"1 + 2" +
				"</script>";
		
		String result = XStreamHelper.toXML(script);
		
		assertEquals(exptected, result);
		
	} 
	
	@Test
	public void testText() {
		Script script = new Script(new BundleScriptLoader()).setText("assert \"hola world\" == \"hola $x\"; z=99; return 'ok';"); 
		script.setProperty("x", "world");
		script.run();
		
		assertEquals(new Integer(99), script.getProperty("z"));
		assertEquals("ok", script.getResult());
	} 

	@Test
	public void testFile() {

		String scriptText = 
				"assert \"hola world\" == \"hola $x\" \n" +
				"z=99 \n" +
				"return 'ok' \n";
		
		File file;
		IO.writeContent(scriptText, file = new File("./TestScript.groovy"));
		
		Script script = new Script(new BundleScriptLoader()).setFile("./TestScript.groovy"); 
		script.setProperty("x", "world");
		script.run();
		
		assertEquals(new Integer(99), script.getProperty("z"));
		assertEquals("ok", script.getResult());
		
		file.delete();
		
		
	} 
	
}

package models;

import org.junit.Before;
import org.junit.Test;

import play.libs.IO;
import play.test.UnitTest;
import util.TestHelper;
import util.XStreamHelper;
import exception.CommandException;

public class GenericCommandTest extends UnitTest {
	
	@Before
	public void init() {
		TestHelper.init("field1=value 1", "field2=value 2");
	}
	

	@Test 
	public void testGenericCommand() {
		
		String xml = 
			"<exec>" +
				"<logfile>out.txt</logfile> " +
				"<errfile>err.txt</errfile> " +
				"<envfile>env.txt</envfile> " +
				"<cmdfile>cmd.txt</cmdfile> " +
				"<validCode>1</validCode> " +
			
				"<env var1='${field1}' var2='dos' var3='tres' />" + 

				"<cmd>job.sh -arg=${field2}</cmd>" +
			"</exec>";

		
		GenericCommand cmd = XStreamHelper.fromXML(xml);
		assertNotNull(cmd);
		assertEquals("out.txt", cmd.logfile);
		assertEquals("err.txt", cmd.errfile);
		assertEquals("env.txt", cmd.envfile);
		assertEquals("cmd.txt", cmd.cmdfile);
		assertEquals(1, cmd.validCode );
		assertEquals("job.sh -arg=value 2", cmd.cmd.eval());
		
	}
	
	@Test 
	public void testExecuteOK() throws Exception {
		GenericCommand cmd = new GenericCommand();
		cmd.cmd = new Eval("ls -la");
		cmd.logfile = "outfile.txt";
		cmd.errfile = "errfile.txt";
		cmd.cmdfile = "cmdfile.txt";
		cmd.envfile = "envfile.txt";
		
		cmd.init();
		boolean result = cmd.execute();
		
		assertTrue(result);
		assertTrue(cmd.getCmdFile().exists());
		assertTrue(cmd.getLogFile().exists());
		assertTrue(cmd.getErrFile().exists());
		assertTrue(cmd.getEnvFile().exists());
		
		String cmdline = IO.readContentAsString(cmd.getCmdFile());
		assertEquals("ls -la", cmdline.trim());
		
		assertTrue( cmd.getResult().getStdout().exists() );
	} 

	@Test 
	public void testExecuteFail() {
		GenericCommand cmd = new GenericCommand();
		cmd.cmd = new Eval("xxx");
		cmd.init();
		boolean result;
		try {
			result = cmd.execute();
		} catch (CommandException e) {
			result = false;
		}
		
		assertFalse(result);
	} 
	
//	@Test 
//	public void testSetPath() {
//		
//		Runtime runtime = Runtime.getRuntime();
//
//		boolean result=false;
//		File local = Module.current().folder();
//		try {
//			IO.writeContent("export PATH=/Users/ptommaso/tools/play-1.0.1/tcoffee/osx\npoa", new File(local,"runner.sh"));
//			Process process = runtime.exec("bash runner.sh", null, local);
//			FileOutputStream out = new FileOutputStream(new File(local,"result.txt"));
//			IO.write(process.getErrorStream(), out);
//			result = 0==process.waitFor();
//			
//			out.close();
//		}
//		catch( Exception e ) {
//			e.printStackTrace();
//			fail();
//		}
//		
//		assertTrue(result);
// 	} 
	
}

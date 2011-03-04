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
		TestHelper.init("field1=uno", "field2=dos");
	}
	

	@Test 
	public void testGenericCommand() {
		
		String xml = 
			"<exec program='job.sh' >" +
			    "<args>-arg=${field2}</args>" +
				"<logfile>out.txt</logfile> " +
				"<errfile>err.txt</errfile> " +
				"<envfile>env.txt</envfile> " +
				"<cmdfile>cmd.txt</cmdfile> " +
				"<validCode>1</validCode> " +
			
				"<env var1='${field1}' var2='dos' var3='tres' />" + 
			"</exec>";

		
		GenericCommand cmd = XStreamHelper.fromXML(xml);
		assertNotNull(cmd);
		assertEquals("out.txt", cmd.logfile);
		assertEquals("err.txt", cmd.errfile);
		assertEquals("env.txt", cmd.envfile);
		assertEquals("cmd.txt", cmd.cmdfile);
		assertEquals(1, cmd.validCode );
		assertEquals("job.sh", cmd.program.eval());
		assertEquals("-arg=dos", cmd.args.toCmdLine());
		
	}
	
	@Test 
	public void testExecuteOK() throws Exception {
		GenericCommand cmd = new GenericCommand();
		cmd.program = new Eval("ls");
		cmd.args = new CmdArgs("-la");
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
		cmd.program = new Eval("xxx");
		cmd.init();
		boolean result;
		try {
			result = cmd.execute();
		} catch (CommandException e) {
			result = false;
		}
		
		assertFalse(result);
	} 
	
	@Test
	public void testConstructorWithProgram() { 
		GenericCommand cmd = new GenericCommand("ls");
		assertEquals( "ls", cmd.program.eval() );
		assertEquals( null, cmd.args );
		
		cmd = new GenericCommand("ls -la");
		assertEquals( "ls", cmd.program.eval() );
		assertEquals( "-la", cmd.args.toCmdLine() );
		
	}
	
	@Test
	public void testProgramWithFlagArgument() { 
		GenericCommand cmd = new GenericCommand("protogene --something=${field1} --flag=${missing}");
		cmd.init();
		
		assertEquals( "protogene --something=uno", cmd.getCmdLine() );
	}
	

}

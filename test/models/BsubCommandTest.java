package models;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import play.libs.IO;
import play.test.UnitTest;
import util.TestHelper;
import util.XStreamHelper;
import exception.CommandException;

public class BsubCommandTest extends UnitTest {
	
	@Before
	public void register() {
		TestHelper.init("output=html");
	}
	
	@Test 
	public void testCmdLine() {
		String xml = 
			"<bsub>" +
				"<cmdfile>cmd.txt</cmdfile>" +
				"<envfile>env.txt</envfile>" +
				"<logfile>log.txt</logfile>" +
				"<errfile>err.txt</errfile>" +

				"<tcoffee>" +
					"<args>-in=sample.fasta -mode=expresso</args>" +
				"</tcoffee>" +
			"</bsub>";
		
		BsubCommand bsub = XStreamHelper.fromXML(xml);
		
		assertNotNull(bsub);
		assertEquals( "cmd.txt", bsub.cmdfile ); 
		assertEquals( "log.txt", bsub.logfile ); 
		assertEquals( "err.txt", bsub.errfile ); 
		assertEquals( "env.txt", bsub.envfile ); 
		assertEquals( 1, bsub._commands.size() );
	} 	
	

	@Test 
	public void testBsubCommandLine() { 
	
		CmdArgs args = new CmdArgs();
		args.put("mode", "regular");
		args.put("in", "sample.fasta" );
		
		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.errfile = "err.log";
		tcoffee.logfile = "out.log";
		tcoffee.cmdfile = "cmd.log";
		tcoffee.envfile = "env.log";
		tcoffee.args = args;
		
		BsubCommand bsub = new BsubCommand();
		bsub._commands = new ArrayList<AbstractShellCommand>();
		bsub._commands.add(tcoffee);
		bsub.queue = "queuename";
		bsub.jobname = "jobname";
		bsub.errfile = "bsub.err.log";
		bsub.logfile = "bsub.out.log";
		bsub.cmdfile = "bsub.cmd.txt";
		bsub.envfile = "bsub.env.txt";
		bsub.disabled = false;
		bsub.init();
		
		
		final String cmd = String.format("bsub -K -cwd %s -q queuename -J jobname -o out.log -e err.log < cmd.log", bsub.ctxfolder);
		assertEquals( cmd, bsub.getCmdLine() );
		
	}

	
	@Test
	public void testParseResultOK() throws CommandException { 
		

		CmdArgs args = new CmdArgs();
		args.put("mode", "regular");
		args.put("in", "sample.fasta" );
		
		final TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.errfile = "err.log";
		tcoffee.logfile = "out.log";
		tcoffee.cmdfile = "cmd.log";
		tcoffee.envfile = "env.log";
		tcoffee.args = args;
		
		
		BsubCommand bsub = new BsubCommand() {
			public boolean run() throws CommandException {

				/* 
				 * mock a sample t_coffee output
				 */
				TestHelper.copy( TestHelper.sampleLog(), tcoffee.getLogFile() );
				IO.writeContent("<html> blah blah </html>", new File(tcoffee.ctxfolder, "tcoffee.score_html"));
				
				/* 
				 * MOCK the bsub out file 
				 */
				final String RESULT = 	
					"Job <12345> is submitted to default queue <normal>.\n" +
					"<<Waiting for dispatch ...>>\n" +
					"<<Job is finished>>";
				
	
				IO.writeContent(RESULT, getLogFile());
				return true;
			};
		}; 
		bsub._commands = new ArrayList<AbstractShellCommand>();
		bsub._commands.add(tcoffee);
		bsub.queue = "queuename";
		bsub.errfile = "bsub.err.log";
		bsub.logfile = "bsub.out.log";
		bsub.cmdfile = "bsub.cmd.txt";
		bsub.envfile = "bsub.env.txt";
		bsub.disabled = false;
		bsub.init();
		
		bsub.execute();

		assertTrue(bsub.isOK());
		assertEquals( "12345", bsub.getJobId() );
		
		
	}
	

	
	@Test 
	public void testDisabledProperty() { 
		BsubCommand bsub = new BsubCommand();
		assertTrue( bsub.disabled ); // disabled in DEV mode by default
		
		AppProps.instance().setProperty("bsub.disabled", "false");
		bsub = new BsubCommand();
		assertFalse( bsub.disabled ); 

		AppProps.instance().setProperty("bsub.disabled", "true");
		bsub = new BsubCommand();
		assertTrue( bsub.disabled ); 
		
	}
	
}

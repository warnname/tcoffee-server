package models;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import play.libs.IO;
import play.test.UnitTest;
import util.TestHelper;
import util.Utils;
import util.XStreamHelper;
import exception.CommandException;

public class QsubCommandTest extends UnitTest {
	
	@Before
	public void register() {
		TestHelper.init("output=html pdf clustal");
	}
	
	@Test 
	public void testCmdLine() {
		String xml = 
			"<qsub>" +
				"<cmdfile>cmd.txt</cmdfile>" +
				"<envfile>env.txt</envfile>" +
				"<logfile>log.txt</logfile>" +
				"<errfile>err.txt</errfile>" +

				"<env BLASTMAT=\"/path\" />" +

				"<tcoffee>" +
					"<env BLASTMAT=\"/path\" />" +
				"</tcoffee>" +
			"</qsub>";
		
		QsubCommand qsub = XStreamHelper.fromXML(xml);
		
		assertNotNull(qsub);
		assertEquals( "cmd.txt", qsub.cmdfile ); 
		assertEquals( "log.txt", qsub.logfile ); 
		assertEquals( "err.txt", qsub.errfile ); 
		assertEquals( "env.txt", qsub.envfile ); 
		assertEquals( 1, qsub._commands.size() );
		
		//assertTrue( Utils.isEquals(Arrays.asList(new Env()), c2))
	} 	
	

//	@Test 
	public void testExecute() throws CommandException {
		File source = TestHelper.sampleFasta();
		TestHelper.copy(source, new File(Service.current().folder(), "sample.fasta"));		
		
		CmdArgs args = new CmdArgs();
		args.put("mode", "regular");
		args.put("in", "sample.fasta" );
		
		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.errfile = "err.log";
		tcoffee.logfile = "out.log";
		tcoffee.cmdfile = "cmd.log";
		tcoffee.envfile = "env.log";
		tcoffee.args = args;
		
		
		QsubCommand qsub = new QsubCommand();
		qsub.disabled = false;
		qsub._commands = new ArrayList<AbstractShellCommand>();
		qsub._commands.add(tcoffee);
		qsub.queue = "ws_cn@palestine";
		qsub.errfile = ".qsub.err.log";
		qsub.logfile = ".qsub.out.log";
		qsub.cmdfile = ".qsub.cmd.txt";
		qsub.envfile = ".qsub.env.txt";
		qsub.init();
		
		// check tcoffee
		assertTrue(tcoffee.getCmdLine().contains("-in=sample.fasta"));
		assertTrue(tcoffee.getCmdLine().contains("-mode=regular"));
		assertEquals("cmd.log", tcoffee.getCmdFile().getName());
		assertEquals("env.log", tcoffee.getEnvFile().getName());
		assertEquals("out.log", tcoffee.getLogFile().getName());
		assertEquals("err.log", tcoffee.getErrFile().getName());

		String testCmd = String.format("qsub -cwd -sync y -now y -r no -terse -q %s -o %s -e %s -N %s %s", 
					qsub.getQueue(),
					tcoffee.logfile,
					tcoffee.errfile,
					qsub.getJobName(),
					qsub.getJobFile().getName()
					);
		assertEquals(testCmd, qsub.getCmdLine());
		
		
		qsub.execute();
		assertTrue(qsub.isOK());
		assertTrue(qsub.getLogFile().exists());
		assertTrue(qsub.getErrFile().exists());
		assertTrue(qsub.getCmdFile().exists());
		assertTrue(qsub.getEnvFile().exists());
		assertTrue(qsub.getJobFile().exists());

		assertTrue(tcoffee.getLogFile().exists());
		assertTrue(tcoffee.getErrFile().exists());
		assertTrue(tcoffee.getCmdFile().exists());
		
		assertTrue(qsub.getResult().getAlignmentHtml().file.exists());
		assertTrue(qsub.getJobId().length()>0);
		
	} 	
	
	@Test
	public void testQsubCouldNotScheduleError() throws CommandException {

		CmdArgs args = new CmdArgs();
		args.put("mode", "regular");
		args.put("in", "sample.fasta" );
		
		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.errfile = "err.log";
		tcoffee.logfile = "out.log";
		tcoffee.cmdfile = "cmd.log";
		tcoffee.envfile = "env.log";
		tcoffee.args = args;
		
		
		QsubCommand qsub = new QsubCommand() {
			public boolean run() throws CommandException {

				IO.writeContent("t-coffee rulez", _commands.get(0).getLogFile());
				
				/* write the qsub out file */
				IO.writeContent("12345", getLogFile());
				
				/* write the qsub error file */
				IO.writeContent(
						"Waiting for immediate job to be scheduled.\n\n" +
						"Your qsub request could not be scheduled, try again later.", getErrFile());
				return true;
			};
		}; 
		qsub._commands = new ArrayList<AbstractShellCommand>();
		qsub._commands.add(tcoffee);
		qsub.queue = "ws_cn@palestine";
		qsub.errfile = "qsub.err.log";
		qsub.logfile = "qsub.out.log";
		qsub.cmdfile = "qsub.cmd.txt";
		qsub.envfile = "qsub.env.txt";
		qsub.disabled = false;
		qsub.init();
		
		qsub.execute();

		assertFalse(qsub.isOK());
		assertEquals( 1, qsub.result.errors.size() );
		assertEquals( "Waiting for immediate job to be scheduled.\n\nYour qsub request could not be scheduled, try again later.", qsub.result.errors.get(0) );
		
	} 
	
	@Test 
	public void testDisabledProperty() { 
		QsubCommand qsub = new QsubCommand();
		assertTrue( qsub.disabled ); // disabled in DEV mode by default
		
		AppProps.instance().setProperty("qsub.disabled", "false");
		qsub = new QsubCommand();
		assertFalse( qsub.disabled ); 

		AppProps.instance().setProperty("qsub.disabled", "true");
		qsub = new QsubCommand();
		assertTrue( qsub.disabled ); 
		
	}
	
	@Test 
	public void testWrapperProperty() { 
		AppProps.instance().remove("qsub.wrapper");
		QsubCommand qsub = new QsubCommand();
		assertTrue( Utils.isEmpty(qsub.wrapper) );

		qsub = new QsubCommand();
		AppProps.instance().setProperty("qsub.wrapper", "time -v %s");
		assertEquals( "time -v %s", qsub.wrapper );
	}
	
	@Test
	public void testWrapper() { 
		CmdArgs args = new CmdArgs();
		args.put("mode", "regular");
		args.put("in", "sample.fasta" );
		
		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.errfile = "err.log";
		tcoffee.logfile = "out.log";
		tcoffee.cmdfile = "cmd.log";
		tcoffee.envfile = "env.log";
		tcoffee.args = args;
		
		
		QsubCommand qsub = new QsubCommand();
		qsub._commands = new ArrayList<AbstractShellCommand>();
		qsub._commands.add(tcoffee);
		qsub.disabled = false;
		qsub.queue = "nada";
		qsub.jobfile = "run.txt";
		qsub.wrapper = "time -v %s";  // <-- 'wrap' the tcoffee command with the time command
		qsub.init();
		
		String run = IO.readContentAsString(qsub.getJobFile());
		assertTrue( run.contains("time -v " + tcoffee.getCmdLine()) );
		
	}
	
}

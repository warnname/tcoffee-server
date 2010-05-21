package models;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.XStreamHelper;
import exception.CommandException;

public class QsubCommandTest extends UnitTest {
	
	@Before
	public void register() {
		TestHelper.module("output=html pdf clustal");
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
		TestHelper.copy(source, new File(Module.current().folder(), "sample.fasta"));		
		
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
		qsub.queue = "ws_cn";
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
					qsub.getPbsFile().getName()
					);
		assertEquals(testCmd, qsub.getCmdLine());
		
		
		qsub.execute();
		assertTrue(qsub.isOK());
		assertTrue(qsub.getLogFile().exists());
		assertTrue(qsub.getErrFile().exists());
		assertTrue(qsub.getCmdFile().exists());
		assertTrue(qsub.getEnvFile().exists());
		assertTrue(qsub.getPbsFile().exists());

		assertTrue(tcoffee.getLogFile().exists());
		assertTrue(tcoffee.getErrFile().exists());
		assertTrue(tcoffee.getCmdFile().exists());
		
		assertTrue(qsub.getResult().getAlignmentHtml().file.exists());
		assertTrue(qsub.getJobId().length()>0);
		
	} 	
	
	
}

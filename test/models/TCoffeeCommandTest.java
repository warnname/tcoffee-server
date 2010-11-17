package models;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.libs.IO;
import play.test.UnitTest;
import util.TcoffeeHelperTest;
import util.TestHelper;
import util.XStreamHelper;
import exception.CommandException;

public class TCoffeeCommandTest extends UnitTest {

	@Before
	public void register() {
		TestHelper.init("output=html pdf clustal");
	}
	
	
	@Test 
	public void testFromXml() {
		String xml = 
			"<tcoffee>" +
				"<env name='alfa' value='beta' />" +
				"<args>-in=input.txt -mode=regular -output=${output}</args>" +
			"</tcoffee>";
		
		TCoffeeCommand tcoffee = XStreamHelper.fromXML(xml);
		tcoffee.init ( new CommandCtx(Service.current().fCtx) );
		String cmd = tcoffee.getCmdLine();
		
		assertEquals( "t_coffee -in=input.txt -mode=regular -output=html pdf clustal -quiet=stdout", cmd );
	} 

	@Test
	public void testParseResultItem() {
		String TEST = "	#### File Type=        MSA Format= clustalw_aln Name= tcoffee.clustalw_aln";
		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.init ( new CommandCtx(Service.current().fCtx) );
		OutItem item = tcoffee.parseForResultItem(TEST);
		
		assertNotNull(item);
		assertEquals( "MSA", item.type );
		assertEquals( "clustalw_aln", item.format );
		assertEquals( "tcoffee.clustalw_aln", item.name );
		
		
		assertNull(tcoffee.parseForResultItem("#### Invalid format"));
	}
	
	@Test 
	public void testParseWarning() { 
		String TEST = "41002 -- WARNING: Blast for A_thaliana_At5g60730 failed (Run: 1)";
		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.init ( new CommandCtx(Service.current().fCtx) );
		String warn = tcoffee.parseForWarning(TEST);
		
		assertNotNull(warn);
		assertEquals( "Blast for A_thaliana_At5g60730 failed (Run: 1)", warn );
		
		assertNull(tcoffee.parseForWarning("xxx"));
		
	}
	
	@Test 
	public void testParseResult() {
		File log = TestHelper.sampleLog();
		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.init ( new CommandCtx(Service.current().fCtx) );

		List<OutItem> result = tcoffee.parseResultFile(log);
		assertNotNull(result);
		assertEquals(3, result.size());
		
		assertEquals("GUIDE_TREE", result.get(0).type );
		assertEquals("newick", result.get(0).format );
		assertEquals("tcoffee.dnd", result.get(0).name );

		assertEquals("MSA", result.get(1).type );
		assertEquals("clustalw_aln", result.get(1).format );
		assertEquals("tcoffee.clustalw_aln", result.get(1).name );

		assertEquals("MSA", result.get(2).type );
		assertEquals("score_html", result.get(2).format );
		assertEquals("tcoffee.score_html", result.get(2).name );
		
	}
	
	@Test
	public void testParseResultWarning() { 
		/*
		 * the sample log contrains some warnings, 
		 * we assert that the result object will contain them
		 */
		File log = TestHelper.sampleLog();
		TestHelper.copy(log, Service.current().folder());

		TCoffeeCommand tcoffee = new TCoffeeCommand();
		tcoffee.logfile = log.getName();
		tcoffee.init ( new CommandCtx(Service.current().fCtx) );
	
		tcoffee.done(true);
		OutResult result = tcoffee.getResult();
		
		assertTrue( result.hasWarnings() );
		String w0 = "_P_  Template |3IBGF| Could Not be Used for Sequence |G_sulphuraria_Gs08590|: Coverage too low [40, Min=50]";
		String w1 = "_P_  Template |3IQXB| Could Not be Used for Sequence |C_merolae_CMER_CMP235C|: Coverage too low [31, Min=50]";
		String w2 = "SAP failed to align: 1II9B.pdb against 1II9B.pdb [T-COFFEE:WARNING]";
		
		assertTrue( result.warnings != null );
		assertEquals( w0, result.warnings.get(0) );
		assertEquals( w1, result.warnings.get(1) );
		assertEquals( w2, result.warnings.get(2) );
		
		XStreamHelper.toXML(result, new File( Service.current().folder(), "_result"));
		
	}

	@Test
	public void testRun() throws IOException, CommandException {
		File source = TestHelper.sampleFasta();
		TestHelper.copy(source, new File(Service.current().folder(), "sample.fasta"));
		
		CmdArgs args = new CmdArgs();
		args.put("mode", "regular");
		args.put("in", "sample.fasta" );
		args.put("output", "score_html fasta_aln");
		args.put("run_name", "sample");
		
		TCoffeeCommand tcoffee = new TCoffeeCommand();

		tcoffee.errfile = "err.log";
		tcoffee.logfile = "out.log";
		tcoffee.cmdfile = "cmd.log";
		tcoffee.envfile = "env.log";
		tcoffee.args = args;

		tcoffee.init ( new CommandCtx(Service.current().fCtx) );

		boolean ok = tcoffee.execute();
		
		assertTrue( ok );
		assertTrue( tcoffee.getLogFile().exists() );
		assertTrue( tcoffee.getErrFile().exists() );
		assertTrue( tcoffee.getEnvFile().exists() );
		
		assertTrue( tcoffee.getCmdFile().exists() );
		assertEquals( tcoffee.getCmdLine(), IO.readContentAsString(tcoffee.getCmdFile()).trim() );
		
		OutResult result = tcoffee.getResult();
		assertNotNull( result.first("name", "out.log" ));
		assertNotNull( result.first("name", "sample.score_html" ));
		assertNotNull( result.first("name", "sample.dnd" ));
		assertNotNull( result.first("name", "sample.fasta_aln"));

		assertNotNull( result.getAlignmentHtml() );
		assertNotNull( result.getAlignmentFasta() );
		
		/* assert that the html is in the expected format */
		TcoffeeHelperTest.parseHtmlFile(result.getAlignmentHtml().file);
	}
	
	
}

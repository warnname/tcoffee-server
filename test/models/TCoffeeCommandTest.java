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
		
		/* assert that the html is the exècted form */
		TcoffeeHelperTest.parseHtmlFile(result.getAlignmentHtml().file);
	}
	
	
}

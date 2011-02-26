package util;

import java.io.File;

import org.junit.Test;

import play.test.UnitTest;
import util.TcoffeeHelper.ResultHtml;

public class TcoffeeHelperTest extends UnitTest { 

	@Test 
	public void testParseHtml() {
		parseHtmlFile( TestHelper.file("/sample-alignment.html") );
	}
	
	public static void parseHtmlFile( File file ) {
		String TEST_STYLE = "SPAN { font-family: courier new, courier-new, courier, monospace; font-weight: bold; font-size: 11pt;}";
		String TEST_BODY = "<span class=valuedefault>T-COFFEE,&nbsp;Version_8.98(Wed&nbsp;Jan&nbsp;12&nbsp;00:16:57&nbsp;CET&nbsp;2011&nbsp;-&nbsp;Revision&nbsp;528)</span><br><span class=valuedefault>Cedric&nbsp;Notredame&nbsp;</span><br><span class=valuedefault>SCORE=45</span><br>";

		ResultHtml result = TcoffeeHelper.parseHtml(file);
		assertEquals( TEST_STYLE,  result.style.trim() .substring(0,TEST_STYLE.length()) );
		assertEquals( TEST_BODY, result.body.trim() .substring(0,TEST_BODY.length()) );
	}
	
	@Test
	public void parseConsensus() { 
		
		TcoffeeHelper.parseConsensus(TestHelper.file("/sample-alignment.html"));
	}
	
}

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
		String TEST_BODY = "<p>&dash;&dash;a&dash;</p><span class=valuedefault>T&dash;COFFEE,&nbsp;";

		ResultHtml result = TcoffeeHelper.parseHtml(file);
		assertEquals( TEST_STYLE,  result.style.trim() .substring(0,TEST_STYLE.length()) );
		assertEquals( TEST_BODY, result.body.trim() .substring(0,TEST_BODY.length()) );
		
	}
	
}

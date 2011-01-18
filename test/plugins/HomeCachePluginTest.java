package plugins;

import org.dom4j.DocumentException;
import org.junit.Test;

import play.test.UnitTest;

public class HomeCachePluginTest extends UnitTest {


	@Test 
	public void testFixLinks() throws DocumentException { 
		
		String DOC = 
			"<body>" +
			"<a href='1.html' ></a>" +
			"<img src='2.jpg' />" +
			"<img src='http://3.com' />" +
			"<img src='/4.jpg' />" +
			"<style>p{ background: url(\"5.jpg\"); attr: url('/6.jpg'); more:url(7.jpg)  } </style>" +
			"</body>";
		


		String result = HomeCachePlugin.fixPaths(DOC);
		
		assertTrue( result.contains("href='/apps/tcoffee/1.html'") );
		assertTrue( result.contains("src='/apps/tcoffee/2.jpg'") );
		assertTrue( result.contains("src='http://3.com'") );
		assertTrue( result.contains("src='/4.jpg'") );
		assertTrue( result.contains("url(\"/apps/tcoffee/5.jpg\")") );
		assertTrue( result.contains("url('/6.jpg')") );
		assertTrue( result.contains("url(/apps/tcoffee/7.jpg)") );
	}
	

}

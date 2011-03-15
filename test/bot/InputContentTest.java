package bot;

import org.junit.Test;

import play.test.UnitTest;

public class InputContentTest extends UnitTest {

	@Test
	public void testParse( )  { 
		
		InputContent content = new InputContent("Some text");

		assertEquals( "Some text", content.raw );
		assertEquals( "Some text", content.sequences );
		assertEquals( null, content.mode );

	
	}

	
	@Test 
	public void testParseWithMode() { 
		InputContent content = new InputContent("[mode=tcoffee]\nSome text");

		assertEquals( "[mode=tcoffee]\nSome text", content.raw );
		assertEquals( "Some text", content.sequences );
		assertEquals( "tcoffee", content.mode );
		
	}
	
	@Test 
	public void test () { 

		assertEquals( "tcoffee", InputContent.parseMode("[mode tcoffee]") );
		assertEquals( "tcoffee", InputContent.parseMode("[mode=tcoffee]") );
		assertEquals( null, InputContent.parseMode("[mode-tcoffee]") );
		assertEquals( "mcoffee", InputContent.parseMode("[mode mcoffee]") );
		assertEquals( "rcoffee", InputContent.parseMode("[mode=rcoffee]") );
		assertEquals( null, InputContent.parseMode("") );
			
	}
	
}

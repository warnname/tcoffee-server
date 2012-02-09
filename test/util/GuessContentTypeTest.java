package util;

import org.junit.Test;

import play.test.UnitTest;

public class GuessContentTypeTest extends UnitTest {

	private GuessContentType guess;

	@Test
	public void testParse( ) { 
		
		guess = new GuessContentType();

		guess.parse("text/x-java; charset=us-ascii");
		assertEquals( "text/x-java", guess.getMimeType() );
		assertEquals( "us-ascii", guess.getCharset() );

		guess.parse("hola");
		assertEquals( "hola", guess.getMimeType() );
		assertEquals( null, guess.getCharset() );
	
		guess.parse("text/html; ");
		assertEquals( "text/html", guess.getMimeType() );
		assertEquals( null, guess.getCharset() );

	
		guess.parse("text/html; something");
		assertEquals( "text/html", guess.getMimeType() );
		assertEquals( "something", guess.getCharset() );
	}
	
	@Test 
	public void testKnown() {

		assertEquals( "text/plain", new GuessContentType("file.fasta").getMimeType() );
		assertEquals( "text/plain", new GuessContentType("file.tfa").getMimeType() );
		assertEquals( "text/plain", new GuessContentType("file.fa").getMimeType() );
		assertEquals( "text/plain", new GuessContentType("file.pdb").getMimeType() );

	} 
	
	@Test 
	public void testGuess() { 

		guess = new GuessContentType( TestHelper.file("/util/GuessContentTypeTest.java") );

		assertEquals( "text/x-java", guess.getMimeType() );
		assertEquals( "us-ascii", guess.getCharset() );
		assertTrue( guess.isText() );
		assertFalse( guess.isBinary() );
	}
	
}

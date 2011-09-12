package models;

import org.junit.Test;

import play.test.UnitTest;

public class AmpaCommandTest extends UnitTest{

	@Test
	public void testParseStretches( ) { 
		String TEST = 
				"Antimicrobial stretch found in 58 to 75\n" + 
				"Antimicrobial stretch found in 80 to 93\n" +
				"Antimicrobial stretch found in 95 to 110\n" +
				"# This protein has 4 bactericidal stretches\n" +
				"# This protein has a mean antimicrobial value of 0.22663125 ";
		
		String result = AmpaCommand.parseStretches(TEST);
		String expected = "[[58,75],[80,93],[95,110]]";
		assertEquals( expected, result );
	}
	
}

package models;

import models.AmpaCommand.ResultData;

import org.junit.Test;

import play.test.UnitTest;

public class AmpaCommandTest extends UnitTest{

	@Test
	public void testParseResult( ) { 
		String TEST = 
				"Antimicrobial stretch found in 58 to 75. Propensity value 0.214 (4 %) \n" + 
				"Antimicrobial stretch found in 80 to 93. Propensity value 0.233 (15 %) \n" +
				"Antimicrobial stretch found in 95 to 110. Propensity value 0.228 (11 %) \n" +
				"# This protein has 4 bactericidal stretches\n" +
				"# This protein has a mean antimicrobial value of 0.22663125 ";
		
		ResultData result = AmpaCommand.parseResult(TEST);
		String expected = 
				"[{\"from\":58,\"to\":75,\"propensity\":0.214,\"probability\":4}," +
				"{\"from\":80,\"to\":93,\"propensity\":0.233,\"probability\":15}," +
				"{\"from\":95,\"to\":110,\"propensity\":0.228,\"probability\":11}" +
				"]";
		assertEquals( expected, result.stretches );
		assertEquals( "0.22663125", result.mean );
	}
	
}

package models;

import models.AmpaCommand.ResultData;

import org.blackcoffee.commons.format.Fasta;
import org.junit.Test;

import play.test.UnitTest;

public class AmpaCommandTest extends UnitTest{

	@Test
	public void testParseResult( ) { 
		
		Fasta fasta = new Fasta();
		fasta.parse(">abc\nMVPKLFTSQICLLLLLGLMGVEGSLHARPPQFTRAQWFAIQHISLNPPRCTIAMRAINNYRWRCKNQNTFLRTTFANVVNVCGNQSIRCPHNRTLNNCHRSRFRVPLLHCDLINPGAQNISNCTYADRPGRRFYVVACDNRDPRDSPRYPVVPVHLDTTI");
		
		String TEST = 
				"Antimicrobial stretch found in 58 to 75. Propensity value 0.214 (4 %) \n" + 
				"Antimicrobial stretch found in 80 to 93. Propensity value 0.233 (15 %) \n" +
				"Antimicrobial stretch found in 95 to 110. Propensity value 0.228 (11 %) \n" +
				"# This protein has 4 bactericidal stretches\n" +
				"# This protein has a mean antimicrobial value of 0.22663125 ";
		
		ResultData result = AmpaCommand.parseResult(TEST, fasta.sequences.get(0));
		String expected = 
				"[{\"from\":58,\"to\":75,\"propensity\":0.214,\"probability\":4}," +
				"{\"from\":80,\"to\":93,\"propensity\":0.233,\"probability\":15}," +
				"{\"from\":95,\"to\":110,\"propensity\":0.228,\"probability\":11}" +
				"]";
		assertEquals( expected, result.stretches );
		assertEquals( "0.22663125", result.mean );
		
		assertEquals( "abc,1,58,75,0.214,4%,NNYRWRCKNQNTFLRTTF", result.tabular.get(0));
		assertEquals( "abc,2,80,93,0.233,15%,NVCGNQSIRCPHNR", result.tabular.get(1));
		assertEquals( "abc,3,95,110,0.228,11%,LNNCHRSRFRVPLLHC", result.tabular.get(2));
	}
	
}

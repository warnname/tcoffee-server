package models;

import org.junit.Test;

import util.TestHelper;

public class RCoffeeNoMethodTest extends RCoffeeTest {

	@Test(timeout=60000)
	public void testNoMethod() throws Exception { 

		new TestRunner("rcoffee-no-method")
		
		.input( TestHelper.file("/sample-dna.fasta.txt") )
	
		.args( 
				"in=sample-dna.fasta.txt",
				"special_mode=rcoffee", 
				"tree", 
				"method_limits=consan_pair 5 150",

				/* defaults */
			    "output=score_html clustalw_aln fasta_aln phylip",
				"case=upper",
				"seqnos=on",
				"outorder=input",
				"outfile=tcoffee",
				"cache=no",
				"remove_template_file=1"
			)
				
		.go();		
	}
	
	
}

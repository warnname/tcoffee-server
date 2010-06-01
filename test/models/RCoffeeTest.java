package models;

import util.TestHelper;
import exception.CommandException;

public abstract class RCoffeeTest extends XModeCoffeeTest {

	
	protected void rcoffee( String method ) throws CommandException {
		
		new TestRunner("rcoffee-" + method)
		
		.input( TestHelper.file("/sample-dna.fasta.txt") )
	
		.args( 
				"in=sample-dna.fasta.txt",
				"special_mode=rcoffee", 
				"tree", 
				"method_limits=consan_pair 5 150", 
				"method=" + method,

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

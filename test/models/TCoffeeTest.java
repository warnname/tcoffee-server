package models;

import util.TestHelper;
import exception.CommandException;


public abstract class TCoffeeTest extends XModeCoffeeTest {

	void testWithMethod(String mode, String  method) throws CommandException {
		
		new TestRunner("tcoffee-" + method)
		
		.input( TestHelper.file("/sample.fasta") )
	
		.args( 
				"in=sample.fasta",
				"mode=" + mode, 
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



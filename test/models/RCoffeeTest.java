package models;

import util.TestHelper;
import exception.CommandException;

public abstract class RCoffeeTest extends XModeCoffeeTest {

	
	protected void rcoffee( String method ) throws CommandException {
		testWithDefaults( 
				TestHelper.file("/sample-dna.fasta.txt"), 
				"rcoffee-" + method, 
				"special_mode=rcoffee", "tree", "method_limits=consan_pair 5 150", "method=" + method
				);
	}
	

}

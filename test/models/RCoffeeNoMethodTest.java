package models;

import org.junit.Test;

import util.TestHelper;

public class RCoffeeNoMethodTest extends RCoffeeTest {

	@Test(timeout=60000)
	public void testNoMethod() throws Exception { 

		testWithDefaults( TestHelper.file("/sample-dna.fasta.txt"), "rcoffee-no-method", "special_mode=rcoffee", "tree", "method_limits=consan_pair 5 150");

	}
	
	
}

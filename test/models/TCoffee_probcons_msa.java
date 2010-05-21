package models;

import org.junit.Test;

public class TCoffee_probcons_msa extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_probcons_msa() throws Exception {
		testWithMethod("regular", "probcons_msa");
	}
	
}

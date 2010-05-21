package models;

import org.junit.Test;

public class TCoffee_poa_msa extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_poa_msa() throws Exception {
		testWithMethod("regular", "poa_msa");
	}
		
}


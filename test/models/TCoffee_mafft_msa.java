package models;

import org.junit.Test;


public class TCoffee_mafft_msa extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_mafft_msa() throws Exception {
		testWithMethod("regular", "mafft_msa");
	}
}


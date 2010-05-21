package models;

import org.junit.Test;

public class TCoffee_pcma_msa extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_pcma_msa() throws Exception {
		testWithMethod("regular", "pcma_msa");
	}		
}

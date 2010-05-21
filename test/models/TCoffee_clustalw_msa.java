package models;

import org.junit.Test;

public class TCoffee_clustalw_msa extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_clustalw_msa() throws Exception {
		testWithMethod("regular", "clustalw_msa");
	}
	
}








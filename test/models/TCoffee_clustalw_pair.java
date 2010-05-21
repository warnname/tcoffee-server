package models;

import org.junit.Test;


public class TCoffee_clustalw_pair extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_clustalw_pair() throws Exception {
		testWithMethod("regular", "clustalw_pair");
	}
}

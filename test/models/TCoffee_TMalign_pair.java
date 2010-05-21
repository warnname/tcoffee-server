package models;

import org.junit.Test;


public class TCoffee_TMalign_pair extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_TMalign_pair() throws Exception {
		testWithMethod("regular", "TMalign_pair");
	}
	
}

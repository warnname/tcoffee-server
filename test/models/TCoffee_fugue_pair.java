package models;

import org.junit.Test;

public class TCoffee_fugue_pair extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_fugue_pair() throws Exception {
		testWithMethod("regular", "fugue_pair");
	}
	
}

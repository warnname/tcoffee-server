package models;

import org.junit.Test;

public class TCoffee_proba_pair extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_proba_pair() throws Exception {
		testWithMethod("regular", "proba_pair");
	}
}


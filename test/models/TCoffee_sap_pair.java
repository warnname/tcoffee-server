package models;

import org.junit.Test;

public class TCoffee_sap_pair extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_sap_pair() throws Exception {
		testWithMethod("regular", "sap_pair");
	}
}


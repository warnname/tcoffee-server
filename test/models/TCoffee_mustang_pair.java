package models;

import org.junit.Test;


public class TCoffee_mustang_pair extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_mustang_pair() throws Exception {
		testWithMethod("regular", "mustang_pair");
	}
	
}

package models;

import org.junit.Test;

public  class TCoffee_slow_pair extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_slow_pair() throws Exception {
		testWithMethod("regular", "slow_pair");
	}
}


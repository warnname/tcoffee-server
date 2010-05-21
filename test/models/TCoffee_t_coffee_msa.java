package models;

import org.junit.Test;


public class TCoffee_t_coffee_msa extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_t_coffee_msa() throws Exception {
		testWithMethod("regular", "t_coffee_msa");
	}		
}
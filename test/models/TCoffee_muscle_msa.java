package models;

import org.junit.Test;

public class TCoffee_muscle_msa extends TCoffeeTest {
	
	@Test(timeout=60000)
	public void testRegularWith_muscle_msa() throws Exception {
		testWithMethod("regular", "muscle_msa");
	}
}


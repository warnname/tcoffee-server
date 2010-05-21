package models;

import org.junit.Test;

import exception.CommandException;

public class RCoffee_mafft_msa extends RCoffeeTest {

	
	@Test(timeout=60000)
	public void testMethod_mafft_msa() throws CommandException {
		rcoffee("mafft_msa");
	}

}

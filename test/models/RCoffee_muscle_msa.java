package models;

import org.junit.Test;

import exception.CommandException;

public class RCoffee_muscle_msa extends RCoffeeTest {

	@Test(timeout=60000)
	public void testMethod_muscle_msa() throws CommandException {
		rcoffee("muscle_msa");
	}
	
}

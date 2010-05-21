package models;

import org.junit.Test;

import exception.CommandException;

public class RCoffee_laling_test extends RCoffeeTest {

	@Test(timeout=60000)
	public void testMethod_lalign_id_pair() throws CommandException {
		rcoffee("lalign_id_pair");
	}
	

	
}

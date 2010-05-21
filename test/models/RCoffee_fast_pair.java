package models;

import org.junit.Test;

import exception.CommandException;

public class RCoffee_fast_pair extends RCoffeeTest {

	@Test(timeout=60000)
	public void testMethod_fast_pair() throws CommandException {
		rcoffee("fast_pair");
	}
	

}

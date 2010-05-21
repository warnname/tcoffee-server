package models;

import org.junit.Test;

import exception.CommandException;

public class RCoffee_slow_pair extends RCoffeeTest {

	@Test(timeout=60000)
	public void testMethod_slow_pair() throws CommandException {
		rcoffee("slow_pair");
	}
	

	
}

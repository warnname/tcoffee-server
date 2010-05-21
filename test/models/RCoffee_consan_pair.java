package models;

import org.junit.Test;

import exception.CommandException;

public class RCoffee_consan_pair extends RCoffeeTest {

	@Test(timeout=60000)
	public void testMethod_consan_pair() throws CommandException {
		rcoffee("consan_pair");
	}
	

}

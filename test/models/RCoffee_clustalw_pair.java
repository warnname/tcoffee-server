package models;

import org.junit.Test;

import exception.CommandException;

public class RCoffee_clustalw_pair extends RCoffeeTest {

	@Test(timeout=60000)
	public void testMethod_clustalw_pair() throws CommandException {
		rcoffee("clustalw_pair");
	}

	
}

package models;

import org.junit.Test;

import exception.CommandException;

public class RCoffee_probaconsRna_msa extends RCoffeeTest {

	@Test(timeout=60000)
	public void testMethod_probconsRNA_msa() throws CommandException {
		rcoffee("probconsRNA_msa");
	}		
	
}

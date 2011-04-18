package models;

import org.junit.Test;

import play.test.UnitTest;

public class AmpaCommandTest extends UnitTest{

	@Test
	public void testParseLine( ) { 
		
		assertEquals( "4,0.239", AmpaCommand.parseLine("4	0.239") );
	}
	
}

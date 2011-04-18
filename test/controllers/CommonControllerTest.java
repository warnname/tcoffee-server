package controllers;

import org.junit.Test;

import play.mvc.results.Error;
import play.test.UnitTest;

public class CommonControllerTest extends UnitTest {

	@Test 
	public void testAssertEmpty() { 
		CommonController.assertEmpty((String)null);
		CommonController.assertEmpty("");
		
		try { 
			CommonController.assertEmpty("no-empty");
			fail();
		}
		catch( Error e ) { 
			// OK
		}
		
	}

	@Test 
	public void testAssertEmptyWithMessage() { 
		CommonController.assertEmpty((String)null,"Fail");
		CommonController.assertEmpty("", "Fail");
		
		try { 
			CommonController.assertEmpty("no-empty", "Oops: %s", 1);
			fail();
		}
		catch( Error e ) { 
			assertEquals( "Oops: 1", e.getMessage());
		}
		
	}
	
	
	@Test 
	public void testAssertNotEmpty() { 
		CommonController.assertNotEmpty("xxx");
		
		try { 
			CommonController.assertNotEmpty((String)null);
			fail();
		}
		catch( Error e ) { 
			// OK
		}

		
		try { 
			CommonController.assertNotEmpty("");
			fail();
		}
		catch( Error e ) { 
			// OK
		}
	
	}

	@Test 
	public void testAssertNotEmptyWithMessage() { 
		CommonController.assertNotEmpty("xxx", "Oops");
		
		try { 
			CommonController.assertNotEmpty((String)null, "Oops: %s", 2);
			fail();
		}
		catch( Error e ) { 
			assertEquals( "Oops: 2", e.getMessage());
		}

		
		try { 
			CommonController.assertNotEmpty("", "Oops: %s", 3);
			fail();
		}
		catch( Error e ) { 
			assertEquals( "Oops: 3", e.getMessage());
		}
	}	
	
	
	@Test 
	public void testAssertNull() { 
		CommonController.assertNull(null);
		
		try { 
			CommonController.assertNull("no-null");
			fail();
		}
		catch( Error e ) { 
			// OK
		}
		
	}

	@Test 
	public void testAssertNullWithMessage() { 
		CommonController.assertNull(null,"Fail");
		
		try { 
			CommonController.assertNull("no-null", "Oops: %s", 4);
			fail();
		}
		catch( Error e ) { 
			assertEquals( "Oops: 4", e.getMessage());
		}
		
	}
	

	
	@Test 
	public void testAssertNotNull() { 
		CommonController.assertNotNull("no-null");
		
		try { 
			CommonController.assertNotNull(null);
			fail();
		}
		catch( Error e ) { 
			// OK
		}
		
	}

	@Test 
	public void testAssertNotNullWithMessage() { 
		CommonController.assertNotNull("no-null","Fail");
		
		try { 
			CommonController.assertNotNull(null, "Oops: %s", 5);
			fail();
		}
		catch( Error e ) { 
			assertEquals( "Oops: 5", e.getMessage());
		}
	}
	
	@Test 
	public void tetsAssertTrue() { 
		CommonController.assertTrue(true);

		try { 
			CommonController.assertTrue(false); fail();
		}
		catch( Error e ) {  /* OK */ }
	}

	
	@Test 
	public void tetsAssertTrueWithMessage() { 
		CommonController.assertTrue(true,"Fail");

		try { 
			CommonController.assertTrue(false, "Fail: %s", 99); fail();
		}
		catch( Error e ) {  assertEquals("Fail: 99", e.getMessage()); }
	}

	@Test 
	public void tetsAssertFalse() { 
		CommonController.assertFalse(false);

		try { 
			CommonController.assertFalse(true); fail();
		}
		catch( Error e ) {  /* OK */ }
	}

	
	@Test 
	public void tetsAssertFalseWithMessage() { 
		CommonController.assertFalse(false,"Fail");

		try { 
			CommonController.assertFalse(true, "Fail: %s", 99); fail();
		}
		catch( Error e ) {  assertEquals("Fail: 99", e.getMessage()); }
	}

	@Test 
	public void tetsAssertEquals() { 
		CommonController.assertEquals(1,1);

		try { 
			CommonController.assertEquals(1,2); fail();
		}
		catch( Error e ) {  /* OK */ }
	}

	
	@Test 
	public void tetsAssertEqualsWithMessage() { 
		CommonController.assertEquals(1,1,"Fail");

		try { 
			CommonController.assertEquals(1,2, "Fail: %s", 99); fail();
		}
		catch( Error e ) {  assertEquals("Fail: 99", e.getMessage()); }
	}	


	@Test 
	public void tetsAssertNotEquals() { 
		CommonController.assertNotEquals(1,2);

		try { 
			CommonController.assertNotEquals(1,1); fail();
		}
		catch( Error e ) {  /* OK */ }
	}

	
	@Test 
	public void tetsAssertNotEqualsWithMessage() { 
		CommonController.assertNotEquals(1,2,"Fail");

		try { 
			CommonController.assertNotEquals(1,1, "Fail: %s", 99); fail();
		}
		catch( Error e ) {  assertEquals("Fail: 99", e.getMessage()); }
	}	

}

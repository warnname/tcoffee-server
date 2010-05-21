package util;

import play.test.UnitTest;

public class ObjectsTest extends UnitTest {

	@org.junit.Test 
	public void testEquals() {
		SimpleClazz TEST1 = new SimpleClazz();
		TEST1.field1 = "Hola";
		TEST1.field2 = 99;
		TEST1.notPublicFieldIsNotChecked = "xxx";

		SimpleClazz TEST2 = new SimpleClazz();
		TEST2.field1 = "Hola";
		TEST2.field2 = 99;
		TEST2.notPublicFieldIsNotChecked = "zzz";
		
		assertTrue( Objects.equals(TEST1, TEST2) );
	
	}
}



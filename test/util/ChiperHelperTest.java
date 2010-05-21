package util;

import org.junit.Test;

import play.test.UnitTest;

public class ChiperHelperTest extends UnitTest {

	@Test 
	public void testEncryptDecrypt() {
		
		String TEST = "Hola, Que tal?";
		
		assertEquals(TEST, ChiperHelper.decrypt( ChiperHelper.encrypt(TEST) ));
	}
	
}

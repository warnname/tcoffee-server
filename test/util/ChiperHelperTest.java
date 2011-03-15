package util;

import org.junit.Test;

import play.test.UnitTest;

public class ChiperHelperTest extends UnitTest {

	@Test 
	public void testEncryptDecrypt() {
		
		String TEST = "Hola, Que tal?";
		assertEquals(TEST, ChiperHelper.decrypt( ChiperHelper.encrypt(TEST) ));
		
	}
	
	@Test
	public void testEncrypt() { 
		assertEquals("Gm8WSxyWcdwTWApq+sp6dQ==", ChiperHelper.encrypt("Hola, Que tal?") );
	}

	@Test
	public void testDecrypt() { 
		assertEquals("Hola, Que tal?", ChiperHelper.decrypt("Gm8WSxyWcdwTWApq+sp6dQ==") );
	}
}

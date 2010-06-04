package util;

import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import play.libs.IO;
import play.test.FunctionalTest;

public class PerlTest extends FunctionalTest {
	
	@Test 
	public void testPerlExist() throws IOException, InterruptedException {
		
		Process exec = Runtime.getRuntime().exec("perl -v");

		int exitCode = exec.waitFor();
		String result = IO.readContentAsString(exec.getInputStream());
		assertEquals(0,exitCode);
		assertTrue( result.trim().startsWith("This is perl") );

	} 

}

package util;

import java.io.File;

import org.junit.Test;

import play.libs.IO;
import play.test.UnitTest;

public class ReloadableSingletonFileTest extends UnitTest {

	
	@Test
	public void testReloadOnChange() {

		ReloadableSingletonFile<String> singleton = new ReloadableSingletonFile<String>(TestHelper.sampleFasta()) {
			@Override
			public String readFile(File file) {
				return IO.readContentAsString(file);
			}
		}; 
		
		String instance = singleton.get();
		assertNotNull(instance);

		/* being a singleton it MUST be the same instance */
		String instance2 = singleton.get();
		assertSame( instance, instance2 );
		
		/* touch the conf file to force instance reload */
		singleton.file.setLastModified( System.currentTimeMillis() );
		String instance3 = singleton.get();
		/* so the second instance MUST be a different instance */
		assertNotSame( instance, instance3 );		
	}
	
}

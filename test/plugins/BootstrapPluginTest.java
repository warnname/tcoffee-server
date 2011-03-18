package plugins;

import java.io.File;

import org.junit.Test;

import play.test.UnitTest;

public class BootstrapPluginTest extends UnitTest {

	@Test
	public void testGetDatabaseFileFromUrl( ) { 
		
		assertEquals( null, BootstrapPlugin.getDatabaseFileFromUrl(null) );
		assertEquals( null, BootstrapPlugin.getDatabaseFileFromUrl("") );
		assertEquals( null, BootstrapPlugin.getDatabaseFileFromUrl("xxx") );
		assertEquals( null, BootstrapPlugin.getDatabaseFileFromUrl("jdbc:h2:mem:test") );
		assertEquals( null, BootstrapPlugin.getDatabaseFileFromUrl("jdbc:h2:mem:") );
		assertEquals( new File("mem.h2.db"), BootstrapPlugin.getDatabaseFileFromUrl("jdbc:h2:mem") );

		assertEquals( new File("/some/path.h2.db"), BootstrapPlugin.getDatabaseFileFromUrl("jdbc:h2:/some/path") );
		assertEquals( new File("/some/path.h2.db"), BootstrapPlugin.getDatabaseFileFromUrl("jdbc:h2:/some/path;more confix") );
	
	}
	
}

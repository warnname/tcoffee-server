package models;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.libs.Crypto;
import play.mvc.Router;
import play.test.UnitTest;


public class AppPropsTest extends UnitTest {
	
	private AppProps props;

	@Before
	public void init() {
		props = new AppProps();
		props.put("alpha", "1");
		props.put("beta", "2");
		props.put("gamma", "abc");
		
	}
	
	
	@Test
	public void testCopy( ) { 
		AppProps copy = new AppProps(props);
		
		assertEquals("1", copy.getString("alpha"));
		assertEquals("2", copy.getString("beta"));
		assertEquals( props.getNames().size(), copy.getNames().size() );
	}
	
	@Test 
	public void testSingletonInstance() {
		AppProps instance = AppProps.instance();
		assertNotNull(instance);

		/* being a singleton it MUST be the same instance */
		AppProps instance2 = AppProps.instance();
		assertTrue( instance == instance2 );
		
	}
	
	
	@Test 
	public void testGetNames() {
		List<String> list = props.getNames();
		assertTrue( list.contains("alpha")  );
		assertTrue( list.contains("beta")  );
		assertFalse( list.contains("uhoh")  );
		
	}
	
	@Test
	public void testGetString() {
		assertEquals( "1", props.getString("alpha"));
		assertEquals( "2", props.getString("beta"));
		assertEquals( "99", props.getString("xxx", "99"));
		assertNull( props.getString("xxx"));
	}	
	
	@Test
	public void testGetInteger() {
		assertEquals( Integer.valueOf(1), props.getInteger("alpha"));
		assertEquals( Integer.valueOf(2), props.getInteger("beta"));
		assertEquals( null, props.getInteger("xxx"));
		assertEquals( Integer.valueOf(99), props.getInteger("xxx",99));
	}	
	
	
	@Test
	public void testGetLong() {
		assertEquals( Long.valueOf(1), props.getLong("alpha"));
		assertEquals( Long.valueOf(2), props.getLong("beta"));
		assertEquals( null, props.getLong("xxx"));
		assertEquals( Long.valueOf(99), props.getLong("xxx",99));
	}	
	
	@Test 
	public void testGetEncruptedString() { 
		String secret = Crypto.encryptAES("Hola");
		secret = "{{{" + secret + "}}}";
		props.put("secret", secret);

		assertEquals( "Hola", props.getString("secret") );
				
	}

	@Test 
	public void testContainsKey() {
		assertTrue( props.containsKey("alpha") );
		assertFalse( props.containsKey("xxx") );
	}

	
	@Test
	public void testGetWebmasterEmail() {
		props.put("settings.webmaster", "gino@crg.es");
		assertEquals("gino@crg.es", props.getWebmasterEmail());
	}
	
	@Test
	public void testGetDataPath() {
		assertEquals( AppProps.WORKSPACE_FOLDER.getAbsolutePath(), props.getDataPath() );
	}
	
	@Test 
	public void testGetDataCacheDuration() {
		assertEquals(864000, props.getDataCacheDuration());

		// set to 1 seconds 
		props.put("data.cache.duration", "1s");
		assertEquals(1, props.getDataCacheDuration());

		// set to 1 minute 
		props.put("data.cache.duration", "1min");
		assertEquals(60, props.getDataCacheDuration());
	
	} 

	@Test
	public void testContextPath() { 
		/* default root context */
		assertEquals("", props.contextPath);
		
		/* simulate a top level context path */
		Router.routes.add(0, Router.getRoute("GET", "/root/path", "Main.index", null, null));
		props.contextPath = null; // force re-evaluating
		assertEquals("/root", props.contextPath);

		Router.routes.remove(0);
	}
	
	@Test
	public void testHostName() { 

		props.put("settings.hostname", "tcoffee.crg.cat");
		assertEquals("tcoffee.crg.cat", props.getHostName());

		props.put("settings.hostname", "localhost:9000");
		assertEquals("localhost:9000", props.getHostName());
	
	}
	
}

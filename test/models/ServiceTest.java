package models;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class ServiceTest extends UnitTest {

	
	
	@Test 
	public void testFieldText() {
		
		String xml = 
			"<service name='alfa' >" +
				"<title>Hola</title>" +
				"<description>Some text</description>" +
				"<group>Alignment</group>" +
				"<cite>to cite report that</cite>" +
				"<input></input>" +
				"<process></process>" +
				"<output></output>" +
			"</service>";
		
		Service service = XStreamHelper.fromXML(xml);
		assertNotNull(service);

		assertEquals("alfa", service.name);
		assertEquals("Hola", service.title);
		assertEquals("Alignment", service.group);
		assertEquals("Some text", service.description);
		assertEquals("to cite report that", service.cite);

		assertNotNull(service.input);
		assertNotNull(service.process);
		assertNotNull(service.output);
		
	} 	
	
	@Test
	public void testResolveEnv() { 
		Bundle bundle = new Bundle();
		bundle.name = "tcoffee";
		bundle.root = new File("/root");
		bundle.author = "Pablo";
		
		
		bundle.environment = new Properties();
		bundle.environment.setProperty("home", "${bundle.path}");
		bundle.environment.setProperty("conf.path", "${bundle.path}/conf");
		bundle.environment.setProperty("complex", "${bundle.path}/other_${data.path}");
		bundle.environment.setProperty("email", "plain@email.com");
		bundle.environment.setProperty("basic", "value");
		bundle.environment.setProperty("env_home", "${env.HOME}");
		
		
		Map<String,Object> ctx = new HashMap<String, Object>();
		ctx.put("bundle.path", "/bundle/root");
		ctx.put("data.path", "/data/path");
		
		Service service = new Service();
		service.bundle = bundle;
		service.fContextHolder = new ContextHolder(ctx);
		
		Map<String,String> env = service.defaultEnvironment();
		assertEquals( "/bundle/root", env.get("home") );
		assertEquals( "/bundle/root/conf", env.get("conf.path") );
		assertEquals( "/bundle/root/other_/data/path", env.get("complex") );
		assertEquals( System.getenv("HOME"), env.get("env_home") );
		assertEquals( "value", env.get("basic") );
		assertEquals( "plain@email.com", env.get("email") );
	}
	
	@Test 
	public void testGetUrl() { 
		Service service = new Service();
		service.bundle =  new Bundle("tcoffee");
		service.fRid = "999";
		String url = service.getResultURL();
		
		assertEquals( "http://localhost:9000/apps/tcoffee/result?rid=999", url );
	}
	
	
}

package models;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.Utils;
import util.XStreamHelper;

public class EnvTest extends UnitTest {

	@Before
	public void init() {
		TestHelper.module("var1=99", "var2=66");
	}
	
	@Test
	public void testFromXml() {
		String xml = "<env test='alpha' />";
		Env env = XStreamHelper.fromXML(xml);
		assertNotNull(env);
		assertEquals( "test", env.getNames().get(0) );
		assertEquals( "alpha", env.get("test") );
	}

	@Test 
	public void testVar() {
		Env env = XStreamHelper.fromXML("<env test='${var1}' />");
		assertEquals("99", env.get("test"));
		
		env = new Env("test=${var2}");
		assertEquals("66", env.get("test"));
	}
	
	@Test
	public void testCreate() {
		Env env = new Env("X=1", "Y=2", "W");
		assertEquals(3, env.getNames().size());
		assertEquals("1", env.get("X") );
		assertEquals("2", env.get("Y") );
		assertEquals(null, env.get("W") );
		assertEquals(null, env.get("Z") );
	}
	
	@Test
	public void testCopy() {
		Env env = new Env("X=1", "Y=2");
		Env copy = Utils.copy(env);
		
		assertEquals(copy.get("X"), env.get("X"));
		assertEquals(copy.get("Y"), env.get("Y"));
		
	}

}

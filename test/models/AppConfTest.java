package models;

import java.util.ArrayList;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;
import exception.QuickException;

public class AppConfTest extends UnitTest {


	@Test 
	public void testGetModule() {
		
		AppConf c = new AppConf();
		c.modules.add(new Module("a"));
		c.modules.add(new Module("b"));
		c.modules.add(new Module("c"));
		
		Module m = c.module("b");
		assertEquals("b", m.name );
		
		try {
			c.module("wrong name");
			fail("method 'mudule' have to raise exception but it didn't");
		}
		catch( QuickException e ) {
			// IT MUST RAISE AN EXCEPTION  
		}
		
	}

	@Test 
	public void testFromXML() {
		String xml = 
			"<server>" +
			 	"<def ></def>" +
			
				"<module name='primero' ><title>uno</title></module>" +
				"<module name='segundo' ><title>dos</title></module>" +
				"<module name='tercero' ><title>tres</title></module>" +
			"</server>";
		
		AppConf conf = XStreamHelper.fromXML(xml);
		
		assertNotNull(conf);
		
		assertNotNull(conf.def);
		
		assertEquals("uno", conf.modules.get(0).title);
		assertEquals("dos", conf.modules.get(1).title);
		assertEquals("tres", conf.modules.get(2).title);

		assertEquals("primero", conf.modules.get(0).name);
		assertEquals("segundo", conf.modules.get(1).name);
		assertEquals("tercero", conf.modules.get(2).name);

	}
	
	@Test 
	public void testDefaultConf() {
		AppConf conf = AppConf.instance();
		assertNotNull(conf);
	}

	@Test
	public void testGetGroups() {
		
		Module m1 = new Module();
		m1.group = "G1";

		Module m2 = new Module();
		m2.group = "G2";
		
		Module m3 = new Module();
		m3.group = " G2 ";

		Module m4 = new Module();

		Module m5 = new Module();
		m5.group = "";
		
		AppConf c = new AppConf();
		c.modules = new ArrayList<Module>();
		c.modules.add(m1);
		c.modules.add(m2);
		c.modules.add(m3);
		c.modules.add(m4);
		c.modules.add(m5);
		
		assertArrayEquals( new String[]{"G1","G2",""}, c.getGroups().toArray());

		assertArrayEquals( new Module[]{m1}, c.modulesByGroup("G1").toArray() );
		assertArrayEquals( new Module[]{m2,m3}, c.modulesByGroup("G2").toArray() );
		assertArrayEquals( new Module[]{m4,m5}, c.modulesByGroup("").toArray() );
		assertArrayEquals( new Module[]{}, c.modulesByGroup("XX").toArray() );
		
		try {
			c.modulesByGroup(null);
			fail("Argument null is not supported");
		}
		catch( Exception e ) { /* ok */  }
	} 


}

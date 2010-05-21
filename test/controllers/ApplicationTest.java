package controllers;

import models.Module;

import org.junit.Test;

import play.test.UnitTest;

public class ApplicationTest extends UnitTest {

	@Test 
	public void testLoadModule() {
		/*
		 * 1. test load a valid module template 
		 */
		Module module = Application.loadModuleFile( "templates/configure.xml" );
		assertNotNull(module);
		assertEquals("configure", module.name);
		
		/*
		 * 2. test exception on missing template file 
		 */
		try {
			Application.loadModuleFile("/missing/file.xml");
			fail();
		} catch( Exception e ) {
			/* it must fail */
		}
	}
	

}
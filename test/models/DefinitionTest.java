package models;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class DefinitionTest extends UnitTest {
	
	@Test
	public void testFromXml() {
		
		String xml = 
			"<def>" +
//				"<props>" +
//				"<property name='alfa' >uno</property>" +
//				"<property name='beta' >dos</property >" +
//				"</props>" +
			
				"<valid-result>" +
				"<title>Alignment completed</title>" + 
				"</valid-result>" +
				
				"<fail-result>" +
				"<title>Alignment failed</title>" + 
				"</fail-result>" + 
				
			"</def>";
		
		Definition def = XStreamHelper.fromXML(xml);
		assertNotNull(def);

//		assertEquals( "uno", def.props.get("alfa")); 
//		assertEquals( "dos", def.props.get("beta")); 
//		
//		
		assertNotNull( def.validResult );
		assertEquals( "Alignment completed", def.validResult.title );

		assertNotNull( def.failResult );
		assertEquals( "Alignment failed", def.failResult.title );
	}
	

//	@Test 
//	public void testSave() {
//		Definition def = new Definition();
//		def.props = new Properties();
//		def.props.setProperty("alpha", "1");
//		def.props.setProperty("beta", "2");
//		
//		System.out.println(XstreamUtils.toXML(def));
//	}
	
	
//	@Test
//	public void testCopy() {
//		Definition def = new Definition();
//		def.consts = new ArrayList<Constant>();
//		def.consts.add( new Constant("alpha", "1") );
//		def.consts.add( new Constant("beta", "2") );
//		def.validResult = new OutSection();
//		def.validResult.title = "Ok!";
//		def.failResult = new OutSection();
//		def.failResult.title = "Ummhhh";
//
//		Definition copy = new Definition(def);
//		
//		assertTrue(Utils.isEquals(def.consts, copy.consts ));
//		assertEquals(def.validResult, copy.validResult);
//		assertEquals(def.failResult, copy.failResult);
//	}
	
//	@Test
//	public void testGetConst() {
//		
//		Definition def = new Definition();
//		def.consts = new ArrayList<Property>();
//		def.consts.add( new Property("alpha", "1") );
//		def.consts.add( new Property("beta", "2") );
//		
//		assertEquals("1", def.getConst("alpha"));
//		assertEquals("2", def.getConst("beta"));
//		assertNull(def.getConst("delta"));
//		
//	}

}

package models;

import org.junit.Test;

import play.test.UnitTest;
import util.XStreamHelper;

public class DefinitionTest extends UnitTest {
	
	@Test
	public void testFromXml() {
		
		String xml = 
			"<def>" +
			
				"<valid-result>" +
				"<result>" +
				"<title>Alignment completed</title>" + 
				"</result>" +
				"</valid-result>" +
				
				"<fail-result>" +
				"<result>" +
				"<title>Alignment failed</title>" + 
				"</result>" +
				"</fail-result>" +
				"" +
				"<dictionary>" +
				"<label key='x' value='alpha' />" +
				"<label key='y' value='beta' />" +
				"</dictionary>" + 
				
			"</def>";
		
		Definition def = XStreamHelper.fromXML(xml);
		assertNotNull(def);

		assertNotNull( def.validResult );
		assertEquals( "Alignment completed", def.validResult.result.title );

		assertNotNull( def.failResult );
		assertEquals( "Alignment failed", def.failResult.result.title );
		
		assertNotNull( def.dictionary );
		assertEquals( 2, def.dictionary.labels.size() );
	}
	



}

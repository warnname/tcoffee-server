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
				"<title>Alignment completed</title>" + 
				"</valid-result>" +
				
				"<fail-result>" +
				"<title>Alignment failed</title>" + 
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
		assertEquals( "Alignment completed", def.validResult.title );

		assertNotNull( def.failResult );
		assertEquals( "Alignment failed", def.failResult.title );
		
		assertNotNull( def.dictionary );
		assertEquals( 2, def.dictionary.labels.size() );
	}
	



}

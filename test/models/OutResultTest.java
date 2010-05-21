package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.XStreamHelper;

public class OutResultTest extends UnitTest {

	@Before
	public void register() {
		TestHelper.module();
	}
	
	@Test
	public void testFromXml() {
	
		String xml  
			= "<result>" +
				"<elapsed-time>999</elapsed-time>" +
				"<status>DONE</status>" +
				"<error>the error message</error>" +
				"<item>" +
					"<name>sample.html</name>" +
					"<format>html</format>" +
					"<type>xxx</type>" +
					"<label>Hola</label>" +
				"</item>" +
			  "</result>";
		
		OutResult out = XStreamHelper.fromXML(xml);
		assertNotNull(out);
		assertEquals(999, out.elapsedTime);
		assertEquals( Status.DONE, out.status);
		assertEquals("the error message", out.errors.get(0));
		assertEquals(1, out.getItems().size() );
		assertEquals("sample.html", out.getItems().get(0).name );
		assertEquals("html", out.getItems().get(0).format );
		assertEquals("xxx", out.getItems().get(0).type );
		assertEquals("Hola", out.getItems().get(0).label );
	}
	
	@Test 
	public void testToXml() throws IOException {
		OutResult result = new OutResult();
		result.status = Status.DONE;
		result.elapsedTime = 99;
		result.errors = new ArrayList<String>();
		result.errors.add("Hola");
		result.errors.add("Ciao");
		
		OutItem item = new OutItem("sample.html","xxx");
		result.add(item);
		
		BufferedReader reader = new BufferedReader(new StringReader(XStreamHelper.toXML(result)));
		assertEquals("<result>", reader.readLine().trim());
		assertEquals("<item>", reader.readLine().trim());
		assertEquals(String.format("<webpath>%s</webpath>", item.webpath), reader.readLine().trim());
		assertEquals("<label>Sequence alignment in HTML format</label>", reader.readLine().trim());
		assertEquals("<type>xxx</type>", reader.readLine().trim());
		assertEquals("<name>sample.html</name>", reader.readLine().trim());
		assertEquals(String.format("<file>%s</file>", item.file.getCanonicalPath()), reader.readLine().trim());
		assertEquals("<format>html</format>", reader.readLine().trim());
		assertEquals("</item>", reader.readLine().trim());
		assertEquals(String.format("<created-time>%s</created-time>", result.createdTime), reader.readLine().trim());
		assertEquals("<elapsed-time>99</elapsed-time>", reader.readLine().trim());
		assertEquals("<status>DONE</status>", reader.readLine().trim());
		assertEquals("<error>Hola</error>", reader.readLine().trim());
		assertEquals("<error>Ciao</error>", reader.readLine().trim());
		assertEquals("</result>", reader.readLine().trim());
	}
	
	@Test 
	public void testConstructor() {
		OutResult result = new OutResult();
		assertTrue( result.createdTime>0 && result.createdTime<=System.currentTimeMillis() );
	}
}

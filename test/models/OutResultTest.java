package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.Utils;
import util.XStreamHelper;

public class OutResultTest extends UnitTest {

	@Before
	public void register() {
		TestHelper.init();
		Service.current().bundle.def.dictionary = null;
	}
	
	@Test 
	public void testCopy() { 
		
		OutResult result = new OutResult();
		result.bundle = "tcoffee";
		result.cite = "cite";
		result.elapsedTime = 99;
		result.errors = new ArrayList<String>();
		result.errors.add("oops");
		result.service = "mode";
		result.status = Status.DONE;
		result.title = "Hola";
		
		OutResult copy = Utils.copy(result);
		
		assertEquals( copy, result );
		assertEquals( copy.bundle, result.bundle );
		assertEquals( copy.cite, result.cite );
		assertEquals( copy.elapsedTime, result.elapsedTime );
		assertEquals( copy.errors.size(), result.errors.size() );
		assertEquals( copy.errors.get(0), result.errors.get(0) );
		assertEquals( copy.service, result.service);
		assertEquals( copy.status, result.status );
		assertEquals( copy.title , result.title );

		
	}
	
	@Test
	public void testFromXml() {
	
		String xml  
			= "<result>" +
				"<title>the-title</title>" + 
				"<bundle>bundle-name</bundle>" + 
				"<service>the-mode</service>" + 
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
		assertEquals("bundle-name", out.bundle);
		assertEquals("the-title", out.title);
		assertEquals("the-mode", out.service);
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
		item.webpath = "/web/path";
		item.file = new File("/root/file.txt");
		result.add(item);
		
		BufferedReader reader = new BufferedReader(new StringReader(XStreamHelper.toXML(result)));
		assertEquals("<result>", reader.readLine().trim());
		assertEquals("<item>", reader.readLine().trim());
		assertEquals("<webpath>/web/path</webpath>", reader.readLine().trim());
		assertEquals("<label>Sequence alignment in HTML format</label>", reader.readLine().trim());
		assertEquals("<type>xxx</type>", reader.readLine().trim());
		assertEquals("<name>sample.html</name>", reader.readLine().trim());
		assertEquals("<file>/root/file.txt</file>", reader.readLine().trim());
		assertEquals("<format>html</format>", reader.readLine().trim());
		assertEquals("<aggregation>xxx</aggregation>", reader.readLine().trim());
		assertEquals("</item>", reader.readLine().trim());
		assertEquals("<elapsed-time>99</elapsed-time>", reader.readLine().trim());
		assertEquals("<status>DONE</status>", reader.readLine().trim());
		assertEquals("<error>Hola</error>", reader.readLine().trim());
		assertEquals("<error>Ciao</error>", reader.readLine().trim());
		assertEquals("</result>", reader.readLine().trim());
	}
	
	@Test 
	public void testTypes() {
		OutResult out = new OutResult();
		out.add( new OutItem("file1.txt", "MSA") );
		out.add( new OutItem("file2.txt", "SYSTEM") );
		out.add( new OutItem("file3.txt", "MSA") );
		out.add( new OutItem("file4.txt", "TREE") );
		
		List<String> types = out.aggregations();
		assertEquals( 3, types.size());
		assertEquals( Arrays.asList("MSA", "SYSTEM", "TREE"), types);
	
	}
	
	@Test
	public void testFilter() {
		OutResult out = new OutResult();
		OutItem i1; 
		OutItem i2; 
		OutItem i3; 
		OutItem i4; 
		
		out.add( i1 = new OutItem("file1.txt", "MSA") );
		out.add( i2 = new OutItem("file2.txt", "SYSTEM") );
		out.add( i3 = new OutItem("file3.txt", "MSA") );
		out.add( i4 = new OutItem("file4.txt", "TREE") );
		
		List<OutItem> result = out.filter("MSA");
		assertEquals( 2, result.size());
		assertEquals( Arrays.asList(i1,i3), result);
	}
}

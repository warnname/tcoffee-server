package models;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;

public class OutItemTest extends UnitTest{

	@Before
	public void register() {
		TestHelper.module();
	}
	
	@Test 
	public void testCreateWithName() {
		OutItem item = new OutItem("sample.html", "sample");
		assertEquals("sample.html", item.name);
		assertEquals("sample.html", item.file.getName() );
		assertEquals(String.format("/data/%s/sample.html", Module.current().rid()), item.webpath);
		assertEquals("html", item.format);
		assertEquals("Sequence alignment in HTML format", item.label);
		assertEquals("sample", item.type);
	}  
	
}

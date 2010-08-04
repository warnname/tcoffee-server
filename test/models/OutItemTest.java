package models;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;

public class OutItemTest extends UnitTest{

	@Before
	public void register() {
		TestHelper.module();
		AppConf.instance().def.dictionary = null;
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
		assertEquals("sample", item.aggregation);
	}  

	@Test 
	public void tetsAggregation () { 
		Dictionary dic = new Dictionary();
		dic.addLabel("xxx", "yyy");
		AppConf.instance().def.dictionary = dic;
		
		OutItem item = new OutItem("sample.html", "xxx");
		
		assertEquals( "xxx", item.type );
		assertEquals( "yyy", item.aggregation );
	}
	
}

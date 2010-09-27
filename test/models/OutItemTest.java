package models;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.Utils;

public class OutItemTest extends UnitTest{

	@Before
	public void register() {
		TestHelper.init();
		Service.current().bundle.def.dictionary = null;
	}
	
	@Test
	public void testCopy() { 
		OutItem item = new OutItem();
		item.name = "alpha";
		item.label = "the label";
		item.aggregation = "some aggregation";
		item.file = new File("file.txt");
		item.format = "the fmt";
		item.type = "the type";
		item.webpath = "/path/to/file";
		
		OutItem copy = Utils.copy(item);
		
		assertEquals( copy, item );
		assertEquals( copy.name, item.name );
		assertEquals( copy.label, item.label );
		assertEquals( copy.aggregation, item.aggregation );
		assertEquals( copy.file, item.file );
		assertEquals( copy.format, item.format );
		assertEquals( copy.type , item.type );
		assertEquals( copy.webpath , item.webpath );
			
	}
	
	@Test 
	public void testCreateWithName() {
		OutItem item = new OutItem("sample.html", "sample");
		assertEquals("sample.html", item.name);
		assertEquals("html", item.format);
		assertEquals("Sequence alignment in HTML format", item.label);
		assertEquals("sample", item.type);
		assertEquals("sample", item.aggregation);
	}  

	@Test 
	public void testAggregation () { 
		Dictionary dic = new Dictionary();
		dic.addLabel("xxx", "yyy");
		Service.current().bundle.def.dictionary = dic;
		
		OutItem item = new OutItem("sample.html", "xxx");
		
		assertEquals( "xxx", item.type );
		assertEquals( "yyy", item.aggregation );
	}
	
}

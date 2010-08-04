package models;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.Utils;
import util.XStreamHelper;

public class DictionaryTest extends UnitTest {

	private Dictionary dic;

	@Before
	public void init() {
		String xml = 
			"<dictionary >" +
			"<label key='1' value='a' />" +
			"<label key='2' value='b' />" +
			"</dictionary>";
	
		dic = XStreamHelper.fromXML(xml);
		
	} 
	
	@Test 
	public void testFromXml() {
		
		assertNotNull(dic);
		assertEquals( 2, dic.labels.size() );
		assertEquals( "1", dic.labels.get(0).key );
		assertEquals( "a", dic.labels.get(0).value );
	} 
	
	@Test 
	public void testCopy() {
		Dictionary copy = Utils.copy(dic);
		
		assertEquals( dic, copy );
		assertEquals( dic.labels.size(), copy.labels.size() );
	} 
	
	@Test 
	public void testDecode() { 
		
		assertEquals( "a", dic.decode("1") );
		assertEquals( "b", dic.decode("2") );
		assertEquals( "3", dic.decode("3") );
		assertEquals( null, dic.decode("3", null) );
	}
}

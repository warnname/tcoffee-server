package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import play.test.UnitTest;


public class UtilsTest extends UnitTest {

	@Test
	public void isEmpty() {
		assertTrue( Utils.isEmpty((String)null) );
		assertTrue( Utils.isEmpty("") );
		assertFalse( Utils.isEmpty(" ") );
		assertFalse( Utils.isEmpty("hola") );
	}
	
	@Test
	public void equalsArray() {
		assertTrue( Utils.isEquals((Object[])null,null) );
		assertTrue( Utils.isEquals(new String[]{"a","b","c"},new String[]{"a","b","c"}) );
		assertFalse( Utils.isEquals(null,new String[]{"a","b","c"}) );
		assertFalse( Utils.isEquals(new String[]{"a","b","c"},new String[]{}) );
		assertFalse( Utils.isEquals(new String[]{"1","2","3"},new String[]{"a","b","c"}) );
	}

	@Test
	public void equalsCollections() {
		final List<String> TEST = Arrays.asList("a","b","c");
		
		assertTrue( Utils.isEquals((Collection<?>)null,null) );
		assertTrue( Utils.isEquals(new ArrayList<String>(TEST), new ArrayList<String>(TEST)));
		assertFalse( Utils.isEquals(null, new ArrayList<String>(TEST)) );
		assertFalse( Utils.isEquals(new ArrayList<String>(TEST),new ArrayList()) );
		assertFalse( Utils.isEquals(new ArrayList<String>(TEST),Arrays.asList("1","2","3")) );
	}

	@Test 
	public void testAsStringBool() {  
		assertEquals("true", Utils.asString(true));
		assertEquals("false", Utils.asString(false));
		assertEquals("false", Utils.asString((Boolean)null));
	}

	
	@Test 
	public void testAsStringShort() { 
		assertEquals("1", Utils.asString( Short.parseShort("1") ));
		assertEquals("0", Utils.asString( Short.parseShort("0") ));
		assertEquals("0", Utils.asString((Short)null));
	}	
	
	@Test 
	public void testAsStringInteger() { 
		assertEquals("1", Utils.asString(1));
		assertEquals("0", Utils.asString((Integer)null));
	}
	
	@Test 
	public void testAsStringLong() { 
		assertEquals("1", Utils.asString(1L));
		assertEquals("0", Utils.asString(0L));
		assertEquals("0", Utils.asString((Long)null));
	}	
	
	@Test 
	public void testAsStringFloat() { 
		assertEquals("1.0", Utils.asString(1.0));
		assertEquals("0", Utils.asString(0));
		assertEquals("0", Utils.asString((Float)null));
	}	

	@Test 
	public void testAsStringDouble() { 
		assertEquals("1.0", Utils.asString(1.0d));
		assertEquals("0.0", Utils.asString(0d));
		assertEquals("0.0", Utils.asString((Double)null));
	}	

	@Test 
	public void testAsStringString() { 
		assertEquals("abc", "abc" );
		assertEquals("abc", Utils.asString("  abc  ") );
		assertEquals("a b c", Utils.asString("  a b c  "));
		assertEquals("", Utils.asString("   "));
		assertEquals("", Utils.asString((String)null) );
	}	
	
	public void testAsStringDate() { 
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2010);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 23);
		c.set(Calendar.HOUR_OF_DAY, 14);
		c.set(Calendar.MINUTE, 30);
		
		assertEquals("23/02/2010 14:30", Utils.asString(c.getTime()));
		assertEquals("", Utils.asString((Date)null));
		
		Date now = new Date();
		c.setTime(now);
		
		/* today - short format */
		assertEquals( String.format("%s:%s", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)), Utils.asString(now));

		assertEquals( String.format("%s:%s", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)), Utils.asString(now));
		
	}
	
	
	@Test
	public void testAsStringArray() { 
		assertNull(Utils.asStringArray(null));
		assertArrayEquals(new String[]{}, Utils.asStringArray(new HashMap<String,String>()));
		Map<String,String> map = new LinkedHashMap<String, String>();
		map.put("a", "1");
		map.put("b", "2");
		assertArrayEquals(new String[]{"a=1","b=2"}, Utils.asStringArray(map));
	}
	
	@Test
	public void testAsStringMap() {
		
		assertNull( Utils.asStringMap((String[])null) );
		
		Map<String,String> map = Utils.asStringMap("x=1", "y=2", null, "z=3", "a=", "b");
		assertEquals( 5, map.size() );
		assertEquals("1", map.get("x"));
		assertEquals("2", map.get("y"));
		assertEquals("3", map.get("z"));
		assertEquals(null, map.get("a"));
		assertEquals(null, map.get("b"));
		assertTrue( map.containsKey("a"));
		assertTrue( map.containsKey("b"));
	}
	
	//@Test()
	public void testArStringCollection() {
		List<String> list = Arrays.asList("1", "2", "3");
		
		assertEquals( "1, 2, 3", Utils.asString(list));
	}
	
	@Test
	public void testGetClasses() {
		Class[] result = Utils.getClasses("test_package.containing.a.clazz");
		assertNotNull(result);
		assertEquals(3, result.length);
	}
	
	
	@Test
	public void testCopy() {
		
		assertNull( Utils.copy((Object)null) );
		
		String c = "test";
		assertEquals( c, Utils.copy(c) );
		assertNotSame( c, Utils.copy(c) );
		
	}
	
	@Test 
	public void testCopyArray() {
		String[] test = {"a", "b", "c"};
		assertArrayEquals( test, Utils.copy(test) );
		assertNotSame( test, Utils.copy(test) );
		assertNull( Utils.copy((Object[])null) );
	}
	
	@Test 
	public void testCopyList() {
		List<String> test = Arrays.asList("A","B","C");
		
		assertEquals( test, Utils.copy(test) );
		assertNotSame( test, Utils.copy(test) );
		assertNull( Utils.copy((List)null) );
		
	}	
	
	@Test
	public void testParseShort( ) {
		assertEquals( Short.valueOf("123"), Utils.parseShort("123") );
		assertEquals( Short.valueOf("0"), Utils.parseShort("---") );
		assertEquals( Short.valueOf("-1"), Utils.parseShort("---", Short.valueOf("-1")) );
	}
	
	@Test
	public void testParseInteger( ) {
		assertEquals( new Integer(123), Utils.parseInteger("123") );
		assertEquals( new Integer(0), Utils.parseInteger("---") );
		assertEquals( new Integer(-1), Utils.parseInteger("---", -1) );
		assertEquals( (Integer)null, Utils.parseInteger("---", null) );
	} 

	@Test
	public void testParseLong( ) {
		assertEquals( new Long(123), Utils.parseLong("123") );
		assertEquals( new Long(0), Utils.parseLong("---") );
		assertEquals( new Long(-1), Utils.parseLong("---", -1L) );
		assertEquals( (Long)null, Utils.parseLong("---", null) );
	} 
	
	@Test
	public void testParseFloat( ) {
		assertEquals( new Float(123.45), Utils.parseFloat("123.45") );
		assertEquals( new Float(0), Utils.parseFloat("---") );
		assertEquals( new Float(-1), Utils.parseFloat("---", -1F) );
		assertEquals( (Float)null, Utils.parseFloat("---", null) );
	}
	
	@Test
	public void testParseDouble( ) {
		assertEquals( new Double(123.456789), Utils.parseDouble("123.456789") );
		assertEquals( new Double(0), Utils.parseDouble("---") );
		assertEquals( new Double(-1), Utils.parseDouble("---", -1D) );
		assertEquals( (Double)null, Utils.parseDouble("---", null) );
	} 	

	
	@Test
	public void testParseDate( ) {
		Calendar c = new GregorianCalendar();
		c.setTime(Utils.parseDate("01/02/2010 13:31"));

		assertEquals( 1, c.get(Calendar.DAY_OF_MONTH) );
		assertEquals( 1, c.get(Calendar.MONTH) );
		assertEquals( 2010, c.get(Calendar.YEAR) );
		assertEquals( 13, c.get(Calendar.HOUR_OF_DAY));
		assertEquals( 31, c.get(Calendar.MINUTE));

		
		c.setTime(Utils.parseDate("02/01/2010"));
		assertEquals( 2, c.get(Calendar.DAY_OF_MONTH) );
		assertEquals( 0, c.get(Calendar.MONTH) );
		assertEquals( 2010, c.get(Calendar.YEAR) );

		c.setTime(Utils.parseDate("02-01-2010"));
		assertEquals( 2, c.get(Calendar.DAY_OF_MONTH) );
		assertEquals( 0, c.get(Calendar.MONTH) );
		assertEquals( 2010, c.get(Calendar.YEAR) );

		
		c.setTime(Utils.parseDate("17:55"));
		assertEquals( 17, c.get(Calendar.HOUR_OF_DAY));
		assertEquals( 55, c.get(Calendar.MINUTE));
		
		assertNull(Utils.parseDate("9999"));
		assertNull(Utils.parseDate(""));
		assertNull(Utils.parseDate(null));

		Date now = new Date();
		assertEquals(now, Utils.parseDate("9999", now));
		
		
	} 	
	
	@Test
	public void testParseCalendar( ) {
		Calendar cal = Utils.parseCalendar("01/02/2010 13:31");

		assertEquals( 1, cal.get(Calendar.DAY_OF_MONTH) );
		assertEquals( 1, cal.get(Calendar.MONTH) );
		assertEquals( 2010, cal.get(Calendar.YEAR) );
		assertEquals( 13, cal.get(Calendar.HOUR_OF_DAY));
		assertEquals( 31, cal.get(Calendar.MINUTE));

		
		cal = Utils.parseCalendar("02/01/2010");
		assertEquals( 2, cal.get(Calendar.DAY_OF_MONTH) );
		assertEquals( 0, cal.get(Calendar.MONTH) );
		assertEquals( 2010, cal.get(Calendar.YEAR) );

		cal = Utils.parseCalendar("02-01-2010");
		assertEquals( 2, cal.get(Calendar.DAY_OF_MONTH) );
		assertEquals( 0, cal.get(Calendar.MONTH) );
		assertEquals( 2010, cal.get(Calendar.YEAR) );

		
		cal = Utils.parseCalendar("17:55");
		assertEquals( 17, cal.get(Calendar.HOUR_OF_DAY));
		assertEquals( 55, cal.get(Calendar.MINUTE));
		
		assertNull(Utils.parseDate("9999"));
		assertNull(Utils.parseDate(""));
		assertNull(Utils.parseDate(null));

		Calendar now = new GregorianCalendar();
		now.setTime(new Date());
		assertEquals(now, Utils.parseCalendar("9999", now));
		
	}
	
	
	@Test 
	public void testReplaceVars( ) {
		
		Map<String,Integer> vars = new HashMap<String, Integer>();
		vars.put("uno", new Integer(1));
		vars.put("dos", new Integer(2));
		vars.put("tres", 3);
		vars.put("cuatro", null);
		 
		
		String result = Utils.replaceVars( "num ${uno} y ${dos} mas ${tres} ${cuatro}" , vars);
		assertEquals("num 1 y 2 mas 3 ", result);
		
	}
	
	
	static class TestObj {
		
		private String val1 = "val1";
		
		private String val2 = "val2";
		
		public String getVal2() { return val2; }

		public void setVal2(String value) { val2 = "f-" + value; }
		
		
		String val3 = "3";
		public void setVal3(String value) { val3 = "f-" + value; }

		public void setVal4(String value) { val3 = value; }

		String val5 = "5";
		public void setVal5(String value) { val5 = "f-" + value; }
		public void setVal5(Object value) {  }

		public void setVal6(String value) {  }
		public void setVal6(Object value) {  }
		
	}
	
	@Test 
	public void testGetProperty() {
		TestObj obj = new TestObj();
		
		assertEquals("val1", Utils.getProperty(obj, "val1"));
		assertEquals("val2", Utils.getProperty(obj, "val2"));

		try {
			Utils.getProperty(obj, "xxx");
			fail();
		} catch( RuntimeException e ) 
		{ /* ok if raise exception */ }
		
	}  
	
	@Test
	public void testSetProperty() {
		TestObj obj = new TestObj();

		/* setter on private field */
		Utils.setPropery(obj, "val1", "uno");
		assertEquals( "uno", Utils.getProperty(obj, "val1"));
		
		/* setter on public setter method */
		Utils.setPropery(obj, "val2", "dos");
		assertEquals( "f-dos", obj.getVal2());
		
		/* setter with null method using field definition */
		Utils.setPropery(obj, "val3", null);
		assertEquals( "f-null", obj.val3 );

		/* setter with null and w// field declaration */
		obj.val3 = "xxx";
		Utils.setPropery(obj, "val4", null);
		assertNull( obj.val3 );

		/* multiple setter with null and w/t field declaration */
		Utils.setPropery(obj, "val5", null);
		assertEquals( "f-null", obj.val5 );

		/* multiple setter with null and w/o field declaration */
		try {
			Utils.setPropery(obj, "val6", null);
			fail();
		} catch( RuntimeException e ) { /* have to fail */ }
		
	}

	static class TestItem {
		private String field;
		public TestItem(String val) { field=val; }
	}
	
	@Test 
	public void testGetArrayItems() {
		
		/* must raise an exception when the array is null */
		try {
			Utils.getItems((Object[])null, "field", "value");
			fail();
		} catch(Exception e ) { /* OK */  }

		/* must raise an exception when the property name is null */
		try {
			Utils.getItems(new Object[0], null, "value");
			fail();
		} catch(Exception e ) { /* OK */  }
		
	
		TestItem[] array = { new TestItem("uno"), new TestItem("dos"), new TestItem("dos"), new TestItem("tres") };
		assertEquals( 1, Utils.getItems(array, "field", "uno").size() );
		assertEquals( 2, Utils.getItems(array, "field", "dos").size() );
		assertEquals( 1, Utils.getItems(array, "field", "tres").size() );
		assertEquals( 0, Utils.getItems(array, "field", "cuatro").size() );
	
		/* accessing a non existing property will raise an exception */
		try {
			Utils.getItems(array, "none", null);
			fail();
		}
		catch( Exception e ) { /* OK */ }
	}
	
	@Test
	public void testGetListItems() {

		List<TestItem> list = new ArrayList<TestItem>();
		list.add( new TestItem("uno") );
		list.add( new TestItem("dos") );
		list.add( new TestItem("dos") );
		list.add( new TestItem("tres") );
		list.add( null );
		
		assertEquals( 1, Utils.getItems(list, "field", "uno").size() );
		assertEquals( 2, Utils.getItems(list, "field", "dos").size() );
		assertEquals( 1, Utils.getItems(list, "field", "tres").size() );
		assertEquals( 0, Utils.getItems(list, "field", "cuatro").size() );
		
	}

	@Test 
	public void testArrayContainsChar() {
		
		assertTrue( Utils.contains(new char[] {'a', 'b', 'c'}, 'b') );
		assertFalse( Utils.contains(new char[] {'a', 'b', 'c'}, 'z') );
	}
	
	@Test 
	public void testArrayContainsInteger() {
		assertTrue( Utils.contains(new int[] {1, 2, 3}, 3) );
		assertFalse( Utils.contains(new int[] {1, 2, 3}, 4) );
	}

	@Test 
	public void testArrayContainsLong() {
		assertTrue( Utils.contains(new long[] {1, 2, 3}, 3) );
		assertFalse( Utils.contains(new long[] {1, 2, 3}, 4) );
	}
	
	@Test 
	public void testArrayContainsFloat() {
		assertTrue( Utils.contains(new float[] {1, 2, 3}, 3) );
		assertFalse( Utils.contains(new float[] {1, 2, 3}, 4) );
	}
	
	@Test 
	public void testArrayContainsDouble() {
		assertTrue( Utils.contains(new double[] {1, 2, 3}, 3) );
		assertFalse( Utils.contains(new double[] {1, 2, 3}, 4) );
	}

	@Test
	public void testAsTimeString() {
		assertEquals( "500 ms", Utils.asTimeString(500));
		assertEquals( "1 secs", Utils.asTimeString(1000));
		assertEquals( "1 secs", Utils.asTimeString(1100));
		assertEquals( "2 secs", Utils.asTimeString(1800));

		assertEquals( "1 mins", Utils.asTimeString(1 * 60 * 1000));
		assertEquals( "2 mins", Utils.asTimeString(2 * 60 * 1000));

		assertEquals( "1 hours", Utils.asTimeString(1 * 60 * 60 * 1000));
		assertEquals( "2 hours", Utils.asTimeString(2 * 60 * 60 * 1000));

		assertEquals( "1 days", Utils.asTimeString(25 * 60 * 60 * 1000));
		assertEquals( "2 days", Utils.asTimeString(2 * 24 * 60 * 60 * 1000));
		
	}
	
	@Test
	public void testCamelize() {
		
		assertEquals( "isCamelCase", Utils.camelize("is.camel.case"));
		assertEquals( "isCamelCase", Utils.camelize("is camel case"));
		assertEquals( "isCamelCase", Utils.camelize("is:camel:case"));
		assertEquals( "isCamelCase", Utils.camelize("is_camel_case"));
		assertEquals( "iscamelcase", Utils.camelize("iscamelcase"));
		assertEquals( null, Utils.camelize(null));
	}
	
	@Test 
	public void testCreateNoArgs() {
		assertNotNull(Utils.create(String.class));
	}

	public void testCreateArgs() {
		assertEquals("Hola", Utils.create(String.class,"Hola"));
	}
	
	@Test 
	public void testNextUniqueFile() {
		assertEquals(new File("name.1"), Utils.nextUniqueFile(new File("name")));
		assertEquals(new File("name.2"), Utils.nextUniqueFile(new File("name.1")));
		assertEquals(new File("name.1.zip"), Utils.nextUniqueFile(new File("name.zip")));
		assertEquals(new File("name.2.zip"), Utils.nextUniqueFile(new File("name.1.zip")));
		assertEquals(new File("name.10.zip"), Utils.nextUniqueFile(new File("name.9.zip")));
		assertEquals(new File("name.1."), Utils.nextUniqueFile(new File("name.")));
		assertEquals(new File("name.1.tar.gz"), Utils.nextUniqueFile(new File("name.tar.gz")));

		assertEquals(new File("/root/name.1"), Utils.nextUniqueFile(new File("/root/name")));
		assertEquals(new File("/root/name.1.tar.gz"), Utils.nextUniqueFile(new File("/root/name.tar.gz")));
		assertEquals(new File("/root/name.10.tar.gz"), Utils.nextUniqueFile(new File("/root/name.9.tar.gz")));

		assertEquals(new File("/root/x.y.z/name.1"), Utils.nextUniqueFile(new File("/root/x.y.z/name")));
		assertEquals(new File("/root/x.y.z/name.1.tar.gz"), Utils.nextUniqueFile(new File("/root/x.y.z/name.tar.gz")));
		assertEquals(new File("/root/x.y.z/name.10.tar.gz"), Utils.nextUniqueFile(new File("/root/x.y.z/name.9.tar.gz")));
		
		
	}

}

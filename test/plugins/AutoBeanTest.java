package plugins;

import org.junit.Test;

import play.test.UnitTest;
import util.Utils;

public class AutoBeanTest extends UnitTest {

	/*
	 * simple class. they don't have any constructor or equals method. 
	 * 
	 * These methods are provided automatically using the @AutoBean annotation 
	 */
	@AutoBean
  	public static class Base {
		public String field1;
		public int field2;  
	}  
	
	@AutoBean
	public static class Extend extends Base {
		public String field3;
	}
	
	
	@Test 
	public void testToString() {
		Extend bean = new Extend();
		bean.field1 = "uno";
		bean.field2 = 2;
		bean.field3 = "tres";
		
		assertEquals("Extend[field1='uno', field2=2, field3='tres']", bean.toString());
	}
	
	@Test 
	public void testCopy() {
		Extend bean = new Extend();
		bean.field1 = "val1";
		bean.field2 = 99;
		bean.field3 = "val3";
		
		Extend copy = Utils.copy(bean);
		
		assertEquals(bean.field1, copy.field1);
		assertEquals(bean.field2, copy.field2);
		assertEquals(bean.field3, copy.field3);
	}
	
	@Test 
	public void testEquals() {
		Extend bean1 = new Extend();
		bean1.field1 = "val1";
		bean1.field2 = 99;
		bean1.field3 = "val3";
		
		Extend bean2 = new Extend();
		bean2.field1 = "val1";
		bean2.field2 = 99;
		bean2.field3 = "val3";
		
		
		assertTrue(bean1.equals(bean2));
		
		/* anti-test: check that changing a field in the super class it will fail  */
		bean2.field1 = null;
		assertFalse(bean1.equals(bean2));
	}

	@Test 
	public void testHashCode() {
		Extend bean1 = new Extend();
		bean1.field1 = "val1";
		bean1.field2 = 99;
		bean1.field3 = "val3";
		
		Extend bean2 = new Extend();
		bean2.field1 = "val1";
		bean2.field2 = 99;
		bean2.field3 = "val3";
		
		
		assertTrue(bean1.hashCode() == bean2.hashCode());
		
		/* anti-test: check that changing a field in the super class it will fail  */
		bean2.field1 = null;
		assertFalse(bean1.hashCode() == bean2.hashCode());
	}
	
}


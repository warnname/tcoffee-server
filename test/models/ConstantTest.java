package models;

import org.junit.Test;

import play.test.UnitTest;
import util.Utils;
import util.XStreamHelper;

public class ConstantTest extends UnitTest {
	
//	@Test
	public void testFromXml() {
		String xml = "<const name='XXX' >the value</const>";
		
		Constant _const = XStreamHelper.fromXML(xml);
		assertEquals( "XXX", _const.getName());
		assertEquals( "the value", _const.getValue());
	}
	
	@Test
	public void testCopy() {
		
		Constant _const = new Constant("alfa", "1");
		Constant copy = Utils.copy(_const);
		
		assertEquals( "alfa", copy.getName());
		assertEquals( "1", copy.getValue());
		
	}
	
	@Test 
	public void testEquals() { 
		Constant c1 = new Constant("alfa", "1");
		Constant c2 = new Constant("alfa", "1");
		
		assertEquals(c2,c1);
	}

	@Test
	public void testHash() { 
		Constant c1 = new Constant("alfa", "1");
		Constant c2 = new Constant("alfa", "1");
		
		assertEquals(c1.hashCode(),c2.hashCode());
		
	}

}

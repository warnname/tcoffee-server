package models;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.Utils;
import util.XStreamHelper;

public class CmdLineTest extends UnitTest {
	
	@Before
	public void register() {
		TestHelper.module("field1=Value 1", "x=1", "x=2", "x=3");
	}
	
	@Test 
	public void testCopy() {
		CmdArgs args = new CmdArgs();
		args.put("c", "ece");
		args.put("a", "uno");
		args.put("z", "zeta");
		args.put("b", "dos");
		
		CmdArgs copy = Utils.copy(args);
		assertEquals( args.size(), copy.size() );
	}
	
	@Test 
	public void testGet() {
		CmdArgs args = new CmdArgs();

		assertNull( args.get("xxx") );
		
		args.put("xxx", "123");
		assertEquals("123", args.get("xxx"));
		
		
		args.put("another", "${field1}");
		assertEquals("Value 1", args.get("another"));
	}
	
	@Test 
	public void testPut() {
		CmdArgs args = new CmdArgs();
		args.put("a","b");
		args.put("x=y");
		args.put("z=");
		args.put("w");
		args.put("q 1"); // ALSO SPACE IS SUPPORTED AS SEPARATOR 
		assertEquals("b", args.get("a"));
		assertEquals("y", args.get("x"));
		assertEquals(null, args.get("z"));
		assertEquals(null, args.get("w"));
		assertEquals("1", args.get("q"));
	}

	@Test 
	public void testParse() {
		CmdArgs args = new CmdArgs();
		args.parse("-a=alfa -c=gamma -z=${field1} -b=beta"); 
		
		assertEquals("a", args.items.get(0).name);
		assertEquals("c", args.items.get(1).name);
		assertEquals("z", args.items.get(2).name);
		assertEquals("b", args.items.get(3).name);

		assertEquals("alfa", args.get("a"));
		assertEquals("beta", args.get("b"));
		assertEquals("gamma", args.get("c"));
		assertEquals("Value 1", args.get("z"));
		
	}
	
	@Test 
	public void testConstructorWithArgs() {
		CmdArgs args = new CmdArgs("-a=alfa -c=gamma -z=${field1} -b=beta");
		
		assertEquals("a", args.items.get(0).name);
		assertEquals("c", args.items.get(1).name);
		assertEquals("z", args.items.get(2).name);
		assertEquals("b", args.items.get(3).name);

		assertEquals("alfa", args.get("a"));
		assertEquals("beta", args.get("b"));
		assertEquals("gamma", args.get("c"));
		assertEquals("Value 1", args.get("z"));
		
	}
	
	@Test
	public void testFromXml() {
		String xml = "<args >-a=alfa -c=gamma -b=beta</args>";
		CmdArgs args = XStreamHelper.fromXML(xml);
		assertNotNull(args);
		assertEquals("a", args.items.get(0).name);
		assertEquals("c", args.items.get(1).name);
		assertEquals("b", args.items.get(2).name);
		
		assertEquals("alfa", args.get("a"));
		assertEquals("beta", args.get("b"));
		assertEquals("gamma", args.get("c"));
	}

	@Test
	public void testToXml() {
		CmdArgs args = new CmdArgs(); 
		args.put("a", "alfa");
		args.put("b", "beta");
		args.put("c", "${field1}");
		
		String xml = XStreamHelper.toXML(args);
		assertEquals("<args>-a=alfa -b=beta -c=${field1}</args>", xml);
	}
	
	public void testToCmdString() {
		CmdArgs args = new CmdArgs();
		args.put("a", "uno");
		args.put("b", "dos");
		args.put("c", "${field1}");
		
		assertEquals("-a=uno -b=dos -c=Value 1", args.toString());
	}
	
	@Test 
	public void testContains() {
		CmdArgs args = new CmdArgs();
		args.put("a", "uno");
		args.put("b", "dos");
		args.put("c", "${field1}");
		
		assertTrue( args.contains("a"));
		assertFalse( args.contains("z"));
	} 
	
	@Test 
	public void testAtOrder() {
		CmdArgs args = new CmdArgs();
		args.put("c", "ece");
		args.put("a", "uno");
		args.put("z", "zeta");
		args.put("b", "dos");
		
		/* assertes that have the same names and order */
		assertEquals("c", args.at(0));
		assertEquals("a", args.at(1));
		assertEquals("z", args.at(2));
		assertEquals("b", args.at(3));
	} 
	
	@Test 
	public void testCmdLineMultiple() {
		CmdArgs cmd = new CmdArgs("-a=${field1} -b -c= -d value -x=${x}");
		
		assertEquals("-a=Value 1 -b -c -d=value -x=1 -x=2 -x=3", cmd.toCmdLine()); 
	}

}

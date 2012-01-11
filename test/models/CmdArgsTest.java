package models;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.blackcoffee.commons.utils.CmdLineUtils;
import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.Utils;
import util.XStreamHelper;

public class CmdArgsTest extends UnitTest {
	
	@Before
	public void register() {
		TestHelper.init("field1=Value 1", "x=1", "x=2", "x=3", "xy=99");
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
		args.put("--q=1"); // it is possible to specify even the prefix
		assertEquals("b", args.get("a"));
		assertEquals("y", args.get("x"));
		assertEquals(null, args.get("z"));
		assertEquals(null, args.get("w"));
		assertEquals("1", args.get("q"));
	}
	
	@Test 
	public void testAdd() { 
		CmdArgs args = new CmdArgs();
		args.add("p","a");
		args.add("q","b");
		args.add("q","c");

		assertEquals("a", args.get("p"));
		assertEquals("b c", args.get("q"));
	}

	@Test 
	public void testGetList() { 
		CmdArgs args = new CmdArgs();
		args.put("a","b");
		args.put("x=1 2 3");
		
		assertEquals( Arrays.asList("b"), args.getAsList("a") );
		assertEquals( Arrays.asList("1","2","3"), args.getAsList("x"));
		
	}
	
	@Test 
	public void testParse() {
		CmdArgs args = new CmdArgs();
		args.parse("sample.fasta -a=alfa -c=gamma -z=${field1} -b=beta --flag"); 
		
		assertEquals("sample.fasta", args.getItems().get(0).name);
		assertEquals("a", args.getItems().get(1).name);
		assertEquals("c", args.getItems().get(2).name);
		assertEquals("z", args.getItems().get(3).name);
		assertEquals("b", args.getItems().get(4).name);
		assertEquals("flag", args.getItems().get(5).name);

		assertEquals("alfa", args.get("a"));
		assertEquals("beta", args.get("b"));
		assertEquals("gamma", args.get("c"));
		assertEquals("Value 1", args.get("z"));
		
		
		assertEquals( "sample.fasta -a=alfa -c=gamma -z=Value 1 -b=beta --flag", args.toCmdLine() );
		
	}
	
	@Test 
	public void testConstructorWithArgs() {
		CmdArgs args = new CmdArgs("-a=alfa -c=gamma -z=${field1} -b=beta");
		
		assertEquals("a", args.getItems().get(0).name);
		assertEquals("c", args.getItems().get(1).name);
		assertEquals("z", args.getItems().get(2).name);
		assertEquals("b", args.getItems().get(3).name);

		assertEquals("alfa", args.get("a"));
		assertEquals("beta", args.get("b"));
		assertEquals("gamma", args.get("c"));
		assertEquals("Value 1", args.get("z"));
		
	}

	
	@Test 
	public void testToCmdLine() {
		CmdArgs args = new CmdArgs("-a=alfa -c=gamma -z=val");
		assertEquals( "-a=alfa -c=gamma -z=val", args.toCmdLine() );

		args = new CmdArgs("-a=alfa\n-c=gamma \n-z=val");
		assertEquals( "-a=alfa -c=gamma -z=val", args.toCmdLine() );
	
	}	
	
	@Test
	public void testFromXml() {
		String xml = "<args >-a=alfa -c=gamma -b=beta</args>";
		CmdArgs args = XStreamHelper.fromXML(xml);
		assertNotNull(args);
		assertEquals("a", args.getItems().get(0).name);
		assertEquals("c", args.getItems().get(1).name);
		assertEquals("b", args.getItems().get(2).name);
		
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
		CmdArgs cmd = new CmdArgs("-a=${field1} -b -c= -d=value --x=${x} --x-y=${xy}");
		
		assertEquals("-a=Value 1 -b -c -d=value --x=1 --x=2 --x=3 --x-y=99", cmd.toCmdLine()); 
	}
	
	
	@Test
	public void testDynamicCommandLine() { 
		Service.current().getCtx().put("args", "-x=1 -y=2 -w=3 4 5 -z=hola");

		// in this case all the command line is specified by a single variable that
		// contains all the CL CmdArgs.CMD_OPTIONs
		CmdArgs cmd = new CmdArgs("${args}");
		assertEquals( "1", cmd.get("x") ); 
		assertEquals( "2", cmd.get("y") ); 
		assertEquals( "3 4 5", cmd.get("w") ); 
		assertEquals( "hola", cmd.get("z") ); 

	}
	

	
	
	@Test
	public void testTrim()  { 
		
		assertEquals( "x", " x \n ".trim() ); 
	}
	
	
	@Test 
	public void testPattern() { 
		Pattern p = Pattern.compile("[ \\t\\n\\x0B\\f\\r]-");
		
		Matcher m = p.matcher("hola -prop");
		assertTrue( m.find() );
		assertEquals( 4, m.start() );	

		m = p.matcher("hola \n-prop");
		assertTrue( m.find() );
		assertEquals( 5, m.start() );	

	}

	@Test
	public void testInvalidDashSeparator() { 
		// in this test the option 'wrong' uses a bad option separator, it is not a minus character '-' 
		// but a dash character (usually it came out when using word processor auto replacement) 
		assertEquals( "-opt 1 -wrong -more", CmdLineUtils.normalize("-opt 1 –wrong -more") );
	}
	
	
	
	@Test 
	public void testCmdLineSeparator() {
		CmdArgs cmd = new CmdArgs("-a=1 -b 2 --c=3 --d 4");
		
		assertEquals("-a=1 -b 2 --c=3 --d 4", cmd.toCmdLine()); 
	}	
}

package models;

import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.Utils;

public class ArgTest extends UnitTest {
	
	@Test 
	public void testCreate() {
		Arg arg = new Arg("x", "1");
		assertEquals("x", arg.name);
		assertEquals("1", arg.value);
	}
	
	@Test 
	public void testCopy() {
		Arg arg = new Arg("x", "1");
		Arg copy = Utils.copy(arg);
		
		assertEquals("x", copy.name);
		assertEquals("1", copy.value);
		assertEquals(arg, copy);
	}
	
	@Test
	public void testGetVal() {
		/*
		 * static value (i.e.) no variable to resolve 
		 */
		Arg arg = new Arg("x", "value");
		assertEquals( "value", arg.getVal() );
		
		/*
		 * null value
		 */
		arg = new Arg("y",null);
		assertEquals( null, arg.getVal() );
		
		/*
		 * empty value
		 */
		arg = new Arg("y","");
		assertEquals( null, arg.getVal() );
	} 
	
	@Test 
	public void testGetAll() {
		TestHelper.init("x=alfa", "y=beta", "y=delta");
		
		Arg arg = new Arg ("param", "${x}");
		assertEquals( 1, arg.getAll().size() );
		assertEquals( "alfa", arg.getAll().get(0) );

		arg = new Arg ("param", "${y}");
		assertEquals( 2, arg.getAll().size() );
		assertEquals( "beta", arg.getAll().get(0) );
		assertEquals( "delta", arg.getAll().get(1) );
		
	} 
	
	@Test
	public void testRawString() {
		Arg arg = new Arg("param", "${x} y ${z}" );
		assertEquals( "-param=${x} y ${z}", arg.toRawString() );
	}
	
	@Test 
	public void testToCmdListStatic() {
		Arg arg = new Arg("param", "value");
		assertEquals("-param=value", arg.toCmdLine());
	} 
	
	@Test 
	public void testToCmdLineFlag() {
		Arg arg = new Arg("flag",null);
		assertEquals("-flag", arg.toCmdLine());

		// same behavior with empty string
		arg = new Arg("flag","");
		assertEquals("-flag", arg.toCmdLine());
	} 
	
	@Test 
	public void testToCmdLineVars() {
		TestHelper.init("x=1", "y=2");
		
		Arg arg = new Arg("param", "${x}");
		assertEquals( "-param=1", arg.toCmdLine() );

		arg = new Arg("param", "abc${x}");
		assertEquals( "-param=abc1", arg.toCmdLine() );

		arg = new Arg("param", "${x}0${y}");
		assertEquals( "-param=102", arg.toCmdLine() );
	}
	
	@Test 
	public void testToCmdLineMultiple() {
		TestHelper.init("x=1", "x=2", "x=3", "z=99");
		
		Arg arg = new Arg("param", "${x}");
		assertEquals( "-param=1 -param=2 -param=3", arg.toCmdLine());
	}
	
	@Test 
	public void testToCmdLineMissingVar() {
		/*
		 * argument with all missing variable are removed 
		 */
		TestHelper.init();
		Arg arg = new Arg("param", "${x} ${y}");
		assertEquals( "", arg.toCmdLine());

		/*
		 * if at lest one exists it is mantained 
		 */
		TestHelper.init("z=3");
		arg = new Arg("param","${x} ${y} ${z}");
		assertEquals("-param=3", arg.toCmdLine());
		
	}

}

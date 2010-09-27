package models;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;
import util.XStreamHelper;

public class EvalTest extends UnitTest {

	@Before
	public void init() {
		TestHelper.init("field1=alfa", "field2=beta");
	}

	@Test 
	public void testFromXml() {
		String xml = "<eval>field value is ${field1}</eval>";
		Eval eval = XStreamHelper.fromXML(xml);
		assertEquals( "field value is alfa", eval.eval() ); 
	}
	
	@Test 
	public void testVariables() {
		assertEquals("simple", new Eval("simple").eval());
		assertEquals("alfa", new Eval("${field1}").eval());
		assertEquals("beta", new Eval("${field2}").eval());
		assertEquals("a alfa and beta with more text", new Eval("a ${field1} and ${field2} with more text").eval());
	}
	
	@Test 
	public void testIsStatic() {
		Eval eval = new Eval("ciao");
		assertTrue( eval.isStatic() );
	} 

	@Test 
	public void testEval() {
		Eval eval = new Eval("ciao");
		assertEquals("ciao", eval.eval());
		
		eval = new Eval("${field1}");
		assertEquals("alfa", eval.eval());
		
	} 
	
	@Test 
	public void testPrefetch() {
		Eval eval = new Eval("${x} ${y}");
		
		assertEquals( 2, eval.vars.size() );
		assertEquals( "x", eval.vars.get(0) );
		assertEquals( "y", eval.vars.get(1) );
	}
	
	@Test 
	public void testEvalContext() {
		Map<String,Object> ctx = new HashMap<String, Object>();
		ctx.put("x", "hola");
		ctx.put("y", new File("test.txt"));
		
		Eval eval = new Eval("${x} - ${y}");
		String result = eval.eval(ctx);
		
		assertEquals( "hola - test.txt", result );
	} 
	
}

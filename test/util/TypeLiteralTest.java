package util;

import org.junit.Test;

import play.test.UnitTest;

public class TypeLiteralTest extends UnitTest {
	
	@Test 
	public void testType() {
		TypeLiteral<String> type = new TypeLiteral<String>() {};
		assertEquals( String.class, type.getType() );
	} 
	
	
	@Test 
	public void testNewInstance() {
		assertEquals( "", new TypeLiteral<String>() {}.newInstance() );
		assertEquals( "Hola", new TypeLiteral<String>() {}.newInstance("Hola") );
	} 
	
	
}

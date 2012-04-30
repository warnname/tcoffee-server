package util;

import org.junit.Test;

import play.test.UnitTest;
import util.DSL.Pair;

public class DslTest extends UnitTest{

	@Test
	public void testPair() {
		Pair pair = Pair.create("alpha=1");
		assertEquals( "alpha", pair.first );
		assertEquals( "1", pair.second );
		
		pair = Pair.create("alpha");
		assertEquals( "alpha", pair.first );
		assertEquals( null, pair.second );
		
		pair = Pair.create("alpha","=", "default");
		assertEquals( "alpha", pair.first );
		assertEquals( "default", pair.second );
		
		pair = Pair.create("alpha:1",":", "def");
		assertEquals( "alpha", pair.first );
		assertEquals( "1", pair.second );
		
	} 
	
}

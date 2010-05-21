package util;

import java.io.StringReader;

import org.junit.Test;

import play.test.UnitTest;

public class ReaderIteratorTest extends UnitTest {
	
	@Test
	public void testReaderIterator() {
		StringReader reader = new StringReader("one\ntwo\nthree");
		
		int i=0;
		String[] lines = new String[3];
		for( String line : new ReaderIterator(reader)) {
			lines[i++] = line;
		}
		
		assertEquals("one", lines[0]);
		assertEquals("two", lines[1]);
		assertEquals("three", lines[2]);
	} 

}

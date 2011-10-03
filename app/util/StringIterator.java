package util;

import java.io.StringReader;

/**
 * Split a string by each newline char an iterator over each line 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class StringIterator extends ReaderIterator {

	public StringIterator( String str ) { 
		super(new StringReader(str));
	}
}

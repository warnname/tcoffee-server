package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import play.Logger;
import exception.QuickException;

public class ReaderIterator implements Iterable<String> {

	private BufferedReader reader;
	
	private boolean autoClose;

	
	/**
	 * Public constructor accepting any reader. 
	 * 
	 * @param reader 
	 */
	public ReaderIterator( Reader reader, boolean close ) {
		this.reader = reader instanceof BufferedReader 
					? (BufferedReader) reader 
					: new BufferedReader(reader);
					
		this.autoClose = close;
	}
	
	public ReaderIterator( Reader reader ) {
		this(reader,true);
	}

	public Iterator<String> iterator() {
		return new Iterator<String>() {

			String nextLine = getLine();
			
			String getLine() {
				try {
					return reader.readLine();
				} catch( IOException e ) {
					throw new QuickException(e, "Unable to read line on buffered reader");
				}
			}
			
			
			public boolean hasNext() {
				return nextLine != null;
			}

			public String next() {
				String result = nextLine;
				nextLine = getLine();
				if( nextLine == null && autoClose ) {
					try { reader.close(); } catch( IOException e ) { Logger.warn(e,"Unable to close iterator reader"); } 
				}
				return result;
			}

			public void remove() {
				throw new UnsupportedOperationException("Remove operation is not supported on ReaderIterator");
			};
		};
	}


}

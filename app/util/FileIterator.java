package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import exception.QuickException;

/**
 * Let to iterate over a text file line by line. Example: 
 * 
 * <pre>
 * for( String line :  new FileIterator(tcoffee.getErrFile()) ) {
 * 	:
 *	do something with lines 
 *	:
 * }
 * </pre>
 * @author ptommaso
 *
 */
public class FileIterator extends ReaderIterator {

	public FileIterator( File file ) {
		super(reader(file),true);
	}
	
	public FileIterator( FileReader file ) {
		super(file,true);
	}
	
	public FileIterator( String path ) {
		this(new File(path));
	}
	
	
	static FileReader reader( File file ) {
		try {
			return new FileReader(file);
		}
		catch( FileNotFoundException e ) {
			throw new QuickException(e, "Unable to find file: %s", file);
		}
	}
}

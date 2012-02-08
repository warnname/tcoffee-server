package controllers;

import java.io.File;

import org.junit.Test;

import play.test.UnitTest;

public class DataTest extends UnitTest {

	@Test 
	public void testNormalize( ) {
		
		assertEquals( new File("_file.fasta"), Data.normalize(new File("-file.fasta")) ); 
		assertEquals( new File("_file-name.fasta"), Data.normalize(new File("-file-name.fasta")) ); 
		assertEquals( new File("file_name_with_symbols.fasta"), Data.normalize(new File("file?name&with:symbols.fasta")) ); 
		assertEquals( new File("file_name__with_parenthesis.fasta"), Data.normalize(new File("file(name) with parenthesis.fasta")) ); 
		assertEquals( new File("garzon.fasta"), Data.normalize(new File("garzón.fasta")) ); 
		assertEquals( new File("Aaaeeoouuuiii__.fasta"), Data.normalize(new File("ÀàåèéòóùúüìíîøΩ.fasta")) ); 
		assertEquals( new File("File_name.fasta"), Data.normalize(new File("File%name.fasta")) ); 

		assertEquals( new File("simple_file_name.fasta"), Data.normalize(new File("simple file name.fasta")) ); 
		assertEquals( new File("/with/some/path/_file-name.fasta"), Data.normalize(new File("/with/some/path/-file-name.fasta")) ); 
		assertEquals( new File("/with/some/path/simple_file_name.fasta"), Data.normalize(new File("/with/some/path/simple file name.fasta")) ); 
		assertEquals( new File("relative/_file.fasta"), Data.normalize(new File("relative/-file.fasta")) ); 

		
		assertEquals( new File("/any/path"), Data.normalize(new File("/any/path/")) ); 
	}  
	
}

package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;

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
	
	@Test
	public void testNewick() {
		
		String NEWICK = "((TNFSF2:107.00000,TNFSF4:107.00000) 100:107.00000,TNFSF1:107.00000);";
		String EXPECTED = "{\"name\":\"\", \"children\":[{\"name\":\"\", \"size\":107.0, \"children\":[{\"name\":\"TNFSF2\", \"size\":107.0}, {\"name\":\"TNFSF4\", \"size\":107.0}]}, {\"name\":\"TNFSF1\", \"size\":107.0}]}";
		String result = Data.treeToJson(NEWICK);

		assertEquals(EXPECTED, result);
		
	} 
	
	@Test()
	public void testNewickFile() throws FileNotFoundException {
		
		StringBuilder json = new StringBuilder();
		Data.treeToJson( new FileReader(TestHelper.file("/newick_big_norm.txt")) , json);
	} 

	@Test
	public void testNewickNormalization() throws FileNotFoundException {
		
		String norm = Data.normalizeNewick(TestHelper.file("/newick_big.txt"));
		String json = Data.treeToJson( norm );
		System.out.println(json.toString());
	} 
	
	
}

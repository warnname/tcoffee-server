package models;

import java.io.File;

import org.junit.Test;

import util.TestHelper;
import exception.CommandException;

public class ApdbModeTest extends XModeCoffeeTest {

	@Test(timeout=5*60*1000) 
	public void testApdbMode() throws CommandException {
		File input = TestHelper.file("/sample-clustalw.txt");
		testMode("apdb", 
				input, 
				"other_pg=apdb",  // <-- must be the FIRST 
				"aln=sample-clustalw.txt", 
	  			"apdb_outfile=default", 
	  			"outfile=default", 
	  			"io_format=hsg3",  
	  			"maximum_distance=5",
	  			"md_threshold=1.0",
	  			"similarity_threshold=50",
	  			"output=score_html", 
	  			"run_name=result"	
	  		);	
	}
	
}

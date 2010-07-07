package models;

import org.junit.Test;

import util.TestHelper;
import exception.CommandException;

public class ExpressoTest extends XModeCoffeeTest {

	@Test
	public void empty() {}
	
	void testWithMethod(String mode, String  method) throws CommandException {
		
		new TCoffeeTestRunner("tcoffee-" + method)
		
		.input( TestHelper.file("/sample.fasta") )
	
		.args( 
				"in=sample.fasta",
				"mode=" + mode, 
				"method=" + method, 

				/* defaults */
			    "output=score_html clustalw_aln fasta_aln phylip",
				"case=upper",
				"seqnos=on",
				"outorder=input",
				"outfile=tcoffee",
				"cache=no",
				"remove_template_file=1"
			)
				
		.go();		
	}	
	
//	@Test(timeout=60000)
//	public void testExpressoWith_best_pair4prot() throws Exception {
//		testWithMethod("expresso", "best_pair4prot");
//	}	
//
//	@Test(timeout=60000)
//	public void testExpressoWith_fast_pair() throws Exception {
//		testWithMethod("expresso", "fast_pair");
//	}	
//	@Test(timeout=60000)
//	public void testExpressoWith_clustalw_pair() throws Exception {
//		testWithMethod("expresso", "clustalw_pair");
//	}	
//	@Test(timeout=60000)
//	public void testExpressoWith_lalign_id_pair() throws Exception {
//		testWithMethod("expresso", "lalign_id_pair");
//	}	
//	@Test(timeout=60000)
//	public void testExpressoWith_slow_pair() throws Exception {
//		testWithMethod("expresso", "slow_pair");
//	}	
//	@Test(timeout=60000)
//	public void testExpressoWith_proba_pair() throws Exception {
//		testWithMethod("expresso", "proba_pair");
//	}	
//	@Test(timeout=60000)
//	public void testExpressoWith_sap_pair() throws Exception {
//		testWithMethod("expresso", "sap_pair");
//	}	
//	@Test(timeout=60000)
//	public void testExpressoWith_fugue_pair() throws Exception {
//		testWithMethod("expresso", "fugue_pair");
//	}	
//	@Test(timeout=60000)
//	public void testExpressoWith_TMalign_pair() throws Exception {
//		testWithMethod("expresso", "TMalign_pair");
//	}	
//	@Test(timeout=60000)
//	public void testExpressoWith_mustang_pair() throws Exception {
//		testWithMethod("expresso", "mustang_pair");
//	}	
	
	
}

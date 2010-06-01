package models;

import org.junit.Test;

import util.TestHelper;

public class CombineModeTest extends XModeCoffeeTest {
	
	@Test
	public void testCombine() {
		new TestRunner("combine")
		
			.input( TestHelper.file("/sample-clustalw.txt") )
		
			.args( "in=sample-clustalw.txt", 
					"in=sample-clustalw.txt", 
					"output=score_html", 
					"evaluate_mode=t_coffee_slow",
					"maxnseq=80",
					"maxlen=2000", 
					"run_name=result"	)
					
			.go();
	}

}

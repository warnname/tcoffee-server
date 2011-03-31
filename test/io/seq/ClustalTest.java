package io.seq;

import io.seq.Clustal.Block;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;

public class ClustalTest extends UnitTest {

	@Test
	public void testClustalHeader() { 
		Clustal clustal = new Clustal();
		
		assertEquals( "CLUSTAL W (1.82) multiple sequence alignment", clustal.parseHeader("CLUSTAL W (1.82) multiple sequence alignment") );
		assertEquals( "CLUSTAL W (2)", clustal.parseHeader("CLUSTAL W (2)") );
	

		/*
		 * wrong header 
		 */
		try { 
			clustal.parseHeader("CLUSTAL (1.82) multiple sequence alignment");
			fail();
		}
		catch (FormatParseException e) {
		}

		/* 
		 * missing version number 
		 */
		try { 
			clustal.parseHeader("CLUSTAL W");
			fail();
		}
		catch (FormatParseException e) {
		}
	
	}
	
	@Test 
	public void testParseBlock() throws IOException { 
		String seqs = 
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP";
		
		Clustal clustal = new Clustal();
		Block block = clustal.parseBlock( new StringReader(seqs) );
		
		assertEquals( 5, block.keyMaxLength );
		assertEquals( 3, block.list.size() );
		assertEquals( "1aboA", block.list.get(0).key );
		assertEquals( "1ycsB", block.list.get(1).key );
		assertEquals( "1pht", block.list.get(2).key );

		assertEquals( "NGQGWVPSNYITPVN------", block.list.get(0).value );
		assertEquals( "DKEGYVPRNLLGLYP------", block.list.get(1).value );
		assertEquals( "GERGDFPGTYVEYIGRKKISP", block.list.get(2).value );

	
		String result = 
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n";
		
		assertEquals( result, block.toString() );
	}

	@Test 
	public void testParseBlockExtended() throws IOException { 
		String seqs = 
			"1aboA  NGQGWVPSNYITPVN------ 20\n" +
			"1ycsB  DKEGYVPRNLLGLYP------ 20\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP 30\n" +
			"           ***:**";
		
		Clustal clustal = new Clustal();
		Block block = clustal.parseBlock( new StringReader(seqs) );
		
		assertEquals( 3, block.list.size() );
		assertEquals( "1aboA", block.list.get(0).key );
		assertEquals( "1ycsB", block.list.get(1).key );
		assertEquals( "1pht", block.list.get(2).key );

		assertEquals( "NGQGWVPSNYITPVN------", block.list.get(0).value );
		assertEquals( "DKEGYVPRNLLGLYP------", block.list.get(1).value );
		assertEquals( "GERGDFPGTYVEYIGRKKISP", block.list.get(2).value );
	}
	

	@Test 
	public void testParseBlockFail() throws IOException { 
		String seqs = 
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsBxxDKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n";
		
		Clustal clustal = new Clustal();
		try { 
			clustal.parseBlock( new StringReader(seqs) );
			fail();
		}
		catch( FormatParseException e ) { 
			// this exception have to be thrown
		}
	}
	
	@Test 
	public void testBlockGetKeys() throws IOException { 
			String seqs = 
				"1aboA  NGQGWVPSNYITPVN------\n" +
				"1ycsB  DKEGYVPRNLLGLYP------\n" +
				"1pht   GERGDFPGTYVEYIGRKKISP";
			
			Clustal clustal = new Clustal();
			Block block = clustal.parseBlock( new StringReader(seqs) ); 
			assertEquals( 3, block.getKeysList().size() );
			assertEquals( "1aboA", block.getKeysList().get(0) );
			assertEquals( "1ycsB", block.getKeysList().get(1) );
			assertEquals( "1pht", block.getKeysList().get(2) );
	}
	
	@Test
	public void testParse() throws IOException { 
		String seqs = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n";
		
		
		Clustal clustal = new Clustal();
		clustal.parse( new StringReader(seqs) );

		assertEquals( 2, clustal.blocks.size() );
		assertEquals( 3, clustal.sequences.size() );

		assertEquals( "1aboA", clustal.sequences.get(0).header );
		assertEquals( "1ycsB", clustal.sequences.get(1).header );
		assertEquals( "1pht", clustal.sequences.get(2).header );
	
		assertEquals( "NGQGWVPSNYITPVNNGQGWVPSNYITPVN", clustal.sequences.get(0).value );
		assertEquals( "DKEGYVPRNLLGLYPDKEGYVPRNLLGLYP", clustal.sequences.get(1).value );
		assertEquals( "GERGDFPGTYVEYIGRKKISPGERGDFPGTYVEYIGRKKISP", clustal.sequences.get(2).value );
	
	}
	
	@Test 
	public void testIsBlockTerminated() { 
		assertTrue( Clustal.isBlockTermination("   ") );
		assertTrue( Clustal.isBlockTermination("  .*. ") );
		assertFalse( Clustal.isBlockTermination("xxx") );
	}
	
	
	@Test 
	public void testIsValidFile1() {
		assertTrue( Clustal.isValid( TestHelper.file("/clustal-sample.txt") , Alphabet.AminoAcid.INSTANCE) );
	}

	@Test 
	public void testIsValidFile2() {
		assertTrue( Clustal.isValid( TestHelper.file("/clustal-sample-2.txt") , Alphabet.AminoAcid.INSTANCE) );
	}

	@Test 
	public void testIsValidFile3() {
		assertTrue( Clustal.isValid( TestHelper.file("/clustal-sample-3.txt") , Alphabet.AminoAcid.INSTANCE) );
	}
	
	
	@Test 
	public void testDna() { 
		String seqs = 
			"CLUSTAL W (1.82)\n" +
			"\n" +
			"1aboA  ATCGCGATCATATCG------\n" +
			"1ycsB  CGATCGATCGATCGT------\n" +
			"1pht   ATCGATCGGCTAAAGCTATTA\n" +
			"\n" +
			"1aboA  ACATTCATTATCTAA------\n" +
			"1ycsB  CGAGCTAGCATATCT------\n" +
			"1pht   ATCAGCATGCAGCATGCGATT\n";
		
		assertTrue(Clustal.isValid(seqs, Alphabet.Dna.INSTANCE));
		assertFalse(Clustal.isValid(seqs, Alphabet.Rna.INSTANCE));
				
	}
	
	@Test 
	public void testProteins() { 
		String seqs = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n";
		
		assertTrue(Clustal.isValid(seqs, Alphabet.AminoAcid.INSTANCE));
		assertFalse(Clustal.isValid(seqs, Alphabet.Rna.INSTANCE));
		assertFalse(Clustal.isValid(seqs, Alphabet.Dna.INSTANCE));
		assertFalse(Clustal.isValid(seqs, Alphabet.NucleicAcid.INSTANCE));
				
	}	
	
	@Test 
	public void testToString() { 
		String seqs = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------ 20\n" +
			"1ycsB  DKEGYVPRNLLGLYP------ 30\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP 20\n" +
			"         .:.:::****            \n" +
			"\n\n\n\n" +
			"1aboA  NGQGWVPSNYITPVN------ 10\n" +
			"1ycsB  DKEGYVPRNLLGLYP------ 10\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP 4\n";
		

		Clustal clustal = new Clustal();
		clustal.parse(seqs);
		
		String result = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n";
		
		
		System.out.println(clustal.toString());;
		assertEquals( result, clustal.toString() );
	}		
}

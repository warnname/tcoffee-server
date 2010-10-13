package models;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;

public class FastaTest extends UnitTest {

	@Test
	public void testFasta() throws IOException {
		
		Fasta fasta = new Fasta(Fasta.AminoAcid.INSTANCE);
		fasta.parse(TestHelper.file("/test.fasta"));
		
		assertNotNull(fasta.sequences);
		//assertEquals(5, fasta.sequences.size());
		
		assertEquals("1aboA", fasta.sequences.get(0).header);
		assertEquals("NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPSNYITPVN", fasta.sequences.get(0).value);
		assertEquals(">1aboA|NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPSNYITPVN", fasta.sequences.get(0).toString());
		
		assertEquals("1ycsB", fasta.sequences.get(1).header);
		assertEquals("KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGYVPRNLLGLYP", fasta.sequences.get(1).value);

		assertEquals("1pht", fasta.sequences.get(2).header);
		assertEquals("GYQYRALYDYKKEREEDIDLHLGDILTVNKGSLVALGFSDGQEARPEEIGWLNGYNETTGERGDFPGTYVEYIGRKKISP", fasta.sequences.get(2).value);

		assertEquals("1vie", fasta.sequences.get(3).header);
		assertEquals("DRVRKKSGAAWQGQIVGWYCTNLTPEGYAVESEAHPGSVQIYPVAALERIN", fasta.sequences.get(3).value);

		assertEquals("1ihvA", fasta.sequences.get(4).header);
		assertEquals("NFRVYYRDSRDPVWKGPAKLLWKGEGAVVIQDNSDIKVVPRRKAKIIRD", fasta.sequences.get(4).value);

	}
	
	@Test 
	public void testIsValidFile() {
		assertTrue( Fasta.isValid(TestHelper.file("/test.fasta"), Fasta.AminoAcid.INSTANCE) );
	}

	@Test 
	public void testNotValidFile() {
		assertFalse( Fasta.isValid(TestHelper.sampleLog(), Fasta.AminoAcid.INSTANCE) );
		assertFalse( Fasta.isValid(new File("XXX"), Fasta.AminoAcid.INSTANCE ));
	}

	@Test
	public void testIsValidNucleicString() { 
		assertTrue( Fasta.isValid(">1aboA\nACGTGGCU", Fasta.NucleicAcid.INSTANCE) );
		assertFalse( Fasta.isValid(">1aboA\nACGTGGCS", Fasta.NucleicAcid.INSTANCE) );
	}

	@Test
	public void testIsValidDnaString() { 
		assertTrue( Fasta.isValid(">1aboA\nACGTACGT", Fasta.Dna.INSTANCE) );
		assertFalse( Fasta.isValid(">1aboA\nACGTACGU", Fasta.Dna.INSTANCE) );
	}

	@Test
	public void testIsValidRnaString() { 
		assertFalse( Fasta.isValid(">1aboA\nACGTACGT", Fasta.Rna.INSTANCE) );
		assertTrue( Fasta.isValid(">1aboA\nACGUACGU", Fasta.Rna.INSTANCE) );
	}	
	
	@Test
	public void testIsValidString() { 
		assertTrue( Fasta.isValid(">1aboA\nNLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPSNYITPVN", Fasta.AminoAcid.INSTANCE) );
	}

	@Test
	public void testIsValidLowerString() { 
		assertTrue( Fasta.isValid(">1aboA\nnlfvalydfvasgdntlsitkgeklrvlgynhngewceaqtkngqgwvpsnyitpvn", Fasta.AminoAcid.INSTANCE) );
	}
	
	@Test
	public void testNotValidString() { 
		assertFalse( Fasta.isValid("XXXX", Fasta.AminoAcid.INSTANCE) );
	}
	
	
	
}

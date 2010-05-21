package models;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;

public class FastaTest extends UnitTest {

	@Test
	public void testFasta() throws IOException {
		
		Fasta fasta = new Fasta(TestHelper.file("/test.fasta"));
		
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
		assertTrue( Fasta.isValid(TestHelper.file("/test.fasta")) );
	}

	@Test 
	public void testNotValidFile() {
		assertFalse( Fasta.isValid(TestHelper.sampleLog()) );
		assertFalse( Fasta.isValid(new File("XXX")) );
	}
	
	@Test
	public void testIsValidString() { 
		assertTrue( Fasta.isValid(">1aboA\nNLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPSNYITPVN") );
	}
	
	@Test
	public void testNotValidString() { 
		assertFalse( Fasta.isValid("XXXX") );
	}
	
	
	
}

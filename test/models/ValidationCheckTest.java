package models;

import org.junit.Test;

import play.data.validation.Validation;
import play.libs.IO;
import play.test.UnitTest;
import util.TestHelper;
import util.Utils;
import util.XStreamHelper;

public class ValidationCheckTest extends UnitTest {
	
	@Test
	public void testFromXml() {
		
		String xml = 
			"<validation " +
			"  required='true' " +
			"  format='TEXT'" +
			"  min='5'" +
			"  max='50'" +
			"  minlen='0'" +
			"  maxlen='10'" +
			"  minnum='1'" +
			"  maxnum='20'" +
			"  pattern='xyz' "+
			">" +
			"<required-error>This field is required</required-error>" +
			"<min-error>MIN value</min-error>" +
			"<max-error>MAX value</max-error>" +
			"<minlen-error>MINLEN value</minlen-error>" +
			"<minnum-error>MINNUM value</minnum-error>" +
			"<maxlen-error>MAXLEN value</maxlen-error>" +
			"<maxnum-error>MAXNUM value</maxnum-error>" +
			"<pattern-error>PATTERN value</pattern-error>" +
			"<script file='/some/file' >doThis() doThat()</script>" +
			"</validation>";
		
		ValidationCheck check = XStreamHelper.fromXML(xml);
		assertEquals( true, check.required );
		assertEquals( "TEXT", check.format );
		assertEquals( "5", check.min );
		assertEquals( "50", check.max );
		assertEquals( (Integer)0, check.minLength );
		assertEquals( (Integer)10, check.maxLength );
		assertEquals( (Integer)1, check.minNum );
		assertEquals( (Integer)20, check.maxNum );
		assertEquals( "xyz", check.pattern );
		
		assertEquals( "This field is required", check.requiredError);
		assertEquals( "MIN value", check.minError);			
		assertEquals( "MAX value", check.maxError);			
		assertEquals( "MINNUM value", check.minNumError);			
		assertEquals( "MINLEN value", check.minLengthError);			
		assertEquals( "MAXNUM value", check.maxNumError);			
		assertEquals( "MAXLEN value", check.maxLengthError);			
		assertEquals( "PATTERN value", check.patternError);			
		
		assertEquals( "/some/file", check.script.file );
		assertEquals( "doThis() doThat()", check.script.text );
		
	} 
	
	@Test 
	public void testCopy() {
		ValidationCheck check = new ValidationCheck();
		check.required = true;
		check.requiredError = "This field is required";
		
		check.min = "1";
		check.minError = "Min message";
		
		check.max = "99";
		check.maxError = "Max message";
		
		check.format = "TEXT";
		check.formatError = "String message";
		
		check.pattern = "xxx";
		check.patternError = "Pattern message";
		
		check.maxLength = 10;
		check.maxLengthError = "Length error message";
		
		check.maxNum = 11;
		check.maxNumError = "Num error message";
		
		check.script = new Script();
		check.script.file = "/script/file";
		
		ValidationCheck copy = Utils.copy(check);
		assertEquals(check, copy);
		
		assertEquals(check.required, copy.required);
		assertEquals(check.min, copy.min);
		assertEquals(check.max, copy.max);
		assertEquals(check.format, copy.format);
		assertEquals(check.pattern, copy.pattern);
		assertEquals(check.maxLength, copy.maxLength);
		assertEquals(check.maxNum, copy.maxNum);

		assertEquals(check.requiredError, copy.requiredError);
		assertEquals(check.minError, copy.minError);
		assertEquals(check.maxError, copy.maxError);
		assertEquals(check.formatError, copy.formatError);
		assertEquals(check.patternError, copy.patternError);
		assertEquals(check.maxLengthError, copy.maxLengthError);
		assertEquals(check.maxNumError, copy.maxNumError);	
		
		assertEquals( check.script.file, copy.script.file );
		assertEquals( check.script.text, copy.script.text );
	}
	
	@Test 
	public void testRequired() {
		ValidationCheck check = new ValidationCheck();
		check.format = "TEXT";
		check.required = true;
		check.requiredError = "Field %s is required";
		
		check.apply("field_name", "simple value");
		assertFalse( Validation.hasErrors() );
		
		check.apply("field_name", "");
		assertTrue( Validation.hasError("field_name") );
		assertEquals("Field field_name is required", Validation.error("field_name").message());
		
	}
	
	@Test 
	public void testMinSize() {
		ValidationCheck check = new ValidationCheck();
		check.format = "TEXT";
		check.min = "3";
		check.minError = "Field should be at least 3 chars length";
		
		check.apply("fieldMinSize", "abcd");
		assertFalse( Validation.hasError("fieldMinSize"));
		
		check.apply("fieldMinSize", " a ");
		assertTrue( Validation.hasError("fieldMinSize"));
		assertEquals( check.minError, Validation.error("fieldMinSize").message() );
		
	}
	

	@Test 
	public void testMaxSize() {
		ValidationCheck check = new ValidationCheck();
		check.format = "TEXT";
		check.max = "3";
		check.maxError = "Field should be at most 3 chars length";
		
		check.apply("fieldMaxSize", "ab");
		assertFalse( Validation.hasError("fieldMaxSize"));
		
		check.apply("fieldMaxSize", "abcd");
		assertTrue( Validation.hasError("fieldMaxSize"));
		assertEquals( check.maxError, Validation.error("fieldMaxSize").message() );
	}	
	
	
	
	@Test
	public void testPattern() {

		ValidationCheck check = new ValidationCheck();
		check.format = "TEXT";
		check.pattern = "a.*c";
		check.patternError = "Field should follow the pattern";
		
		check.apply("fieldPattern", "abc");
		assertFalse( Validation.hasError("fieldPattern"));
		
		check.apply("fieldPattern", "abcd");
		assertTrue( Validation.hasError("fieldPattern"));
		assertEquals( check.patternError, Validation.error("fieldPattern").message() );		
		
	}
	
	@Test
	public void testEmail() {
		ValidationCheck check = new ValidationCheck();
		check.format = "EMAIL";
		check.formatError = "Field is not a valid email address";
			
		check.apply("fieldEmail", "paolo@crg.es");
		assertFalse( Validation.hasError("fieldEmail") );

		
		check.apply("fieldEmail", "xxx");
		assertTrue( Validation.hasError("fieldEmail") );
		assertEquals( check.formatError, Validation.error("fieldEmail").message() );
	}
	
	@Test 
	public void testDateFormat() {
		ValidationCheck check = new ValidationCheck();
		check.format = "DATE";
		check.formatError = "Field is not a valid date format";
			
		check.apply("fieldDate", "13/2/2010");
		assertFalse( Validation.hasError("fieldDate") );
		
		check.apply("fieldDate", "xxx");
		assertTrue( Validation.hasError("fieldDate") );
		assertEquals( check.formatError, Validation.error("fieldDate").message() );	
	}
	
	@Test
	public void testDateMin() {
		ValidationCheck check = new ValidationCheck();
		check.format = "DATE";
		check.formatError = "Field is not a valid date format";
		
		check.min = "2/2/2010";
		check.minError = "Min accepted date is 2/2/2010";
		
		check.apply("fieldDateMin", "3/2/2010");
		assertFalse( Validation.hasError("fieldDateMin") );
		
		check.apply("fieldDateMin", "2/2/2010");
		assertFalse( Validation.hasError("fieldDateMin") );

		check.apply("fieldDateMin", "1/2/2010");
		assertTrue( Validation.hasError("fieldDateMin") );
		assertEquals( check.minError, Validation.error("fieldDateMin").message() );	
	}

	@Test
	public void testDateMax() {
		ValidationCheck check = new ValidationCheck();
		check.format = "DATE";
		check.formatError = "Field is not a valid date format";
		
		check.max = "2/2/2010";
		check.maxError = "MAX accepted date is 2/2/2010";
		
		check.apply("fieldDateMax", "1/2/2010");
		assertFalse( Validation.hasError("fieldDateMax") );
		
		check.apply("fieldDateMax", "2/2/2010");
		assertFalse( Validation.hasError("fieldDateMax") );

		check.apply("fieldDateMax", "3/2/2010");
		assertTrue( Validation.hasError("fieldDateMax") );
		assertEquals( check.maxError, Validation.error("fieldDateMax").message() );			
	}
	
	@Test
	public void testIntegerFormat() {
		ValidationCheck check = new ValidationCheck();
		check.format = "INTEGER";
		check.formatError = "Field is not a valid INTEGER";

		check.apply("fieldInteger", null);
		assertFalse( Validation.hasError("fieldInteger") );
		
		check.apply("fieldInteger", "999");
		assertFalse( Validation.hasError("fieldInteger") );
		
		check.apply("fieldInteger", "99.99");
		assertTrue( Validation.hasError("fieldInteger") );
		assertEquals( check.formatError, Validation.error("fieldInteger").message() );	
	}
	
	@Test
	public void testIntegerMin() {
		ValidationCheck check = new ValidationCheck();
		check.format = "INTEGER";
		
		check.min = "10";
		check.minError = "Min accepted value is 10";
		
		check.apply("fieldIntegerMin", null);
		assertFalse( Validation.hasError("fieldIntegerMin") );

		check.apply("fieldIntegerMin", "10");
		assertFalse( Validation.hasError("fieldIntegerMin") );
		
		check.apply("fieldIntegerMin", "9");
		assertTrue( Validation.hasError("fieldIntegerMin") );
		assertEquals( check.minError, Validation.error("fieldIntegerMin").message() );
	} 

	@Test
	public void testIntegerMax() {
		ValidationCheck check = new ValidationCheck();
		check.format = "INTEGER";
		
		check.max = "10";
		check.maxError = "MAX accepted value is 10";
		
		check.apply("fieldIntegerMax", null);
		assertFalse( Validation.hasError("fieldIntegerMax") );

		check.apply("fieldIntegerMax", "10");
		assertFalse( Validation.hasError("fieldIntegerMax") );
		
		check.apply("fieldIntegerMax", "11");
		assertTrue( Validation.hasError("fieldIntegerMax") );
		assertEquals( check.maxError, Validation.error("fieldIntegerMax").message() );		
	}
	
	@Test
	public void testDecimalFormat() {
		ValidationCheck check = new ValidationCheck();
		check.format = "DECIMAL";
		check.formatError = "Field is not a valid DECIMAL";
			
		check.apply("fieldDec", null);
		assertFalse( Validation.hasError("fieldDec") );

		check.apply("fieldDec", "99.9");
		assertFalse( Validation.hasError("fieldDec") );
		
		check.apply("fieldDec", "99X.99");
		assertTrue( Validation.hasError("fieldDec") );
		assertEquals( check.formatError, Validation.error("fieldDec").message() );			
	}
	
	@Test
	public void testDecimalMin() {
		ValidationCheck check = new ValidationCheck();
		check.format = "DECIMAL";
		
		check.min = "9.99";
		check.minError = "Min accepted value is 9.99";
		
		check.apply("fieldDecMin", null);
		assertFalse( Validation.hasError("fieldDecMin") );

		check.apply("fieldDecMin", "9.99");
		assertFalse( Validation.hasError("fieldDecMin") );
		
		check.apply("fieldDecMin", "9.98");
		assertTrue( Validation.hasError("fieldDecMin") );
		assertEquals( check.minError, Validation.error("fieldDecMin").message() );		
	}

	@Test
	public void testDecimalMax() {
		ValidationCheck check = new ValidationCheck();
		check.format = "DECIMAL";
		
		check.max = "10";
		check.maxError = "Max accepted value is 10";

		check.apply("fieldDecMax", null);
		assertFalse( Validation.hasError("fieldDecMax") );

		check.apply("fieldDecMax", "10");
		assertFalse( Validation.hasError("fieldDecMax") );
		
		check.apply("fieldDecMax", "10.1");
		assertTrue( Validation.hasError("fieldDecMax") );
		assertEquals( check.maxError, Validation.error("fieldDecMax").message() );			
	}
	
	@Test 
	public void testFastaFormat() {
		final String SAMPLE = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			"NYITPVN\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n" +
			"VPRNLLGLYP";
		
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA";
		check.formatError = "Invalid FASTA format";
		
		
		check.apply("fieldFasta", null);
		assertFalse( Validation.hasError("fieldFasta") );		
		
		check.apply("fieldFasta", SAMPLE);
		assertFalse( Validation.hasError("fieldFasta") );		

		check.apply("fieldFasta", "xxx");
		assertTrue( Validation.hasError("fieldFasta") );		
		assertEquals( "Invalid FASTA format. Unrecognized character 'x' (0x78) in FASTA sequences starting line: 1.", Validation.error("fieldFasta").message() );				
	}

	@Test 
	public void testFastaNornmalization() {
		final String SAMPLE = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGE KLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			"NYITPVN\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEG DCMTIIHREDEDEIEWWWARLNDKEGY\n" +
			"VPRNLLGLYP";
		
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA";
		check.formatError = "Invalid FASTA format";
		
		final String NORMALIZED = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			"NYITPVN\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n" +
			"VPRNLLGLYP\n";
		
		check.apply("fieldFastaNormalization", SAMPLE);
		assertFalse( Validation.hasError("fieldFastaNormalization") );		

		assertEquals( NORMALIZED, check.getNormalizedValue() );
	}
	
	
	@Test
	public void testFastaMaxNum() {

		final String SAMPLE = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			"NYITPVN\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n" +
			"VPRNLLGLYP\n";
		
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA";

		check.maxNum = 3;
		check.maxNumError = "Max sequence num error";
		
		
		check.apply("fieldFastaMaxNum", SAMPLE);
		assertFalse( Validation.hasError("fieldFastaMaxNum") );		

		check.apply("fieldFastaMaxNum", SAMPLE+SAMPLE);
		assertTrue( Validation.hasError("fieldFastaMaxNum") );		
		assertEquals( check.maxNumError, Validation.error("fieldFastaMaxNum").message() );			
	}
	
	@Test
	public void testFastaMinNum() {

		final String SAMPLE = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			"NYITPVN\n";
		
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA";

		check.minNum = 2;
		check.minNumError = "Max sequence num error";
		
		
		check.apply("fieldFastaMinNum", SAMPLE+SAMPLE);
		assertFalse( Validation.hasError("fieldFastaMinNum") );		

		check.apply("fieldFastaMinNum", SAMPLE);
		assertTrue( Validation.hasError("fieldFastaMinNum") );		
		assertEquals( check.minNumError, Validation.error("fieldFastaMinNum").message() );			
	}	
	
	@Test
	public void testFastaMinLen() {
		
		final String SAMPLE_BAD = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVP\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n";

		final String SAMPLE_GOOD = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n" +
			"NLFVALYDFVAS\n";		
		
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA";
		check.formatError = "Invalid FASTA format";

		check.minLength = 50;
		check.minLengthError = "Min sequence lengtherror";
		
		
		check.apply("fieldFastaMinLength", SAMPLE_GOOD);
		assertFalse( Validation.hasError("fieldFastaMinLength") );		

		check.apply("fieldFastaMinLength", SAMPLE_BAD);
		assertTrue( Validation.hasError("fieldFastaMinLength") );		
		assertEquals( check.minLengthError, Validation.error("fieldFastaMinLength").message() );			
	}	
	
	@Test
	public void testFastaMaxLen() {
		
		final String SAMPLE_GOOD = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n";

		final String SAMPLE_BAD = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n" +
			"NLFVALYDFVAS\n";		
		
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA";
		check.formatError = "Invalid FASTA format";

		check.maxLength = 50;
		check.maxLengthError = "Max sequence lengtherror";
		
		
		check.apply("fieldFastaMaxLength", SAMPLE_GOOD);
		assertFalse( Validation.hasError("fieldFastaMaxLength") );		

		check.apply("fieldFastaMaxLength", SAMPLE_BAD);
		assertTrue( Validation.hasError("fieldFastaMaxLength") );		
		assertEquals( check.maxLengthError, Validation.error("fieldFastaMaxLength").message() );			
	}
	
	
	@Test
	public void testFastaSameLen() {
		
		final String SAMPLE_GOOD = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEK---LGYNHNGEWCEAQTKNGQGWVPS\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n";

		final String SAMPLE_BAD = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n" +
			"NLFVALYDFVAS\n";		
		
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA";
		check.formatError = "Invalid FASTA format";

		check.sameLength = true;
		check.sameLengthError = "Not the same length";
		
		
		check.apply("fieldSameLength", SAMPLE_GOOD);
		assertFalse( Validation.hasError("fieldSameLength") );		

		check.apply("fieldSameLength", SAMPLE_BAD);
		assertTrue( Validation.hasError("fieldSameLength") );		
		assertEquals( check.sameLengthError, Validation.error("fieldSameLength").message() );			
	}
	
	
	@Test 
	public void testClustalOK() { 
		String GOOD = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP";		
		
		ValidationCheck check = new ValidationCheck();
		check.format = "CLUSTAL";
		check.formatError = "Invalid CLUSTAL format";

		check.apply("fieldClustal", GOOD);
		assertFalse( Validation.hasError("fieldClustal") );		
	}
	
	
	
	
	@Test 
	public void testClustalFail() { 
		String BAD = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"NGQGWVPSNYITPVN------ 20\n" +
			"DKEGYVPRNLLGLYP------ 30\n" +
			"GERGDFPGTYVEYIGRKKISP 20";		
		
		ValidationCheck check = new ValidationCheck();
		check.format = "CLUSTAL";
		check.formatError = "Invalid CLUSTAL format";

		check.apply("fieldClustalFail", BAD);
		assertTrue( Validation.hasError("fieldClustalFail") );		
		assertEquals( "Invalid CLUSTAL format. Invalid Clustal format around line: 3.", Validation.error("fieldClustalFail").message() );			
	}	
	
	@Test 
	public void testClustalNormalization() { 
		String GOOD = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------ 20\n" +
			"1ycsB  DKEGYVPRNLLGLYP------ 30\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP 20\n" +
			"         . : ..  * * . .         ";		
		
		ValidationCheck check = new ValidationCheck();
		check.format = "CLUSTAL";
		check.formatError = "Invalid CLUSTAL format";

		check.apply("fieldClustalNormalized", GOOD);
		assertFalse( Validation.hasError("fieldClustalNormalized") );		

		String NORMALIZED = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP\n";
		
		assertEquals( NORMALIZED, check.getNormalizedValue() );
	}	
	
	@Test
	public void testClustalSameLen() {
		
		String SAMPLE_GOOD = 
				"CLUSTAL W (1.82) multiple sequence alignment\n" +
				"\n" +
				"1aboA  NGQGWVPSNYITPVN--NPTN\n" +
				"1ycsB  DKEGYVPRNLLGLYP---PTN\n" +
				"1pht   GERGDFPGTYVEYIGRKKISP";		

		String SAMPLE_BAD = 
				"CLUSTAL W (1.82) multiple sequence alignment\n" +
				"\n" +
				"1aboA  NGQGWVPSNYITPVN------\n" +
				"1ycsB  DKEGYVPRNLLGLYP\n" +
				"1pht   GERGDFPGTYVEYIGRKKISP";		
		
		ValidationCheck check = new ValidationCheck();
		check.format = "CLUSTAL";
		check.formatError = "Invalid CLUSTAL format";

		check.sameLength = true;
		check.sameLengthError = "Not the same length";
		
		
		check.apply("clustalSameLength", SAMPLE_GOOD);
		assertFalse( Validation.hasError("clustalSameLength") );		

		check.apply("clustalSameLength", SAMPLE_BAD);
		assertTrue( Validation.hasError("clustalSameLength") );		
		assertEquals( check.sameLengthError, Validation.error("clustalSameLength").message() );			
	}
	
	
	@Test 
	public void testFastaOrCLustal() { 
	
		/* 
		 * first try with FASTA 
		 */
		final String FASTA = 
			">1aboA \n" +
			"NLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPS\n" +
			">1ycsB\n" +
			"KGVIYALWDYEPQNDDELPMKEGDCMTIIHREDEDEIEWWWARLNDKEGY\n";
		
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA | CLUSTAL";  // <-- this means that the entry can be FASTA  - or -  CLUSTAL  

		check.apply("field", FASTA);
		assertFalse( Validation.hasError("field") );	
		
		/*
		 * second try with clustal 
		 */
		String CLUSTAL = 
			"CLUSTAL W (1.82) multiple sequence alignment\n" +
			"\n" +
			"1aboA  NGQGWVPSNYITPVN------\n" +
			"1ycsB  DKEGYVPRNLLGLYP------\n" +
			"1pht   GERGDFPGTYVEYIGRKKISP";		

		check.apply("field", CLUSTAL);
		assertFalse( Validation.hasError("field") );	
		
		/*
		 * Third some shit 
		 */
		check.formatError = "Sequence have to be in FASTA or CLUSTAL format";
		check.apply("field", "xxx");
		assertTrue( Validation.hasError("field") );	
		assertEquals( "Sequence have to be in FASTA or CLUSTAL format. Missing Clustal header declaration.", Validation.error("field").message() );			

		
	}

	@Test 
	public void testFastaValidationType() { 
		/* 
		 * this must pass 
		 */
		ValidationCheck check = new ValidationCheck();
		check.format = "FASTA";
		check.type = "rna";
		check.apply("fieldDna1", IO.readContentAsString(TestHelper.file("/sample-rna.fa")));
		assertFalse(Validation.hasError("fieldDna1"));

		/*
		 * this must FAIL 
		 */
		check = new ValidationCheck();
		check.format = "FASTA";
		check.type = "rna";
		check.apply("fieldAminoAcid1", IO.readContentAsString(TestHelper.file("/sample-proteins.fa")));
		assertTrue(Validation.hasError("fieldAminoAcid1"));

	}
	
	@Test
	public void testValidationScript() {
		TestHelper.init("fieldScript1","fieldScript2");
		
		String rule 
			= "assert field.name == 'fieldScript1'; " +
			  "if( value < 100 ) " +
			  "  \"Field cannot be less than 100'. You entered $value\""; 
		

		/* this validation MUST do not pass */
		Validation.clear();
		ValidationCheck check = new ValidationCheck();
		check.format = "integer";
		check.script = new Script();
		check.script.text =  rule;
		check.apply("fieldScript1", "9"); // <-- validate the value '9'
		assertTrue(Validation.hasError("fieldScript1"));
		assertEquals("Field cannot be less than 100'. You entered 9", Validation.error("fieldScript1").message());

		/*
		 * this should pass
		 */
		Validation.clear();
		check = new ValidationCheck();
		check.format = "integer";
		check.script = new Script();
		check.script.text =  rule;
		check.apply("fieldScript1", "101"); // <-- validate the value '101'
		assertFalse(Validation.hasError("fieldScript1"));
	
	}
	
	
	@Test
	public void testValidationScript2() {
		TestHelper.init("fieldScript1","fieldScript2");
		
		String rule = "if( value.length() < 3 ) { return 'String too short' }; value = value.reverse(); return"; 
		

		/* this validation MUST pass */
		Validation.clear();
		ValidationCheck check = new ValidationCheck();
		check.script = new Script();
		check.script.text =  rule;
		check.apply("fieldScript1", "Hi there!"); 
		assertFalse(Validation.hasError("fieldScript1"));
		assertEquals( check.fNormalizedValue, "!ereht iH" );

		/*
		 * this should pass
		 */
		Validation.clear();
		check = new ValidationCheck();
		check.script = new Script();
		check.script.text =  rule;
		check.apply("fieldScript2", "AB"); 
		assertTrue(Validation.hasError("fieldScript2"));
		assertNull( check.fNormalizedValue);
		assertEquals( "String too short", Validation.error("fieldScript2").message());
	} 
	
}

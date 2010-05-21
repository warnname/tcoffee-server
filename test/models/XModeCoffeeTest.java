package models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;

import play.test.FunctionalTest;
import util.TcoffeeHelperTest;
import util.TestHelper;
import util.Utils;
import exception.CommandException;

public abstract class XModeCoffeeTest extends FunctionalTest{

	static CmdArgs DEFAULT_ARGS; 
	
	static class TestRunner {
		
		final String rid;
		
		List<File> input = new ArrayList<File>();
		
		CmdArgs args = new CmdArgs();
		
		public TestRunner(String rid) { this.rid = rid; }
		
		public TestRunner input(File ... files) {
			for( File file : files ){
				input.add(file);
			}
			return this;
		} 
		
		public TestRunner args(String... args) {
			for( String pair : args) {
				this.args.put(pair);
			}
			
			return this;
		}
		
		public TestRunner args( CmdArgs args ) {
			this.args.putAll(args);
			return this;
		}
		
		
		public void go() {
			try {
				final String RID = String.format("test-%s", rid);  
				Repo ctx = new Repo(RID);
				Utils.deleteFolder(ctx.getFile());
				
				/* initialize the current module */
				Module module = Module.current(new Module());
				module.fRid = RID;
				module.fRepo = new Repo(RID,true);
				/* copy the source file */
				for( File file : input ) {
					TestHelper.copy(file, new File(module.folder(), file.getName()));		
					
				}
				
				TCoffeeCommand tcoffee = new TCoffeeCommand();
				tcoffee.args = args;

				tcoffee.errfile = "err.log";
				tcoffee.logfile = "out.log";
				tcoffee.cmdfile = "cmd.log";
				tcoffee.envfile = "env.log";
				
				tcoffee.init();
				boolean ok = tcoffee.execute();
				assertTrue( ok );
				assertTrue( tcoffee.getLogFile().exists() );
				assertTrue( tcoffee.getErrFile().exists() );
				assertTrue( tcoffee.getEnvFile().exists() );
				assertTrue( tcoffee.getCmdFile().exists() );
				
				OutResult result = tcoffee.getResult();
				assertNotNull( result.getAlignmentHtml() );
				
				/* assert that the html is the expected form */
				TcoffeeHelperTest.parseHtmlFile(result.getAlignmentHtml().file);			
			}
			catch(Exception e ) {
				e.printStackTrace();
				fail(e.getMessage());
			}			
		}
		
	}
	
	
	@BeforeClass 
	public static void staticinit() {
		DEFAULT_ARGS = new CmdArgs();
		DEFAULT_ARGS.put("in", "sample.fasta");
	    DEFAULT_ARGS.put("output", "score_html clustalw_aln fasta_aln phylip");
	    DEFAULT_ARGS.put("maxnseq", "50");
	    DEFAULT_ARGS.put("maxlen", "2000");
		DEFAULT_ARGS.put("case", "upper") ;
		DEFAULT_ARGS.put("seqnos", "on") ;
		DEFAULT_ARGS.put("outorder", "input");
		DEFAULT_ARGS.put("outfile", "tcoffee") ;
		DEFAULT_ARGS.put("cache", "no" );
		DEFAULT_ARGS.put("remove_template_file", "1");
		
	}

	
	void testWithMethod(String mode, String  method) throws CommandException {
		String rid = mode+"-"+method;
		mode = "mode=" + mode;
		method = "method=" + method;
		testWithDefaults( TestHelper.sampleFasta(), rid, mode, method );
	}
	
	void testMode( String rid, File input, CmdArgs args ) throws CommandException {
	
		new TestRunner(rid).input(input).args(args).go();
	}
	
	void testMode( String rid, File input, String... args ) throws CommandException {
		CmdArgs obj = new CmdArgs();
		obj.putAll(args);
		testMode(rid,input,obj);
	}
	
	void testWithDefaults( final File input, final String rid, final String... args ) throws CommandException {

		CmdArgs copy = Utils.copy(DEFAULT_ARGS);
		copy.putAll(args);
		testMode(rid, input, copy);
		
	}

	
}

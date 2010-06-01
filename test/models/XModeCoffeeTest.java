package models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import play.test.FunctionalTest;
import util.TcoffeeHelperTest;
import util.TestHelper;
import util.Utils;

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
				//tcoffee.args.put("multi_core=no");

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
	

	


	
}

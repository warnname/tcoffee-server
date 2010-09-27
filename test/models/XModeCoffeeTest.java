package models;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import play.Play;
import play.test.FunctionalTest;
import util.TcoffeeHelperTest;
import util.TestHelper;
import util.Utils;

public abstract class XModeCoffeeTest extends FunctionalTest{

	static CmdArgs DEFAULT_ARGS; 
	
	static class TCoffeeTestRunner {
		
		final String rid;
		
		List<File> input = new ArrayList<File>();
		
		CmdArgs args = new CmdArgs();
		
		public TCoffeeTestRunner(String rid) { this.rid = rid; }
		
		public TCoffeeTestRunner input(File ... files) {
			for( File file : files ){
				input.add(file);
			}
			return this;
		} 
		
		public TCoffeeTestRunner args(String... args) {
			for( String pair : args) {
				this.args.put(pair);
			}
			
			return this;
		}
		
		public TCoffeeTestRunner args( CmdArgs args ) {
			this.args.putAll(args);
			return this;
		}
		
		
		public void go() {
			try {
				final String RID = String.format("test-%s", rid);  
				Repo ctx = new Repo(RID);
				Utils.deleteFolder(ctx.getFile());
				
				Bundle bundle = Bundle.read(new File(Play.applicationPath,"bundles/tcoffee")) ;
				
				/* initialize the current service */
				Service service = Service.current(new Service());
				service.bundle = bundle;
				service.fRid = RID;
				service.fRepo = new Repo(RID,true);
				/* copy the source file */
				for( File file : input ) {
					TestHelper.copy(file, new File(service.repo().getFile(), file.getName()));		
					
				}
				
				service.fCtx = new HashMap<String, Object>();
				/* add the bundle properties content */
				for( Object key : bundle.properties.keySet() ) {
					service.fCtx.put( key.toString(), bundle.properties.getProperty(key.toString()));
				}
				service.fCtx.put("data.path", service.repo().getFile().toString());
				
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

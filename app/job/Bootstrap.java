package job;

import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Router;
import play.vfs.VirtualFile;

@OnApplicationStart
public class Bootstrap extends Job {

	
	
	@Override
	public void doJob() {

		addTestsRoute();
		
	}
	
	private void addTestsRoute() {
		if( "test".equals( Play.id ) ) {
	        Router.addRoute("GET", "/@tests/all", "SlimTestRunner.index");		
	        Play.templatesPath.add( VirtualFile.open(Play.applicationPath).child("test/views") );
		}
		
	}



}

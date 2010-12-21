package job;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Router;
import play.vfs.VirtualFile;

/**
 * This job will be executed once on application start-up
 * 
 * @author paolo di tommaso
 *
 */
@OnApplicationStart
public class Bootstrap extends Job {

	
	
	@Override
	public void doJob() {
		/* add route for testing */
		addTestsRoute();
		/* add current timestamp */
		Cache.set("server-start-time", System.currentTimeMillis());
		
	}
	
	private void addTestsRoute() {
		if( "test".equals( Play.id ) ) {
	        Router.addRoute("GET", "/@tests/all", "SlimTestRunner.index");		
	        Play.templatesPath.add( VirtualFile.open(Play.applicationPath).child("test/views") );
		}
		
	}



}

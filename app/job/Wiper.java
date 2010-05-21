package job;

import models.Repo;
import play.jobs.Every;
import play.jobs.Job;

/** 
 * Remove all expired T-Coffee requests from the file system 
 * 
 * @author Paolo Di Tommaso
 *
 */
@Every("1h")
public class Wiper extends Job {

	
	@Override
	public void doJob() { 
		Repo.deleteExpired();
    }
	
	
}

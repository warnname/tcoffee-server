package job;

import java.io.File;
import java.util.Iterator;

import models.AppProps;
import models.Repo;

import org.apache.commons.io.FileUtils;

import play.Logger;
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
		Logger.debug("Running Wiper job");
		
		/* 
		 * delete expired jobs 
		 */
		Repo.deleteExpired();
		
		/* 
		 * delete temporary files and upload
		 */
		Iterator files = FileUtils.iterateFiles(AppProps.TEMP_PATH, null, false);
		while( files.hasNext() ) { 
			File file = (File) files.next();
			long max = 60 * 60 * 1000; // <-- 1 h
			long delta = System.currentTimeMillis() - file.lastModified();
			if( delta>max && !FileUtils.deleteQuietly(file) ) { 
				Logger.warn("Wiper cannot delete temp file: '%s'", file);
			}
		}
		
		
    }
	
	
}

package job;

import java.io.File;

import models.AppProps;
import models.Repo;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.mvc.Util;

/** 
 * Remove all expired T-Coffee requests from the file system 
 * 
 * @author Paolo Di Tommaso
 *
 */
@Every("cron.wiper.interval")
public class Wiper extends Job {

	
	@Override
	public void doJob() { 
		Logger.debug("Running Wiper job");
		
		
		/*
		 *  delete T-Coffee cache
		 */
		Repo.cleanTcoffeeCache();
		
		/* 
		 * delete temporary files and upload
		 */
		long max = 1000 * AppProps.instance().getDuration("settings.wiper.temp.duration", 60 * 60 );
		empty( AppProps.TEMP_PATH, max );
		
    }
	
	@Util
	static void empty( File path, long max ) {
		File[] files = path.listFiles();
		for( File it : files ) {
			if( !it.isDirectory() ) {
				continue;
			}

			/*
			 * traverse recursively
			 */
			empty( it, max );

			/*
			 * now try to delete 
			 * NOTE The mtime (modification time) on the directory itself changes when a file is added, removed or renamed
			 * so if nothing has been added something to the current folder, it can be deleted safely 
			 * 
			 * See http://stackoverflow.com/questions/3620684/directory-last-modified-date
			 */
			long delta = System.currentTimeMillis() - it.lastModified();
			Logger.debug("Max: %s - delta: %s", max, delta);
			
			if( delta>max ) { 
				if( !FileUtils.deleteQuietly(it) ) {
					Logger.warn("Wiper cannot delete temp file: '%s'", it);
				}
				else { 
					Logger.info("Eviteced temporary path: '%s'", it);
				}
			}

		} 
	} 
	
	
}

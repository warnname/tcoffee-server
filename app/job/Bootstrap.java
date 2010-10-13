package job;

import java.io.File;
import java.io.IOException;

import models.AppProps;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Router;
import play.vfs.VirtualFile;
import exception.QuickException;

@OnApplicationStart
public class Bootstrap extends Job {

	
	
	@Override
	public void doJob() {

		addTestsRoute();
		
		deployPropFile();
		
	}
	
	private void addTestsRoute() {
		if( "test".equals( Play.id ) ) {
	        Router.addRoute("GET", "/@tests/all", "SlimTestRunner.index");		
	        Play.templatesPath.add( VirtualFile.open(Play.applicationPath).child("test/views") );
		}
		
	}


	private void deployPropFile() {
		File source = Play.getFile("conf/tserver.properties.xml");
		File target = AppProps.SERVER_PROPS_FILE;
		
		if( source.equals(target) ) {
			return;
		}
		
		/*
		 * check if properties file exist in data folder otherwise copy them 
		 */
		boolean force = "true".equals(Play.configuration.getProperty("deploy.props.file"));

		if( !target.exists() || force ) {

			Logger.info("Copy default t-server properties file to: %s", target);
			try {
				FileUtils.copyFile(source, target);
			} catch (IOException e) {
				throw new QuickException(e, "Unable to copying tserver properties file");
			}
		}
	}
	


}

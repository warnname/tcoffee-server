package job;

import java.io.File;
import java.io.IOException;

import models.AppProps;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import util.Utils;
import exception.QuickException;

@OnApplicationStart
public class Bootstrap extends Job {

	
	
	@Override
	public void doJob() {

		detectContextPath();
		
		deployBinFolder();
		deployMatrixFolder();
		
		deployPropFile();
		deplotConfFile();
		
		addDefProperties();
		
	}
	
	private void deployMatrixFolder() {

		File source = AppProps.DEFAULT_MATRIX_FOLDER;
		File target = AppProps.instance().getMatrixFolder();
		
		if( source.equals(target) ) {
			/* if the path are the same nothing to do */
			return;
		}
		
		boolean force = "true".equals(Play.configuration.get("deploy.matrix.folder"));
		if( !target.exists() || force ) {
			
			try {
				FileUtils.copyDirectory(source, target);
			} catch (IOException e) {
				Logger.error(e, "Unable to deploy matrix folder to: %s", target);
			}
			
		}		

	}

	private void deployBinFolder() {
		File source = AppProps.DEFAULT_BIN_FOLDER;
		File target = AppProps.instance().getBinFolder(); 
		
		if( source.equals(target) ) {
			/* if the path are the same nothing to do */
			return;
		}
		
		boolean force = "true".equals(Play.configuration.get("deploy.bin.folder"));
		if( !target.exists() || force ) {
			
			try {
				FileUtils.copyDirectory(source, target);
			} catch (IOException e) {
				Logger.error(e, "Unable to deploy bin folder to: %s", target);
			}
			
		}
		
	}

	private void addDefProperties() {
		/*
		 * add other conf property 
		 */
		AppProps props = AppProps.instance();
		
		boolean atLeastOne = false;
		for( Object obj : Play.configuration.keySet() ) {
			String key = (String)obj;
			
			if( key != null ) {
				if( key.startsWith("mail.smtp.") || key.startsWith("tserver.")) {
					/*
					 * add all "mail.smtp.xxx" and "tserver.xxx" properties  
					 */
					if( addPropertyIfNotAlreadyExists(props, key) ) {
						atLeastOne = true;
					};
				}  
			}
		}
		
		/* save it */
		if( atLeastOne ) {
			props.save();
		}

	}

	private void deplotConfFile() {
		File source = Play.getFile("conf/tserver.conf.xml");
		File target = AppProps.SERVER_CONF_FILE;		
		
		/*
		 * check if conf file exist in data folder otherwise copy them 
		 */
		boolean force = "true".equals(Play.configuration.getProperty("deploy.conf.file"));
		
		if( !target.exists() || force ) {

			Logger.info("Copy default t-server conf file to: %s", target);
			try {
				FileUtils.copyFile(source, target);
			} catch (IOException e) {
				throw new QuickException(e, "Unable to copying tserver properties file");
			}
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
	

	private void detectContextPath() {
		/*
		 * detect contex path in quick&dirty way
		 */
//		String context = "";
//		String home = Router.reverse("Application.index").toString();
//		if( !"/".equals(home) ) {
//			context = home.endsWith("/") ? home.substring(0,home.length()-1) : home;
//			Play.configuration.setProperty("context", context);
//			Logger.info("Detected application Context Path: ''", context);
//		}
//		else {
//			Logger.info("Using ROOT Context path");
//		}

		/*
		 * TODO make this dynamic with dependency with an external property
		 */
		String context = Play.configuration.getProperty("context");
		if( Utils.isEmpty(context) ) {
			Logger.info("Using ROOT Context path");
		}
		else {
			if( !context.startsWith("/") ) {
				context = "/" + context;
				Play.configuration.setProperty("context",context);
			}
			Logger.info("Detected application Context Path: '%s'", context);
		}
		
	}

	private boolean addPropertyIfNotAlreadyExists( final AppProps props, final String key ) {
		String value = Play.configuration.getProperty(key);
		
		String name = key;
		// remove "tserver." prefix
		if( name.startsWith("tserver.")) {
			name = name.substring("tserver.".length());
		}
		
		name = Utils.camelize(name,".:"); // <-- garantee that this property name does not contain special chars
		// add to properties if not already exists and has a valid value 
		if( Utils.isNotEmpty(value) && !props.containsKey(name) ) {
			props.add(name, value);
			return true;
		}
		
		return false;
	}
	
}

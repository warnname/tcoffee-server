package models;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.Play;
import util.Check;
import util.ReloadableSingletonFile;
import util.Utils;
import util.XStreamHelper;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import events.AppEvents;


/**
 * Application properties accessible throught the user interface 
 * 
 * @author Paolo Di Tommaso
 *
 */
@XStreamAlias("props")
public class AppProps  {

	static File getConfFile(String confProp, String defName) {
		File result;
		String propsFileName = Play.configuration.getProperty(confProp,defName);
		
		if( propsFileName.startsWith( File.separator ) ) {
			/* when an absolute path is specified just use it */
			result = new File(propsFileName);
		}
		else {
			/* try to find it on the data folder */
			result = new File(DATA_FOLDER,propsFileName);
			
			/* if does not exist fallback on the application conf path */
			if( !result .exists() ) {
				File conf = new File(Play.applicationPath,"conf");
				result = new File(conf,propsFileName);
			}
		}

		if( result.exists() ) {
			Logger.info("Detected %s file: %s", confProp, result);
		}
		else {
			Logger.warn("Missing %s file: %s", confProp, result);
		}
		
		return result;
		
	}
	
	
	/**
	 * The application main <i>data</i> folder. It contains application 
	 * properties <code>tserver.properties.xml></code>, tcoffee configuration file
	 * <code>tserver.conf.xml</code> and application temporary data
	 */
	static final File DATA_FOLDER;

	
	static final Map<String,String> DEF_PROPS;

	/** Reference the server properties file <code>tserver.properties.xml</code> */
	public static final File SERVER_PROPS_FILE;

	/** Reference the server properties file <code>tserver.conf.xml</code> */
	public static final File SERVER_CONF_FILE;

	/** Alignment requests log file */
	public static final File SERVER_LOG_FILE; 
	
	/*
	 * the folder containing all the executable binaries
	 */
	public static final File DEFAULT_BIN_FOLDER;
	
	public static final File DEFAULT_MATRIX_FOLDER;
	
	static final File TCOFFEE_HOME_FOLDER;
	
	/* singleton instance */
	final private static ReloadableSingletonFile<AppProps> INSTANCE;
	
	static {

    	/*
    	 * 1. local data workspace 
    	 */
		String path = Play.configuration.getProperty("tserver.pathData");   
		DATA_FOLDER = Utils.isNotEmpty(path) 
				    ? new File(path)
		 			: new File(Play.applicationPath,"data");
	
		if( !DATA_FOLDER.exists() ) {
			Logger.warn("Application DATA root does not exists: '%s'", DATA_FOLDER);
		}
		
		/*
		 * 2. define the properties file 
		 */
		SERVER_PROPS_FILE = getConfFile("application.props.file","tserver.properties.xml");

		/*
		 * 3. define the conf file 
		 */
		SERVER_CONF_FILE = getConfFile("application.conf.file", "tserver.conf.xml");

		/*
		 * 4. log file name
		 */
		SERVER_LOG_FILE = new File(DATA_FOLDER,"tserver.log");
		
		
		/*
		 * 5. Define the other default folder that can be overriden at runtime
		 */
		
		/* bin apps folder */ 
		final String BIN_PATH = "bin";
		DEFAULT_BIN_FOLDER = new File(Play.applicationPath, BIN_PATH);
		if( !DEFAULT_BIN_FOLDER.exists() ) {
			Logger.warn("Unable to find 'tcoffee' executable on path: '%s'", Utils.getCanonicalPath(DEFAULT_BIN_FOLDER));
		}
		
		/* mcoffee matrix */
		DEFAULT_MATRIX_FOLDER = new File(Play.applicationPath, "matrix");
		if( !DEFAULT_MATRIX_FOLDER.exists() ) {
			Logger.warn("Unable to find 'matrix' folder at path: '%s'", Utils.getCanonicalPath(DEFAULT_MATRIX_FOLDER));
		}
		
		
		/* t-coffee home */
    	TCOFFEE_HOME_FOLDER = new File(DATA_FOLDER, ".t_coffee");
				
		
		DEF_PROPS = new HashMap<String,String>();
		DEF_PROPS.put("pathBin",  Utils.getCanonicalPath(DEFAULT_BIN_FOLDER));
		DEF_PROPS.put("pathTcoffee", Utils.getCanonicalPath(TCOFFEE_HOME_FOLDER));
		DEF_PROPS.put("pathMatrix", Utils.getCanonicalPath(DEFAULT_MATRIX_FOLDER));
		DEF_PROPS.put("requestTimeToLive", "172800"); // = 2 * 24 * 60 * 60 -  The max age (in secs) for which the request is stored in the file system

		/*
		 * 5. create the AppProps singleton
		 */
		INSTANCE = new ReloadableSingletonFile<AppProps>(SERVER_PROPS_FILE) {
			@Override
			public void onReload(File file, AppProps props) {
				super.onReload(file, props);
				AppEvents.appPropsChanged(props);
			}
		};
		
	}

	@XStreamImplicit(itemFieldName="property")
	List<Property> properties;

	/** lazy loadable accessor method */ 
	protected List<Property> properties() {
		if( properties == null ) {
			properties = new ArrayList<Property>();
		}
		return properties;
	}
	
	/** The default constructor */
	public AppProps() {
	}
	
	/** The copy constructor */
	public AppProps( AppProps that ) {
		this.properties = Utils.copy(that.properties);
	}
	
	/** Factory method to create an istance from its xml representation */
	static AppProps create(File file) {
		return XStreamHelper.fromXML(SERVER_PROPS_FILE);
	}
	
	/** Singleton instance accessor */
	public static AppProps instance() {
		return INSTANCE.get();
	} 
	
	/**
	 * Save this properties in the server properties <code>tserver.properties.xml</code>
	 * 
	 * @see {@link #SERVER_PROPS_FILE} 
	 */
	public void save() {
		XStreamHelper.toXML(this, SERVER_PROPS_FILE);
	}
	

	public String getWebmasterEmail() {
		return getString("webmasterEmail");
	}
	
	/**
	 * The application data path. Statically defined cannot be overriden. 
	 */
	public String getDataPath() {
		return Utils.getCanonicalPath(DATA_FOLDER);
	}

	public File getDataFolder() {
		return DATA_FOLDER;
	}
	
	/*
	 * the path 
	 */
	public String getBinPath() {
		return getString("pathBin");
	}
	
	public File getBinFolder() {
		return new File(getBinPath());
	}
	
	public String getTCoffeePath() {
		return getString("pathTcoffee");
	}
	
	public File getTCoffeeFolder() {
		return new File(getTCoffeePath());
	}
	
	public String getMatrixPath() {
		return getString("pathMatrix");
	}
	
	public File getMatrixFolder() {
		return new File(getMatrixPath());
	} 
	
	public int getRequestTimeToLive() {
		return getInteger("requestTimeToLive");
	}
	
	public String getString(final String key) {
		return get(key, DEF_PROPS.get(key));
	}
	
	public int getInteger(final String key) {
		return Integer.parseInt(get(key, DEF_PROPS.get(key)));
	}
	
	public String get(String key, final String defValue) {
		Check.notNull(key, "Argument 'key' cannot be null");
		if( properties == null ) return defValue;
		
		for( Property prop : properties ) {
			if( key.equals(prop.getName())) {
				return prop.getValue();
			}
		}
		
		return defValue;
	}

	public List<String> getNames() {
		if( properties == null ) return null;

		List<String> result = new ArrayList<String>();
		for( Property prop : properties ) {
			result.add(prop.getName());
		}
		return result;
	}
	
	public List<Property> list() {
		return properties != null ? new ArrayList<Property>(properties) : Collections.<Property>emptyList();
	}
	
	public int indexOf( String key ) {
		if( properties == null ) return -1;

		int i=0;
		for( Property prop : properties ) {
			if( key.equals(prop.getName())) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public boolean containsKey( String key ) {
		return indexOf(key) != -1;
	}

	public void put(String key, String value) {
		put(new Property(key,value));
	}
	
	public void put( Property property ) {
		Check.notNull(property, "Argument 'property' cannot be null");
		
		int p = indexOf(property.getName());
		if( p != -1 ) {
			properties().remove(p);
			properties().add(p, property);
		}
		else {
			properties().add(property);
		}
		
	}

	public void add(String key, String value) {
		properties().add(new Property(key, value));
	}
	
	public boolean addIfNotExists(String key, String value) {

		if( !containsKey(key) ) {
			properties().add(new Property(key, value));
			return true;
		}
		return false;
	}
	
	/**
	 * Put all prorties in the specified instance to this instance. 
	 * If a property with the same name exists in this instance it will 
	 * be overriden.  
	 * 
	 * @param props the instance to copy from 
	 */
	public void putAll( AppProps props ) {
		if( props == null ) return;
		
		for( Property current : props.list() ) {
			this.put(current.name, current.value);
		}
	} 

	/**
	 * Put all prorties in the specified instance to this instance. 
	 * 
	 * @param props the instance to copy from 
	 */
	public void putOnlyIfNotExist( AppProps props ) {
		if( props == null ) return;
		
		for( Property current : props.list() ) {
			if( !containsKey(current.getName()) ) {
				put(current.name, current.value);
			}
		}
	} 
	
}

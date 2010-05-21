package models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import play.Play;
import util.Check;
import util.ReloadableSingletonFile;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import events.AppEvents;
import exception.QuickException;

@XStreamAlias("server")
public class AppConf {
	
	/** the configuration singleton instance */
	@XStreamOmitField private static final ReloadableSingletonFile<AppConf> INSTANCE;
	
	static {
		INSTANCE = new ReloadableSingletonFile<AppConf>(AppProps.SERVER_CONF_FILE) {
			@Override
			public void onReload(File file, AppConf conf) {
				super.onReload(file,conf);
				
				/* inject the conf in all modules */
				for( Module module : conf.modules ) {
					module.conf = conf;
				}				
				
				/* notify this event */
				AppEvents.appConfChanged(conf);
			}
			
		};
	}
	

	/** the common {@link Definition} instance */
	public Definition def;
	
	@XStreamImplicit(itemFieldName="module")
	public List<Module> modules = new ArrayList<Module>();
	
	public Definition getDef() {
		if( def == null ) {
			def = new Definition();
		}
		return def;
	}
	
	/**
	 * Look up the {@link Module} instance named <code>name</code>
	 * 
	 * @param name the module unique identifier  
	 * @return the specified instance 
	 * @throws QuickException if not module is found
	 */
	public Module module(String name) {
		Check.notEmpty(name,"Argument name cannot be empty");
		
		for( Module m : modules ) {
			if( name.equalsIgnoreCase(m.name) ) {
				return m;
			}
 		}
		
		throw new QuickException("Unable to find module named: '%s'", name);
	}
	
	/**
	 * Access method to configuration singleton instance
	 * 
	 * @return
	 */
	public static AppConf instance() {
		return INSTANCE.get();
	}
	
	/**
	 * @return the current configuaration file
	 */
	public static File getFile() {
		return Play.getFile("conf/tserver.conf.xml"); 
	}
	

	public List<String> getGroups() {
		List<String> result = new ArrayList<String>();
		
		for( Module m : modules ) {
			String g = (m.group==null) ? "" : m.group.trim(); // <-- normalize the group name;
			
			if( !result.contains(g) ) {
				result.add(g);
			}
		}
		
		return result;
	}
	
	public List<Module> modulesByGroup(String group) {
		Check.notNull(group,"Argument 'group' cannot be null");
		group = group.trim();
		List<Module> result = new ArrayList<Module>();
		
		for( Module m : modules ) {
			String g = (m.group==null) ? "" : m.group.trim(); // <-- normalize the group name;
			
			if( group.equals(g) ) {
				result.add(m);
			}
		}
		
		return result;
		
	}

	public long getLastModified() {
		return INSTANCE.getLastModified();
	}
	
}

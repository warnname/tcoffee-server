package util;

import java.io.File;

import play.Logger;

/**
 * A singleton instance readed from a file that is refreshed whne the file is changed 
 * 
 * @author Paolo Di Tommaso
 *
 * @param <T>
 */
public class ReloadableSingletonFile<T> {

	private T instance;
	
	private long confLastModified;
	
	File file;


	public ReloadableSingletonFile(File file) {
		Check.notNull(file, "Argument 'file' cannot be null");
		this.file = file;
	}

	public long getLastModified() {
		return confLastModified;
	}
	
	public T readFile(File file) {
		return XStreamHelper.fromXML(file);
	}
	
	public boolean isValid( File file ) {
		return confLastModified==file.lastModified();
	}
	
	public void onReload( File file, T instance ) {
		confLastModified = file.lastModified();
	}
	
	public final T get() {

		if( instance != null && isValid(file)) {
			return instance;
		}

		Logger.info("Loading file: '%s'", file);
		
		/* ok */
		instance = readFile(file);
		onReload(file, instance);
		return instance;
	}
	
}

package models;

import java.io.File;
import java.io.Serializable;

import play.Play;
import util.Utils;

@Deprecated
public class Doc implements Serializable {

	public File file;
	
	public String name;
	
	public String webpath; 
	
	
	public void setFile(File file) {
		this.file = file;
		this.name = file.getName();
		
		/*
		 * the file path have to be published under the framework root, 
		 * being so the 'framework path' is the prefix of the file full path
		 */
		String path = Utils.getCanonicalPath(file);
		String root = Utils.getCanonicalPath(Play.applicationPath);
		
		int p = path.indexOf(root);
		if( p==0 ) {
			webpath = path.substring(root.length());
			if( webpath.charAt(0) != '/' ) {
				webpath = "/" + webpath;
			}
		}
		
	}
	

	public boolean exists() {
		return file != null && file.exists();
	}
	
	
	@Override
	public String toString() {
		return webpath;
	}
}

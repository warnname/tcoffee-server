package models;

import java.io.File;

import play.Logger;
import play.Play;
import util.Check;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("item")
public class OutItem {

	/**
	 * The http web path of this file 
	 */
	public String webpath; 

	/**
	 * A descriptive label
	 */
	public String label;
	
	/** 
	 * The type of file as defined by t-coffee i.e. <code>GUIDE_TREE</code>, <code>MSA</code>
	 */
	public String type;
	
	/**
	 * the name relative to the current local context folder  
	 */
	public String name; 
	
	/** 
	 * The real file on file-system
	 */
	public File file;
	
	/** 
	 * defines the content <i>format</i> of the result file i.e. <code>html</code>, 
	 * <code>score_html</code>, <code>score_ascii</code>, <code>clustalw_aln</code> ...
	 */
	public String format;
	
	/** The default constructor */
	public OutItem() {}
	
	/** The copy constructor */
	public OutItem(OutItem obj) {
		this.file = obj.file;
		this.webpath = obj.webpath;
		this.label = obj.label;
		this.type = obj.type;
	}
	
	/** set the file property and infer as well as the {@link #name} and {@link OutItem#webpath} attributes */
	public OutItem( File file, String type ) {
		this.file = file;
		this.type = type;
		this.name = file.getName();
		this.webpath = webpathFor(file);
		this.format = defaultFormatByName(name);
		this.label = defaultLabelByName(name);
	}
	
	/** 
	 * set the {@link #name} property and infer as well as the {@link #file} and {@link #webpath} attributes 
	 */
	public OutItem( String name, String type ) {
		this.name = name;
		this.type = type;
		
		Module module = Module.current();
		File folder = module != null ? module.folder() : null;
		if( folder != null ) {
			this.file = new File(folder,name);
			this.webpath = webpathFor(file);
		}
		else {
			Logger.warn("Local folder for current module is not defined");
		}

		this.format = defaultFormatByName(name);
		this.label = defaultLabelByName(name);
	}

	private String ext( String name ) {
		int p = name.lastIndexOf(".");
		if( p != -1 ) {
			return name.substring(p+1); 
		}
		
		return null;
	}

	private String defaultFormatByName(String name) {
		Check.notNull(name,"Argument null cannot be null");
		return ext(name);
	} 
	
	// TODO make it configurable through XML properties/consts
	private String defaultLabelByName(String name) {
		String ext = ext(name);
		if( ext == null ) {
			return "File " + name;
		}
		else if( ext.equals("html") || ext.equals("score_htm")) {
			return "Sequence alignment in HTML format";
		}
		else  if( ext.equals("score_ascii") ) {
			return "Sequence alignment in ASCII format";
		}
		else  if( ext.equals("score_ascii") ) {
			return "Sequence alignment in ClustalW format";
		}
		else {
			return "File ." + format;
		}
	}
	
	private String webpathFor( File file ) {
		/*
		 * the file path have to be published under the framework root, 
		 * being so the 'framework path' is the prefix of the file full path
		 */
		String context = Play.configuration.getProperty("context");
		String path = Utils.getCanonicalPath(file);
		String root = AppProps.instance().getDataPath();
		
		String result = null;
		int p = path.indexOf(root);
		if( p==0 ) {
			result = path.substring(root.length());
			if( result.charAt(0) != '/' ) {
				result = "/" + result;
			}
			result = "/data" + result;
			
			if( Utils.isNotEmpty(context)) {
				result = context + result;
			}
			
		}
		
		return result;
	}
	
	/**
	 * Check that the file exists on the file system 
	 * @return
	 */
	public boolean exists() {
		return file != null && file.exists();
	} 
}

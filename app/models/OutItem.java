package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Iterator;

import org.blackcoffee.commons.utils.ReaderIterator;

import play.libs.IO;
import play.templates.JavaExtensions;
import plugins.AutoBean;
import util.Check;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@AutoBean
@XStreamAlias("item")
public class OutItem implements Serializable {

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
	
	public String aggregation;
	
	/** The default constructor */
	public OutItem() {}
	
	/** The copy constructor */
	public OutItem(OutItem obj) {
		this.aggregation = obj.aggregation;
		this.file = obj.file;
		this.format = obj.format;
		this.label = obj.label;
		this.name = obj.name;
		this.type = obj.type;
		this.webpath = obj.webpath;
	}
	
	/** 
	 * set the file property and infer as well as the {@link #name} and {@link OutItem#webpath} attributes 
	 *
	 */
	public OutItem( File file, String type ) {
		this.file = file;
		this.type = type;
		this.name = file.getName();
		this.format = defaultFormatByName(name);
		this.label = defaultLabelByName(name);
		this.aggregation = defaultAggregationByType(type);
		fixLabel();
	}

	
	/** 
	 * set the {@link #name} property and infer as well as the {@link #file} and {@link #webpath} attributes 
	 */
	public OutItem( String name, String type ) {
		this.name = name;
		this.type = type;
		
		this.format = defaultFormatByName(name);
		this.label = defaultLabelByName(name);
		this.aggregation = defaultAggregationByType(type);

		fixLabel();
	}
	
	private void fixLabel() {
		if( "Template Profile".equals(aggregation) ) {
			this.label = this.name;
		}
	}
	
	private String defaultAggregationByType(String type) {
		Definition def = Service.current().bundle.def;
		Dictionary dict = def != null ? def.dictionary : null;
		String result = dict != null ? dict.decode(type) : null;
		return result != null ? result : type;
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
			return name + " file";
		}
		else {
			return format + " file";
		}
	}
	

	
	/**
	 * Check that the file exists on the file system 
	 * @return
	 */
	public boolean exists() {
		return file != null && file.exists();
	} 
	
	
	public String toString() { 
		return Utils.dump(this, "name", "label", "type", "file", "format", "webpath", "aggregation");
	}
	
	
	public OutItem label(String label) { 
		this.label = label;
		return this;
	}
	
	public OutItem type( String type ) { 
		this.type = type;
		return this;
	}
	
	public OutItem name( String name ) { 
		this.name = name;
		return this;
	}
	
	public OutItem webpath( String webpath ) { 
		this.webpath = webpath;
		return this;
	}
	
	public OutItem format( String format ) { 
		this.format = format;
		return this;
	}
	
	public String content() {
		return file != null ? IO.readContentAsString(file) : null;
	} 
	
	public String toLine() throws FileNotFoundException {

		if( file == null || !file.exists() || !file.isFile() ) return null;
		
		StringBuilder result = new StringBuilder();
		Iterator<String> it = new ReaderIterator(new FileReader(file)).iterator();
		while( it.hasNext() ) {
			result.append(it.next().trim());
		}

		return result.toString().replaceAll("(\\:([^\\[]*))?\\[([^\\]]*)\\]$?","");
	} 
	
	public long size() { 
		return file != null && file.exists() ? file.length() : 0;
	}
}

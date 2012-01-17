package models;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import plugins.AutoBean;
import util.Check;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@AutoBean
@XStreamAlias("result")
public class OutResult implements Serializable {
	
	@XStreamImplicit(itemFieldName="item")
	private List<OutItem> _items;

	/** The amount of time (in millis) to execute the job */
	@XStreamAlias("elapsed-time")
	public long elapsedTime;
	
	public Status status;
	
	/** Name of the bundle that produced this result */
	public String bundle;
	
	/** The bundle service name executed (i.e. the T-Coffee mode)*/
	public String service;
	
	/** the descriptive mode title */
	public String title;
	
	/** The url reference to cite the alignment result */
	public String cite;
	
	@XStreamImplicit(itemFieldName="error")
	public List<String> errors;

	@XStreamImplicit(itemFieldName="warn")
	public List<String> warnings;
	
	
	/** The default constructor */
	public OutResult() {
	}
	
	/** The copy constructor */
	public OutResult(OutResult that) {
		this._items = Utils.copy(that._items);
		this.elapsedTime = that.elapsedTime;
		this.status = that.status;
		this.errors = that.errors;
		this.service = that.service;
		this.title = that.title;
		this.bundle = that.bundle;
		this.cite = that.cite;
	}

	public List<OutItem> getItems() {
		return items();
	}
	
	public OutItem getItem( String name ) { 
		return Utils.firstItem(items(), "name", name);
	}
	
	private List<OutItem> items() {
		//TODO manage concurrency 
		if( _items == null ) {
			_items = new ArrayList<OutItem>();
		}
		return _items;
	}
	
	public void add(OutItem item) {
		Check.notNull("Argument 'item' cannot be null");
		items().add(item);
	}
	
	public OutItem first(String property, String value) {
		return Utils.firstItem(items(), property, value);
	}
	
	/**
	 * @return The list of all types (categories) of OutItems instances. This is used to show 
	 * the table of produced output in a catagorized manned in the result page.
	 * 
	 */
	public List<String> aggregations() {
		List<String> result = new ArrayList();
		for( OutItem item : items() ) {
			String _type = item.aggregation;
			if( !result.contains(_type) ) {
				result.add(_type);
			}  
		}
		return result;
	}
	
	/**
	 * Filter the list of {@link OutItem}s instances by the {@link OutItem#aggregation} property
	 */
	public List<OutItem> filter( String value ) {
		return Utils.getItems(items(), "aggregation", value);
	} 

	/**
	 * @return the list of 'input' items in this result, or an empty list if no input is available
	 */
	public List<OutItem> getInputItems() { 
		return Utils.getItems(items(), "type", "input_file");
	}
	
	
	
	public List<File> getInputFiles() { 
		List<OutItem> items= getInputItems();
		
		List<File> result = new ArrayList<File>(items.size()); 
		for( OutItem item : items ) { 
			result.add(item.file);
		}
		
		return result;
	}
	

	public void addAll(OutResult that) {
		if( that == null ) { return; }

		if( that.errors != null ) {
			addErrors( that.errors );
		}

		if( that.warnings != null ) {
			addWarnings( that.warnings );
		}
		
		if( that._items != null ) {
			items().addAll(that._items);
		}
	}
	
	public void addAll(List<OutItem> result) {
		Check.notNull(result, "Argument result cannot be null");
		items().addAll(result);
	}
	
	public OutItem getAlignmentHtml() {
		
		OutItem result = Utils.firstItem(items(), "format", "html");

		if( result == null ) {
			result = Utils.firstItem(items(), "format", "score_html");
		}
		
		return result;
	} 
	
	public OutItem getAlignmentFasta() {
		return Utils.firstItem(items(), "format", "fasta_aln");
	} 
	
	public OutItem getCommandLine() {
		return  Utils.firstItem(items(), "format", "cmdline");
	}
	
	/**
	 * @return the output item containing the program standard output 
	 * 
	 * see {@link ShellCommand#done(boolean)}
	 */
	public OutItem getStdout() {
		return  Utils.firstItem(items(), "type", "stdout");
	} 

	/**
	 * @return the output item containing the program standard error
	 * 
	 * see {@link ShellCommand#done(boolean)}
	 */
	public OutItem getStderr() {
		return  Utils.firstItem(items(), "type", "stderr");
	} 
	
	public void addError( String message ) {
		if( StringUtils.isBlank(message)) return;
		
		if( errors == null ) {
			errors = new ArrayList<String>();
		}
		
		errors.add(message);
	}
	
	public void addErrors( List<String> errors ) {
		if( errors == null ) return;
		
		
		if( this.errors == null ) {
			this.errors = new ArrayList<String>();
		}
		
		this.errors.addAll(errors);
	}
	
	public void clearErrors() {
		if( errors != null ) {
			errors.clear();
		}
	} 

	public String getElapsedTimeFmt() {
		return Utils.asDuration( elapsedTime );
	}

	
	public void addWarning( String warn ) {
		if( StringUtils.isBlank(warn) ) return;
		
		if( this.warnings == null ) {
			this.warnings = new ArrayList<String>();
		}
		
		this.warnings.add(warn);
	}

	public void addWarnings( List<String> warns) {
		if( warns == null ) return;
		
		
		if( this.warnings == null ) {
			this.warnings = new ArrayList<String>();
		}
		
		this.warnings.addAll(warns);
	}
	
	/**
	 * 
	 * @return <code>true</code> if contains at least one warning
	 */
	public boolean hasWarnings() { 
		return warnings != null && warnings.size()>0;
	}	
	
	public String toString() { 
		return Utils.dump( this, "bundle", "mode", "status", "title", "_items" );
	}
	
}

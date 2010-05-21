package models;

import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("out-section")
public class OutSection  {
	
	/** The main title */
	public String title;
	
	/** The page description */
	public String description;

	/** the output description */
	public OutResult result;
	
	/** the events to be raised/executed in this section */
	public ProcessCommand events;
	
	/** The default constructor */
	public OutSection() { }
	
	/** The copy constructor */
	public OutSection(OutSection that) {
		this.title = that.title;
		this.description = that.description;
		this.result = Utils.copy(that.result);
		this.events = Utils.copy(that.events);
	} 
	
	
	public boolean hasEvents() {
		return events != null && events.hasCommands(); 
	}
	
	public void append( OutSection that ) {
		if( that == null ) return;
		
		this.title = that.title;
		this.description = that.description;
		
		if( this.events != null ) {
			this.events.addAll(that.events);
		}
		
		if( this.result != null ) {
			this.result.addAll(that.result);
		}
		
	}
}

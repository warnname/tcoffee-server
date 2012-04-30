package models;

import java.io.Serializable;

import plugins.AutoBean;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@AutoBean
@XStreamAlias("out-section")
public class OutSection implements Serializable  {
	
	/** the output description */
	public OutResult result;
	
	/** the events to be raised/executed in this section */
	public ProcessCommand events;
	
	/** The default constructor */
	public OutSection() { }
	
	/** The copy constructor */
	public OutSection(OutSection that) {
		this.result = Utils.copy(that.result);
		this.events = Utils.copy(that.events);
	} 
	
	
	public boolean hasEvents() {
		return events != null && events.hasCommands(); 
	}
	
	public void add( AbstractCommand cmd ) { 
		if( cmd == null ) return;
		
		if( events == null ) { 
			events = new ProcessCommand();
		}
		events.add(cmd);
	}
	
	public void addAll( OutSection that ) {
		if( that == null ) return;
		
		/*
		 * add all events from 'that'
		 */
		if( that.events != null && that.events.hasCommands() ) {
			if( this.events == null ) { 
				this.events = Utils.copy(that.events);
			}
			else { 
				this.events.addAll(that.events);
			}
		}
		
		/*
		 * add all results from that
		 */
		if( that.result != null ) {
			if( this.result == null ) { 
				this.result = new OutResult();
			}
			this.result.addAll(that.result);
		}
		
	}
	
	public String toString() { 
		return Utils.dump(this, "title", "result", "events");
	}
}

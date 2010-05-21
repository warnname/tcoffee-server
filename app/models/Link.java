package models;

import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Models a link in the output page
 * 
 * @author Paolo Di Tommaso
 *
 */

@XStreamAlias("link")
public class Link {

	@XStreamAsAttribute
	public String href;
	
	@XStreamAsAttribute
	public String label;
	
	@XStreamAsAttribute
	public String target;
	
	
	public Link() {}
	
	public Link(Link that)  {
		this.href = Utils.copy(that.href);
		this.label = Utils.copy(that.label);
		this.target = Utils.copy(that.target);
	}
}

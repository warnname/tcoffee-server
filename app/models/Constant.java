package models;

import plugins.AutoBean;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@AutoBean
@XStreamAlias("const")
public class Constant {
	 
	public String name; 
	
	public String value;
	
	/** The Default constructor */
	public Constant() {}
	
	public Constant(final String name, final String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() { 
		return value;
	}
	
 
}

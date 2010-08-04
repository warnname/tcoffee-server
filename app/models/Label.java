package models;

import plugins.AutoBean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@AutoBean
@XStreamAlias("label")
public class Label {

	@XStreamAsAttribute
	public String key;
	
	@XStreamAsAttribute
	public String value;
}

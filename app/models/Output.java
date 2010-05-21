package models;

import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("output")
public class Output {

	public OutSection valid;
	
	public OutSection fail;
	
	
	public Output() {} 
	
	public Output(Output that) {
		this.valid = Utils.copy(that.valid);
		this.fail = Utils.copy(that.fail);
	}
	
}

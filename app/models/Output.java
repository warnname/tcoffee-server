package models;

import java.io.Serializable;

import plugins.AutoBean;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@AutoBean
@XStreamAlias("output")
public class Output implements Serializable {

	public OutSection valid;
	
	public OutSection fail;
	
	
	public Output() {} 
	
	public Output(Output that) {
		this.valid = Utils.copy(that.valid);
		this.fail = Utils.copy(that.fail);
	}
	
	
	public String toString() { 
		return Utils.dump(this,"valid","fail");
	}
}

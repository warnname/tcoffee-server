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
	
	public void addToValid( AbstractCommand cmd ) { 
		if( cmd == null ) { return; }
		if( valid == null) { 
			valid = new OutSection();
		}
		valid.add(cmd);
	}

	public void addToFail( AbstractCommand cmd ) { 
		if( cmd == null ) { return; }
		if( fail == null) { 
			fail = new OutSection();
		}
		fail.add(cmd);
	}
	
	
	public String toString() { 
		return Utils.dump(this,"valid","fail");
	}
}

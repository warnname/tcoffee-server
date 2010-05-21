package models;

import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("def")
public class Definition {

	@XStreamAlias("valid-result")
	OutSection validResult;
	
	@XStreamAlias("fail-result")
	OutSection failResult;

	public Definition() {} 
	
	public Definition( Definition that ) {
		this.validResult = Utils.copy(that.validResult);
		this.failResult = Utils.copy(that.failResult);
	}
	
	
}

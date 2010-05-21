package models;

import util.Check;
import util.ChiperHelper;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("property")
public class Property {
	
	@XStreamAsAttribute
	public String name; 
	
	@XStreamAsAttribute
	public String value;
	
	@XStreamAsAttribute
	public Boolean encrypted;

	/** The Default constructor */
	public Property() {}
	
	/**
	 * The copy constructor 
	 * @param that the instance which copy from 
	 */
	public Property( Property that ) {
		this.name = that.name;
		this.value = that.value;
		this.encrypted = that.encrypted;
	} 
	
	public Property(final String name, final String value) {
		setName(name);
		setValue(value);
	}
	
	public Property(boolean encrypted) {
		this.encrypted = encrypted;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean getEncrypted() {
		return encrypted != null && encrypted;
	}
	
	public void setEncrypted(boolean value) {
		encrypted = value==true ? Boolean.TRUE : (Boolean)null;
	}
	
	public void setName( String name ) {
		Check.notEmpty(name, "Property name cannot be empty");
		Check.isFalse( name.contains( "." ), "Property '%s' is not a valid name. Char '.' is not allowed", name );
		Check.isFalse( name.contains( "[" ), "Property '%s' is not a valid name. Char '[' is not allowed", name );
		Check.isFalse( name.contains( "]" ), "Property '%s' is not a valid name. Char ']' is not allowed", name );
		this.name = name;
	}
	
	public String getValue() { 
		return encrypted ? ChiperHelper.decrypt(value) : value;
	}
	
	public void setValue( String value ) {
		this.value = encrypted ? ChiperHelper.encrypt(value) : value;
	}
	
	@Override
	public String toString() {
		return Utils.dump(this,"name","value");
	}

	@Override
	public boolean equals(Object obj) {
		return Utils.isEquals(this, obj, "name", "value");
	}
	
	@Override
	public int hashCode() {
		return Utils.hash(this, "name", "value");
	}
 
}

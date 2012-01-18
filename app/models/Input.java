package models;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import util.Utils;
import util.XStreamHelper;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("input")
public class Input implements Serializable {

	@XStreamImplicit(itemFieldName="fieldset")
	public List<Fieldset> fieldsets;

	@XStreamOmitField
	private List<Field> __fields; 

	
	/**
	 * @return only the {@Link Fieldset}s not {@Link Fieldset#hideable}    
	 */
	public List<Fieldset> getBasics() {
		List<Fieldset> result = new ArrayList<Fieldset>();
		for( Fieldset f: fieldsets ) {
			if( !f.hideable ) {
				result.add(f);
			};
		}
		
		return result;
	}
	
	public List<Fieldset> getHideables() {
		List<Fieldset> result = new ArrayList<Fieldset>();
		for( Fieldset f: fieldsets ) {
			if( f.hideable ) {
				result.add(f);
			};
		}
		
		return result;
	}
	
	
	/** The default constructor */
	public Input( ) {
		
	}
	
	/** The copy cosntructor */
	public Input( Input that ) {
		this.fieldsets = Utils.copy(that.fieldsets);
	}
	
	public Input( Fieldset ... fieldset ) { 
		if( fieldset == null ) return;
		
		for( Fieldset set: fieldset ) { 
			fieldsets().add(set);
		}
	}
	
	public synchronized List<Fieldset> fieldsets() {
		if( fieldsets == null ) {
			fieldsets = new ArrayList<Fieldset>();
		}
		return fieldsets;
		
	}
	
	public void add( Fieldset... fieldsetArray ) {
		for( Fieldset s : fieldsetArray ) {
			fieldsets().add(s);
		}
	}
	
	/**
	 * Binds this input object description to the specified {@link Request} and {@link Params} instances. 
	 * In other words fetch data from the http requests and populate the input model accordingly
	 *    
	 * @param request the current Play request 
	 * @param params the current Play request parameters 
	 */
	public void bind( final Params params ) {
		__fields = null;
		
		for( Field f : fields() ) {
			f.bind(params);
		}
	}
	
	
	public List<Field> fields() {
		if( __fields != null ) {
			return __fields;
		}
		
		List<Field> result = new ArrayList<Field>();
		if(fieldsets != null) for( Fieldset set : fieldsets ) {
			if(set.fields!=null) for( Field input : set.fields ) {
				result.add(input);
			}
		}
		
		return __fields = result;
	}
	
	public List<Field> fields( String selectByName ) { 
		List<Field> all = fields();

		if( Utils.isEmpty(selectByName) ) { 
			return all;
		}
		
		List<Field> result = new ArrayList<Field>();
		for( Field field : all ) { 
			if( selectByName.equals(field.name)) { 
				result.add(field);
			}
		}
		
		return result;
	}
	
	public Field field( String fieldName ) { 
		List<Field> result = fields(fieldName);
		return result.size()>0 ? result.get(0) : null;
	}
	
	/**
	 * @param fieldName the name that identify the field in this input
	 * @return the {@link Field} instance for the specified name or <code>null</code> if a field with that name does not exist
	 */
	public Field getField( String fieldName ) { 
		List<Field> fields = fields(fieldName);
		if( fields == null || fields.size()==0 ) { 
			Logger.warn("Missing field with name: '%s'", fieldName);
			return null;
		}
		
		if( fields.size()>1 ) {
			Logger.warn("Multiple fields with name: '%s'", fieldName);
		}
		
		return fields.get(0);
	}

	/**
	 * @param fieldName the name that identify the field in this input
	 * @return the string value associated with the field, or <code>null</code> if a field with that name does not exist
	 */
	public String getValue( String fieldName ) { 
		Field result = getField(fieldName);
		return result != null ? result.value : null;
	}
	
	public String getValue( String field, String defValue ) {
		String result = getValue(field);
		return result != null ? result : defValue;
	}
	
	public void validate() {
		for( Field f : fields() ) {
			f.validate();
		}
	}
	
	/**
	 * @return the unique identifier for this request 
	 */
	int hashFields() {
		
		int hash = Utils.hash();
		for( Field f : fields() ) {
			hash = Utils.hash(hash, f.name);
			hash = Utils.hash(hash, f.value);
		}

		return hash;
	}
	
	
	/**
	 * Save this {@link Input} instance as a XML file 
	 * 
	 * @param file the file to which save the input collection 
	 * 
	 */
	public void save( File file ) {
		XStreamHelper.toXML(this, file);
	}
	
	public static Input read( File file ) {
		return (Input) (file.exists() ? XStreamHelper.fromXML(file) : null);
	}
	
}

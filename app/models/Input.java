package models;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.data.validation.Validation;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import util.DSL.Pair;
import util.Utils;
import util.XStreamHelper;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Models the input data for a service invocation and contains list of {@link Field} 
 * groupped by a set of {@link Fieldset}
 * 
 * @author Paolo Di Tommaso
 *
 */
@XStreamAlias("input")
public class Input implements Serializable {

	@XStreamImplicit(itemFieldName="fieldset")
	public volatile List<Fieldset> fieldsets;

	@XStreamOmitField
	private volatile List<Field> __fields; 
	
	@XStreamAlias("validation-script")
	public Script validation;
	

	/**
	 * Create and initialize an {@link Input} instance with the provided fields 
	 * 
	 * @param fields an open array of {@link Field} instances
	 * @return the {@link Input} instance containing the provided fields 
	 */
	public static Input create( Field ... fields ) {
	
		Input result = new Input();
		// add the fields to the fieldset
		Fieldset set = new Fieldset();
		set.add(fields);
		// add the fieldset to the input 
		result.fieldsets().add(set);
		
		return result;		
	}

	/**
	 * Create and initialize an {@link Input} instance with the provided fields. 
	 * <p>By default the field type is 'text', to specify a different one 
	 * use the colon as separator 
	 * 
	 * <pre>
	 * Input in = Input.create("alpha=1", "beta=2", "gamma:memo=some text")
	 * </pre>
	 * 
	 * @param fieldsAndValues
	 * @return
	 */
	public static Input create( String ... fieldsAndValues ) {

		Fieldset set = new Fieldset();

		for( String pair : fieldsAndValues ) {
			Pair keyvalue = Pair.create(pair,"=","");
			Pair nametype = Pair.create(keyvalue.first,":","text");
			
			set.add(new Field( nametype.second, nametype.first, keyvalue. second ));
		}
		
		Input result = new Input();
		result.fieldsets().add(set);
		
		return result;
	}
	
	/** 
	 * Default constructor 
	 */
	public Input( ) {
		
	}
	
	/** 
	 * Copy cosntructor 
	 */
	public Input( Input that ) {
		this.fieldsets = Utils.copy(that.fieldsets);
		this.validation = Utils.copy(that.validation);
	}
	
	/**
	 * Create an input set with the provided {@link Fieldset}
	 * @param fieldset
	 */
	public Input( Fieldset ... fieldset ) { 
		if( fieldset == null ) return;
		
		for( Fieldset set: fieldset ) { 
			fieldsets().add(set);
		}
	}
	
	public List<Fieldset> fieldsets() {
		List<Fieldset> result = fieldsets;
		if( result == null ) synchronized (this) {
			result = fieldsets;
			if( result == null ) {
				fieldsets = result = new ArrayList<Fieldset>();
			}
		}
		return result;
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
		List<Field> result = __fields;
		if( result == null ) synchronized (this) {
			result = __fields;
			if( result == null ) {
				// populate the result list as set in to 'lazy' field
				result = new ArrayList<Field>();
				if(fieldsets != null) for( Fieldset set : fieldsets ) {
					if(set.fields!=null) for( Field input : set.fields ) {
						result.add(input);
					}
				}
				__fields = result;
			}
		}
		
		return result;
	}
	
	/**
	 * @param selectByName the name of the fields to retrieve
	 * @return a list of {@link Field} that match the name specified by <code>selectByName</code>
	 */
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
	
	/**
	 * The 'first' field that matches the specified name in the input 
	 * 
	 * @param fieldName the name of the field to retrieve 
	 * @return the field that matches the provide name of <code>null</code> if no field with that name is found 
	 */
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
	
	/**
	 * Validate all the fields value in the input set against the validation 
	 * rules associated to each field
	 * 
	 * See {@link ValidationCheck}
	 */
	public void validate() {
		for( Field f : fields() ) {
			f.validate();
		}
		
		if( validation != null && !Validation.hasErrors() ) { // <-- this is procecced only if there aren't other errors 
			applyScriptValidation();
		}
	}
	
	private void applyScriptValidation() {


		Service service = Service.current();
		if( service == null ) {
			Logger.warn("Cannot access current service. Skipping script validation");
			return;
		}
		
		/*
		 * pass the field under validation
		 */
		validation.setProperty("input", this);

		/*
		 * invoke the script
		 */
		Object result = validation.run().getResult();
		
		
		/*
		 * any object returned by the script is interpreted as an error message 
		 */
		if( result != null ) {
			Validation.addError("_input_form", result.toString(), new String[0]);
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
	
	/**
	 * Read the {@link Input} from its XML representation
	 * @param file the stored XML input 
	 * @return a populated {@link Input} instance for the specified file, or <code>null</code> if the file does not exist
	 */
	public static Input read( File file ) {
		return (Input) (file.exists() ? XStreamHelper.fromXML(file) : null);
	}
	
}

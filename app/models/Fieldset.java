package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("fieldset")
public class Fieldset implements Serializable {

	public String title;
	
	public String description;

	public String custom;
	
	@XStreamImplicit(itemFieldName="field")
	public List<Field> fields = new ArrayList<Field>();
	
	@XStreamAsAttribute
	public boolean hideable;

	
	public Fieldset() {
		
	}
	
	public Fieldset( Fieldset that ) {
		this.title = Utils.copy(that.title);
		this.description = Utils.copy(that.description);
		this.hideable = that.hideable;
		this.fields = Utils.copy(that.fields);
                this.custom = that.custom;
	}
	
	public Fieldset( Field ... fields ) { 
		if( fields == null ) return;
		
		for( Field ff : fields ) { 
			fields().add(ff);
		}
	}
	
	/** safe accessor method to {@link #fields} attribute */
	synchronized List<Field> fields() {
		if( fields == null ) {
			fields = new ArrayList<Field>();
		}
		return fields;
	}
	
	public void add( Field ... fieldArray ) {
		
		for( Field f : fieldArray ) {
			fields().add(f);
		}

	} 
}

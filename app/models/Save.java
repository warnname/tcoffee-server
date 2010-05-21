package models;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


/**
 * Models a field content <i>save</i> action. Specifing this element the content of the field 
 * will be saved on the job context folder using the provided file name.
 * 
 * @author Paolo Di Tommaso
 *
 */
@XStreamAlias("save")
public class Save {

	/** The file name to be used to store this field */
	@XStreamAsAttribute
	@XStreamAlias("as")
	public String name;
	
	/** Default entity constructor, initialize the object with an empty name*/
	public Save() { }
	
	/** Copy constuctor. Creates a new instance copying the content provided */ 
	public Save( Save obj ) {
		this.name = obj.name;
	}
	
}

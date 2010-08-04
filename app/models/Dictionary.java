package models;

import java.util.ArrayList;
import java.util.List;

import plugins.AutoBean;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A collection of {@link Label} 
 * 
 * @author Paolo Di tommaso
 *
 */

@AutoBean
@XStreamAlias("dictionary")
public class Dictionary {

	@XStreamImplicit(itemFieldName="label")
	public List<Label> labels;
	
	
	/**
	 * Decode the specified key returning the associated label value
	 * 
	 * @param key
	 * @return
	 */
	public String decode( String key, String defValue ) {
		if( labels == null ) return defValue;
		
		for( Label label : labels ) {
			if( Utils.isEquals(key, label.key) ) {
				return label.value;
			}
		}
		
		return defValue;
	}
	
	public void addLabel( String key, String value ) {
		Label label = new Label();
		label.key = key;
		label.value = value;
	
		if( labels == null ) {
			labels = new ArrayList<Label>();
		}
		
		labels.add(label);
	}
	
	public String decode( String key ) {
		return decode(key,key);
	}
	
}

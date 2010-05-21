package converters;

import java.lang.reflect.Array;
import java.util.Collection;

import util.Utils;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SimpleCollectionConverter implements Converter {

	
	private static final String SEPARATOR = ",";
	
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		
		Object[] values; 
		if( source.getClass().isArray() ) {
			values = ((Object[])source);
		}
		else if( source instanceof Collection ) {
			values = ((Collection) source).toArray();
		}
		else {
			throw new XStreamException("Unsupported collection type: " + source.getClass());
		}
		
		writer.setValue(Utils.asString(values, SEPARATOR, ""));
 
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

		String val = reader.getValue();
		String[] items = val != null ? val.split(SEPARATOR) : new String[0]; 

		/* target result is an array
		 */
		Class type = context.getRequiredType();
		if( type.isArray() ) {
	        Object result = Array.newInstance(type.getComponentType(), items.length);
	        int i = 0;
	        for (String v : items) {
	            Array.set(result, i++, v.trim());
	        }

	        return result;
		}
		
		throw new XStreamException("Target type not yet implemented!!");
	}

	public boolean canConvert(Class type) {
		return type.isArray() || Collection.class.isAssignableFrom(type);
	}

}

package converters;

import java.util.Iterator;

import models.AnyAttributeElement;
import util.Utils;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class AnyAttributeConverter<T extends AnyAttributeElement> implements Converter {

	private Class<T> target;
	
	public AnyAttributeConverter(Class target) {
		this.target = target;
	}
	
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		T args = (T) source;
		for( String name : args.getNames() ) {
			writer.addAttribute(name, args.get(name));
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) { 
		Iterator i = reader.getAttributeNames();
		
		T args = Utils.create(target);
		
		while( i.hasNext() ) {
			String name = (String) i.next();
			String value = reader.getAttribute(name);
			args.put(name, value);
		}
		
		return args;
	}

	public boolean canConvert(Class type) { 
		return target.isAssignableFrom(type);
	}

}

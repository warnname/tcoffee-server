package converters;

import models.CmdArgs;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * XStream converter for {@link CmdArgs}
 * 
 * @author Paolo Di Tommaso
 *
 */
public class CmdArgsConverter implements Converter {

	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		CmdArgs args = (CmdArgs) obj;
		writer.setValue( args.toRawString() );
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		CmdArgs args = new CmdArgs(reader.getValue());
		return args;
	}

	public boolean canConvert(Class clazz) {
		return CmdArgs.class.equals(clazz);
	}



}

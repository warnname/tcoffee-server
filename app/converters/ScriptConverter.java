package converters;

import models.Script;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Xml conveter for class {@link Script}
 * 
 * @author Paolo Di Tommaso
 *
 */
public class ScriptConverter implements Converter {

/**	@Override**/
	public boolean canConvert(Class clazz) {
		return Script.class.isAssignableFrom(clazz);
	}

/**	@Override**/
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext ctx) {
		Script script = (Script) obj;
		
		// set the attribte 'file' if exists
		if( script.file != null ) {
			writer.addAttribute("file", script.file);
		}
		

		// set the 'script test' as the node value
		if( StringUtils.isNotBlank(script.text)) {
			writer.setValue(script.text);
		}

	}

/**	@Override**/
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext ctx) {
		Script result = new Script();
		result.text = reader.getValue();
		result.file = reader.getAttribute("file");
		
		return result;
	}

}

package converters;

import models.ScriptCommand;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Custom converter for {@link ScriptCommand} class
 * @author Paolo Di Tommaso
 *
 */
public class ScriptCommandConverter implements Converter {

	@Override
	public boolean canConvert(Class clazz) {
		return ScriptCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext ctx) {
		ScriptCommand cmd = (ScriptCommand) obj;
		
		// set the attribte 'file' if exists
		if( cmd.fFile != null ) {
			writer.addAttribute("file", cmd.fFile);
		}
		
		// set the attribute 'class' if exists
		if( cmd.fClass != null ) {
			writer.addAttribute("clazz", cmd.fClass);
		}

		// set the 'script test' as the node value
		if( StringUtils.isNotBlank(cmd.fScriptText)) {
			writer.setValue(cmd.fScriptText);
		}

	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext ctx) {
		ScriptCommand result = new ScriptCommand();
		result.fScriptText = reader.getValue();
		result.fFile = reader.getAttribute("file");
		result.fClass = reader.getAttribute("clazz");
		
		
		return result;
	}

}

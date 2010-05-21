package converters;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import util.Utils;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import exception.QuickException;

public abstract class AbstractNameValuePairConverter implements Converter {

	private Class clazz;
	private String valueField;
	private String[] attrFields; 

	public AbstractNameValuePairConverter(Class target) {
		this(target,"value","name");
	}
	
	public AbstractNameValuePairConverter(Class target, String valueField, String... attrFields ) {
		this.clazz = target;
		this.valueField = valueField;
		this.attrFields = attrFields;
	}
	
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		Object value;

		if( attrFields != null ) {
			/* write out each attribute value */
			for( String name : attrFields ) {
				value = Utils.getField(source, name);
				if( value != null ) {
					writer.addAttribute(name, value.toString());
				}
			}
		}
		
		/* finally the value field */
		value = Utils.getField(source, valueField);
		if( value != null ) {
			writer.setValue(value.toString());
		}
	}


	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		Object obj;
		try {
			obj = clazz.newInstance();
		} catch (Exception e) {
			throw new XStreamException(String.format("Unable to instantiate class: %s", clazz), e);
		}
		
		if( attrFields != null ) {
			/* 
			 * read each attribute value 
			 */
			for( String name : attrFields ) {
				String str = reader.getAttribute(name);
				
				try {
					Object value;
					/* get the field declaration if exists */
					Field field = clazz.getDeclaredField(name);
					if( str == null && !field.getType().isPrimitive() ) {
						value = null;
					}
					else if( boolean.class.equals(field.getType()) || Boolean.class.equals(field.getType()) ) {
						value = Utils.parseBool(str);
					}
					else if( char.class.equals(field.getType()) || Character.class.equals(field.getType()) ) {
						value = (Utils.isEmpty(str)) ? '\0' : str.charAt(0);  
					}
					else if( byte.class.equals(field.getType()) || Byte.class.equals(field.getType()) ) {
						value = (Utils.isEmpty(str)) ? '\0' : str.charAt(0);
					}
					else if( short.class.equals(field.getType()) || Short.class.equals(field.getType()) ) {
						value = Utils.parseShort(str);
					}
					else if( int.class.equals(field.getType()) || Integer.class.equals(field.getType()) ) {
						value = Utils.parseInteger(str);
					}
					else if( long.class.equals(field.getType()) || Long.class.equals(field.getType()) ) {
						value = Utils.parseLong(str);
					}
					else if( float.class.equals(field.getType()) || Float.class.equals(field.getType()) ) {
						value = Utils.parseFloat(str);
					}
					else if( double.class.equals(field.getType()) || Double.class.equals(field.getType()) ) {
						value = Utils.parseDouble(str);
					}
					else if( BigDecimal.class.equals(field.getType()) ) {
						value = Utils.parseBigDecimal(str);
					}
					else {
						value = str;
					}

					field.setAccessible(true);
					field.set(obj, value);
					
				}
				catch( Exception e ) {
					throw new QuickException(e, "Unable to set object property named: '%s' on class: '%s'", name, clazz.getName());
				}				
			}
		}
		
		/*
		 * read the value
		 */
		String value = reader.getValue();
		
		Utils.setField(obj, valueField, value);
		
		return obj;
	}

	public boolean canConvert(Class type) {
		return clazz.equals(type);
	}

	
}

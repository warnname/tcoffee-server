package util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Objects {

	public static boolean equals( Object thisObj, Object thatObj ) {
		boolean equals = Utils.isEqualsClass(thisObj, thatObj);
		
		if( !equals || thisObj==null ) { 
			return equals;
		}
		
		/*
		 * extract all public fields
		 */
		for( Field field : getFields(thisObj) ) {
			Object val1 = Utils.getProperty(thisObj, field.getName());
			Object val2 = Utils.getProperty(thatObj, field.getName());
			
			equals = Utils.isEquals(val1, val2);
			if( !equals ) return false;
		}

		return true;
	}

	
	static List<Field> getFields(final Object obj ) {
		List<Field> result = new ArrayList<Field>();

		if( obj == null ) {
			return result;
		}
		
		Field[] fields = obj.getClass().getDeclaredFields();
		for( Field field : fields ) {
			if( Modifier.isPublic(field.getModifiers()) ) {
				result.add(field);
			}
		}
		
		return result;
	}
}

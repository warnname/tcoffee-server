package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;

import play.Logger;
import play.Play;
import exception.QuickException;

/**
 * 
 * Defines common static utility methods 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Utils {

	public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";

	private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(.+?)\\}"); 
	
	public interface MapValue<K,V> {
		public V get(K key); 
	} 
	
	public static boolean isEmpty( final CharSequence value ) { 
		return value == null || value.length() == 0;
	}
	
	public static boolean isNotEmpty( final CharSequence value ) { 
		return value != null && value.length() != 0;
	}
	
	public static boolean isNotEmpty( final Collection<?> value ) { 
		return value != null && !value.isEmpty();
	}

	public static boolean isEmpty( final Collection<?> value ) { 
		return value == null || value.isEmpty();
	}
	
	public static <T> boolean isEmpty( final T[] arr ) {
		return arr == null || arr.length==0;
	}
	
	public static <T> boolean isNotEmpty( final T[] arr ) {
		return !isEmpty(arr);
	} 
	
	public static String asString( Object value ) {
		return value != null ? value.toString() : null;
	}

	public static String asString( Float value ) { 
		return value != null ? value.toString() : "0";
	}

	public static String asString( Double value ) { 
		return value != null ? value.toString() : "0.0";
	}
	
	public static String asString( Long value ) { 
		return value != null ? value.toString() : "0";
	}
	
	public static String asString( Integer value ) { 
		return value != null ? value.toString() : "0";
	}
	
	public static String asString( Short value ) { 
		return value != null ? value.toString() : "0";
	}
	
	public static String asString( BigDecimal value ) { 
		return value != null ? value.toString() : "0";
	}

	public static String asString( Boolean value ) { 
		return value != null ? value.toString() : "false";
	} 

	
	/**
	 * Removes initial spaces, final spaces and all double spaces (if present) from a string.
	 *
	 * @param value The string to be normalized
	 * @return The normalized string
	 */
	public static String asString( String value ) { 
		if( value == null ) return "";
		value = value.trim().replaceAll(" {2,}", " ");
		return value;
	}
	
	public static String asString( Date date ) { 
		if( date == null ) { return ""; }
		
		Calendar now = new GregorianCalendar();
		Calendar value = new GregorianCalendar();
		value.setTime(date);
		
		DateFormat fmt; 
		if( value.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) ) {
			// in the same day use just the hours 
			fmt = new SimpleDateFormat("HH:mm");
		} 
		else if( value.get(Calendar.YEAR) == now.get(Calendar.YEAR) ) {
			fmt = new SimpleDateFormat("dd MMM");
		}
		else {
			fmt = new SimpleDateFormat("dd/MM/yyyy");
		}
		
		return fmt.format(date);
	}

	public static <T> String asString( T[] vals ) { 
		return asString(vals,", ");
	}
	
	public static <T> String asString( T[] vals, final String separator ) { 
		return asString(vals,separator,"'");
	}

	/**
	 * Converts a an array of generic object to a string representation
	 * 
	 * @param vals an array on generic objects
	 * @param separator the string to separated each element element in the array  
	 * @param stringDelimiter the string to be used to wrap each item of type {@link CharSequence} 
	 */
	public static <T> String asString( T[] vals, final String separator, String stringDelimiter ) { 
		StringBuilder result = new StringBuilder();
		int c=0;
		for( int i=0; vals != null && i < vals.length; i++ ) { 
			Object o = vals[i];
			if( o != null ) { 
				if( c++ > 0 ) { 
					result .append(separator);
				}

				if( o instanceof CharSequence && isNotEmpty(stringDelimiter) ) { 
					result.append(stringDelimiter).append(o.toString()).append(stringDelimiter);
				}
				else { 
					result.append(o.toString());
				}
			}
		}
		
		return result.toString();
	}
	
	public static <T> String asString( Collection<T> vals ) {
		T[] array = (T[]) vals.toArray();
		return asString(array);
		
	}
	
	
	public static <K,V> String asString( Map<K,V> map ) {
		return asString(map,", ");
	}
	
	
	public static <K,V> String asString( Map<K,V> map, String separator ) {
		
		StringBuilder result = new StringBuilder();
		int i=0;
		for( Map.Entry<K, V> item : map.entrySet() ) {
			if( i++>0 ) {
				result.append(separator);
			}
			
			result
				.append( asString(item.getKey()) )
				.append( "=" )
				.append( asString(item.getValue()) );
		}
		
		return result.toString();
	}	
	
	
	public static Date parseDate( String value, Date def ) {
		if( isEmpty(value)) return def;
		
		/* normalize the date format */
		value = value.trim();
		value = value.replace('-', '/');

		
		String format=null;
		try {
			if( value.contains("/") && value.contains(" ") ) {
				/* we expected a date and time value */
				format = "d/M/yyyy HH:mm";
			}
			else if( value.contains("/") ) {
				/* only a date */
				format = "d/M/yyyy";
			}
			else if( value.contains(":") ) {
				/* only time */
				format = "HH:mm";
			}
			else {
				throw new ParseException("Unknown date format", -1);
			} 
			return new SimpleDateFormat(format).parse(value);
		}
		catch( ParseException e ) {
			Logger.warn("Specified date value: %s does not match expected format: %s", value, format);
			return def;
		}
	}
	
	public static Date parseDate( String value ) {
		return parseDate(value,null);
	}
	
	public static Calendar parseCalendar( String value ) {
		return parseCalendar(value,null);
	}
	
	public static Calendar parseCalendar( String value, Calendar def ) {
		Date date = parseDate(value, def != null ? def.getTime() : null);
		Calendar result = new GregorianCalendar();
		result.setTime(date);
		return result; 
	}
	
	public static Integer parseInteger( String value, Integer def ) {
		if( isEmpty(value)) return def;

		try {
			return Integer.parseInt(value);
		}
		catch( NumberFormatException e ) {
			Logger.warn("Specified value: '%s' is not a valid integer number", value);
			return def;
		}
	}
	
	public static Integer parseInteger( String value ) {
		return parseInteger(value,0);
	}
	
	public static Double parseDouble( String value, Double def ) {
		if( isEmpty(value)) return def;

		try {
			return Double.parseDouble(value);
		}
		catch( NumberFormatException e ) {
			Logger.warn("Specified value: '%s' is not a valid double number", value);
			return def;
		}
	}
	
	public static Double parseDouble( String value ) {
		return parseDouble(value,0D);
	}
	
	public static Float parseFloat( String value, Float def ) {
		if( isEmpty(value)) return def;

		try {
			return Float.parseFloat(value);
		}
		catch( NumberFormatException e ) {
			Logger.warn("Specified value: '%s' is not a valid float number", value);
			return def;
		}
	}
	
	public static Float parseFloat( String value ) {
		return parseFloat(value,0F);
	}
	
	public static Long parseLong( String value, Long def ) {
		if( isEmpty(value)) return def;

		try {
			return Long.parseLong(value);
		}
		catch( NumberFormatException e ) {
			Logger.warn("Specified value: '%s' is not a valid long number", value);
			return def;
		}
	}
	
	public static Long parseLong( String value ) {
		return parseLong(value,0L);
	}

	public static Short parseShort( String value, Short def ) {
		if( isEmpty(value)) return def;

		try {
			return Short.parseShort(value);
		}
		catch( NumberFormatException e ) {
			Logger.warn("Specified value: '%s' is not a valid short number", value);
			return def;
		}
	}
	
	public static Short parseShort( String value ) {
		return parseShort(value, Short.valueOf("0"));
	}

	public static BigDecimal parseBigDecimal( String value ) {
		return parseBigDecimal(value, new BigDecimal("0") );
	}

	public static BigDecimal parseBigDecimal( String value, BigDecimal def ) {
		if( isEmpty(value)) return def;

		try {
			return new BigDecimal(value);
		}
		catch( NumberFormatException e ) {
			Logger.warn("Specified value: '%s' is not a valid long number", value);
			return def;
		}
	}
	
	
	public static Boolean parseBool( String value, Boolean def ) {
		if( isEmpty(value)) return def;

		if( "true".equalsIgnoreCase(value) ) {
			return true;
		}
		else if( "false" .equalsIgnoreCase(value) ) {
			return false;
		}
		else {
			Logger.warn("Specified value: '%s' is not a valid boolean value", value);
			return def;
		}
	}
	
	public static Boolean parseBool( String value ) {
		return parseBool(value, false);
	}
	
	/**
	 * Contact the <code>value</code> string to the <code>source</code> string separeted by the <code>separator</code>
	 * <p> 
	 * The method is smart enouth to remove leading and trailing blanks and to avoid unnecessary separator char duplication
	 *  
	 */
	public static String append( final String source, final String value, final String separator ) {
		String result;
		if( source == null || "".equals((result=source.trim())) ) { 
			result = value;
		}
		else { 
			if( !result.endsWith(separator) ) result += separator;
			result += " " + value;
		}		
		return result;
	}

	
	public static String[] attributes( final Class<?> clazz ) { 
		Method[] methods = clazz.getMethods();
		List<String> list = new ArrayList<String>(methods.length);
		
		for( Method m : methods ) { 
			if( m.getName().startsWith("get") ) { 
				list.add( firstCharDown(m.getName().substring(3)) );
			}
			else if( m.getName().startsWith("is") && m.getReturnType().isAssignableFrom(Boolean.class) ) { 
				list.add( firstCharDown(m.getName().substring(2)) );
			}
		}
		
		String[] result = new String[list.size()];
		return list.toArray(result);
	}

	public static String dump( Object obj ) { 
		Class<?> clazz = obj.getClass();
		return dump( obj, attributes(clazz) );
	}
	
	public static String dump( Object obj, String... fields ) { 
		if( obj == null ) { return "null"; } 
		
		Class<?> clazz = obj.getClass();
		String name = clazz.getSimpleName();
		
		StringBuilder result = new StringBuilder(name);
		result.append("[");
		for( int i=0; fields!=null && i<fields.length; i++ ) { 
			if( i>0 ) { 
				result.append(", ");
			}
			try {
				Object value = getField( obj, fields[i] );
				if( value instanceof Number ) { 
					result
						.append( fields[i] )
						.append( "=")
						.append( value );
				}
				else if( value != null ) { 
					result
						.append( fields[i] )
						.append( "='")
						.append( value )
						.append( "'");
				}
				else { 
					result
						.append( fields[i] )
						.append( "=null");
				}
			} catch (Exception e) {
				Logger.warn(e.getMessage());
			}
		}
		result.append("]");
		return result.toString();		
	}
	
	/**
	 * Returns the attribute value of an object using reflection. 
	 * 
	 * @param object The object instance on which get the value
	 * @param fieldName The attribute name. 
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	private static Object getFieldValue( final Object object, final String fieldName, final boolean propertyAccess  ) throws Exception { 
		String attributeName;
		// look if it is a simple attribute name or a complex one (component.attribute)
		int i = fieldName.indexOf('.');
		attributeName = ( i==-1 )
			 		  ? fieldName
			 		  : fieldName.substring(0,i);
		Class<?> clazz = object.getClass();
		Object value;
		// get the attribute value
		try { 
			value = propertyAccess ? getPropertyValue(clazz, object, attributeName) : getAttributeValue(clazz, object, attributeName);
		} 
		catch( Exception e ) { 
			Logger.warn(e.getMessage());
			value = null;
		}
		if( i != -1 ) { 
			return getFieldValue( value, fieldName.substring(i+1), propertyAccess );
		}
		else { 
			return value;
		}
	}
	
	@Deprecated
	private static Object getAttributeValue( final Class<?> clazz, final Object object, final String attributeName ) { 
		try { 
			Field f = clazz.getDeclaredField(attributeName);
			f.setAccessible(true);
			return  f.get(object);		
		} 
		catch( Exception e ) { 
			throw new RuntimeException("Unable to access attribute '"+attributeName+"' on class '"+clazz.getSimpleName()+"' ", e);
		}
		
	}
	
	@Deprecated
	private static Object getPropertyValue( final Class<?> clazz, final Object object, final String attributeName )  {
		try { 
			String mname = getGetterMethodName(attributeName);
			Method m = clazz.getDeclaredMethod(mname, new Class[] {} );
			return m.invoke(object, new Object[] {} );
		}
		catch( Exception e ) { 
			throw new RuntimeException("Unable to access getter 'get"+Utils.firstCharUp(attributeName)+"' on class '"+clazz.getSimpleName()+"' ", e);
		}
	}

	public static <T>  T getProperty( final Object obj, final String fieldName) {
		return getProperty(obj,fieldName,(T)null);
	}
	
	static String getGetterMethodName( String fieldName ) {
		Check.notNull(fieldName);
		return "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
	}

	static String getSetterMethodName( String fieldName ) {
		Check.notNull(fieldName);
		return "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
	}
	
	static Method getMethod(Class clazz, String name, Class... parameterTypes) throws SecurityException, NoSuchMethodException 
	{
		try {
			return clazz.getMethod(name, parameterTypes);
		}
		catch( NoSuchMethodException e ) {
			return clazz.getDeclaredMethod(name, parameterTypes);
		}
	}
	
	static Field getField(Class clazz, String name) throws SecurityException, NoSuchFieldException {
		try {
			return clazz.getField(name);
		}
		catch( NoSuchFieldException e ) {
			return clazz.getDeclaredField(name);
		}
	}
	
	public static <T>  T getProperty( final Object obj, final String fieldName, T defValue ) {
		Check.notNull(obj, "Argument 'obj' cannot be null");
		Check.notEmpty(fieldName, "Argument 'field' cannot be null");

		T value;
		Class clazz = obj.getClass();

		try {
			/* 1. check if exists a getter method */
			String mname = getGetterMethodName(fieldName);
			Method method;
			try {
				method = getMethod(clazz, mname, new Class[] {} );
			} catch (NoSuchMethodException e) {
				method = null;
			}
			
			if( method != null ) {
				value = (T) method.invoke(obj, new Object[]{});
			}
			/* fallback on object field */
			else {
				Field f = getField(clazz, fieldName);
				f.setAccessible(true);
				value = (T)  f.get(obj);		
			}
		}
		catch( Exception e ) {
			throw new QuickException(e, "Unable to access object property named: '%s' on class: '%s'", fieldName, clazz.getName());
		}

		return value != null ? value : defValue;
	}
	
	public static void setPropery( final Object obj, final String fieldName, final Object value ) {
		Check.notNull(obj, "Argument 'obj' cannot be null");
		Check.notEmpty(fieldName, "Argument 'field' cannot be null");

		/* the main object class */
		Class clazz = obj.getClass();

		try {
			/* get the field declaration if exists */
			Field field;
			try {
				field = getField(clazz,fieldName);
			} catch( NoSuchFieldException e ) {
				field = null;
			}
			
			Class param = value != null ? value.getClass() : null;
			
			if( param == null && field != null ) {
				/* try to find out the paramer type using the field declaration */
				param = field.getType();
			}

			/* the setter name */
			String mname = getSetterMethodName(fieldName);
			/* if the parameter type is know try to find out the setter */
			Method method = null;
			if( param != null ) {
				try {
					method = getMethod(clazz, mname, new Class[] {param} );
				} catch( NoSuchMethodException e ) { /* nothing to do */ }
			}
			/* otherwise try to find the one-and-only-one setter with that name and one single parameter */
			else {
				int c=0;
				for( Method current : clazz.getMethods() ) {
					Class[] p;
					if( current.getName().equals(mname) && (p=current.getParameterTypes())!=null && p.length==1) {
						c++;
						method = current;
					}
				}
				
				if( c > 1 ) {
					throw new QuickException("Ambiguity on object property setter named: '%s'", mname); 
				}
			}
			if( method != null ) {
				method.invoke(obj, new Object[]{value});
			}

			else if( field != null ){
				field.setAccessible(true);
				field.set(obj, value);
			}
		}
		catch( Exception e ) {
			throw new QuickException(e, "Unable to set object property named: '%s' on class: '%s'", fieldName, clazz.getName());
		}

	}

	public static <T>  T getField( final Object obj, final String fieldName) {
		return getField(obj,fieldName,(T)null);
	}
		
	public static <T>  T getField( final Object obj, final String fieldName, T defValue ) {
		Check.notNull(obj, "Argument 'obj' cannot be null");
		Check.notEmpty(fieldName, "Argument 'field' cannot be null");

		T value;
		Class clazz = obj.getClass();

		try {
			Field f = getField(clazz, fieldName);
			f.setAccessible(true);
			value = (T)  f.get(obj);		
		}
		catch( Exception e ) {
			throw new QuickException(e, "Unable to access object field: '%s' on class: '%s'", fieldName, clazz.getName());
		}

		return value != null ? value : defValue;
	}
	
	public static void setField( final Object obj, final String fieldName, Object value ) {
		Check.notNull(obj, "Argument 'obj' cannot be null");
		Check.notEmpty(fieldName, "Argument 'field' cannot be null");

		/* the main object class */
		Class clazz = obj.getClass();

		try {
			/* get the field declaration if exists */
			Field field = getField(clazz, fieldName);
			field.setAccessible(true);
			field.set(obj, value);
			
		}
		catch( Exception e ) {
			throw new QuickException(e, "Unable to set object property named: '%s' on class: '%s'", fieldName, clazz.getName());
		}

	}	
	

	public static int hash() { 
		return 7;
	}
	
	public static int hash( int hash, int value ) { 
		return 31 * hash + value; 
	}

	public static int hash( int hash, long value ) { 
		return 31 * hash + ((int)(value ^ (value >>> 32))); 
	}
	
	public static int hash( int hash, Object object ) { 
		return 31 * hash + ( object != null ? object.hashCode() : 0); 
	}
	
	public static int hash( int hash, boolean value ) { 
		return 31 * hash + (value ? 1231 : 1237); // <-- guru's code, look at Boolean#hashCode()
	}
	
	public static <T> int hash( int hash, T[] array ) {
		for( int i=0; i<array.length; i++ ) {
			hash = hash(hash, array[i]);
		}
		return hash;
	}
	
	public static <T> int hash( int hash, Collection<T> coll ) {
		for( T item : coll ) {
			hash = hash(hash,item);
		}
		return hash;
	}
	
	public static int hash( Object obj, String ... properties ) {
		int hash = hash();
		if( properties == null ) {
			return hash(hash,obj);
		}
		
		for( String p : properties ) {
			hash = hash(hash,getProperty(obj, p));
		}
		
		return hash;
	}
	
	public static boolean isEquals( Object thisObject, Object thatObject ) { 
		  return (thisObject == thatObject || (thisObject != null && thisObject.equals(thatObject)));
	}
	
	public static boolean isEquals( Object t1, Object t2, String ... properties ) {
		if( t1==null || t2==null || properties == null ) {
			return isEquals(t1,t2);
		}
		
		//TODO make it smart enough to handle properly arrays, collections, maps, enums?
		for( String p : properties ) {
			if( !isEquals(getProperty(t1, p), getProperty(t2, p)) ) {
				return false;
			}
		}
		
		return true;
	}
	
	public static <T> boolean isEquals( T[] a1, T[] a2 ) {
		if( a1==a2 ) { return true; }
		if( a1==null || a2==null ) { return false; }
		if( a1.length != a2.length ) { return false; }
		
		boolean equals = true;
		for( int i=0, n=a1.length; i<n && equals; i++ ) {
			equals = equals && Utils.isEquals(a1[i], a2[i]);
		}
		
		return equals;
	}

	public static <T> boolean isEquals( Collection<T> c1, Collection<T> c2 ) {
		if( c1==c2 ) { return true; }
		if( c1==null || c2==null ) { return false; }
		
		boolean equals = true;
		Iterator<T> i1 = c1.iterator();
		Iterator<T> i2 = c2.iterator();
		while( i1.hasNext() && i2.hasNext() && equals) {
			equals = Utils.isEquals(i1.next(), i2.next());
		}
		
		return equals && !i1.hasNext() && !i2.hasNext();
	}
	
	
	/**
	 * Helper method to check for object class equality. Null and hibernate proxyed obeject are managed properly  
	 *  
	 * @param thisObject the first object to check.
	 * @param thatObject the second object ot compare to.
	 * @return <code>true</code> when objects are the same class or false otherwise 
	 */
	public static boolean isEqualsClass( Object thisObject, Object thatObject ) { 
		if( thisObject == thatObject ) return true;
		if( thisObject != null && thatObject == null ) return false;

		return thisObject.getClass().equals(thatObject.getClass());
	}
	
	public static String upper( String string ) { 
		return string != null ? string.toUpperCase() : null;
	}
	
	public static String lower( String string ) { 
		return string != null ? string.toLowerCase() : null;
	}
	
	public static String firstCharUp( String str ) { 
		if( str == null ) { 
			return null;
		}
		
		if( str.length() < 2 ) { 
			return str.toUpperCase();
		}
		
		return str.substring(0,1).toUpperCase() + str.substring(1);
	}
	
	public static String firstCharDown( String str ) { 
		if( str == null ) { 
			return null;
		}
		
		if( str.length() < 2 ) { 
			return str.toLowerCase();
		}
		
		return str.substring(0,1).toLowerCase() + str.substring(1);
	}
	
	public static File appendPath( String parent, String path ) { 

		// remove leading slash
		if( path.startsWith("\\") || path.startsWith("/")) { 
			path = path.substring(1);
		}
		// remove traling slash
		if( path.endsWith("\\") || path.endsWith("/")) { 
			path = path.substring(0,path.length()-1);
		}
		

		if( !parent.endsWith("\\") && !parent.endsWith("/") ) { 
			parent += File.separatorChar;
		}
		
		File result = new File( parent + path );

		try {
			return result.getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException("Unable to get canonical path for \"" + result + "\"");
		}
	}
	
	public static File appendPath( File parent, String path ) { 
		return appendPath(parent.getPath(), path);
	}
	
	public static Object clear(Object obj) { 
		Field[] fields = obj.getClass().getFields();
		for( Field field : fields ) {
			boolean isFinal = (field.getModifiers() & Modifier.FINAL) != 0;
			boolean isStatic = (field.getModifiers() & Modifier.STATIC) != 0;
			if( !isFinal && !isStatic ) { 
				field.setAccessible(true);
				try {
					field.set(obj, null);
				} catch (Exception e) {
					throw new RuntimeException("Unable to set field: \"" + obj.getClass().getName() +  "#" + field.getName() + "()", e );
				}
			}
		}
		
		return obj;
	}
	
	
   /**
    * Get the underlying class for a type, or null if the type is a variable type.
    * @param type the type
    * @return the underlying class
    */
	public static Class<?> getClass(Type type) 
	{
	    if (type instanceof Class) {
	      return (Class<?>) type;
	    }
	    else if (type instanceof ParameterizedType) {
	      return getClass(((ParameterizedType) type).getRawType());
	    }
	    else if (type instanceof GenericArrayType) {
	      Type componentType = ((GenericArrayType) type).getGenericComponentType();
	      Class<?> componentClass = getClass(componentType);
	      if (componentClass != null ) {
	        return Array.newInstance(componentClass, 0).getClass();
	      }
	      else {
	        return null;
	      }
	    }
	    else {
	      return null;
	    }
	} 
	  
	/**
	 * Get the actual type arguments a child class has used to extend a generic base class.
	 *
	 * @param baseClass the base class
	 * @param childClass the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	 
	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) 
	{
	    Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
	    Type type = childClass;
	    // start walking up the inheritance hierarchy until we hit baseClass
	    while (! getClass(type).equals(baseClass)) {
	      if (type instanceof Class) {
	        // there is no useful information for us in raw types, so just keep going.
	        type = ((Class) type).getGenericSuperclass();
	      }
	      else {
	        ParameterizedType parameterizedType = (ParameterizedType) type;
	        Class<?> rawType = (Class) parameterizedType.getRawType();
	  
	        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
	        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
	        for (int i = 0; i < actualTypeArguments.length; i++) {
	          resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
	        }
	  
	        if (!rawType.equals(baseClass)) {
	          type = rawType.getGenericSuperclass();
	        }
	      }
	    }
	  
	    // finally, for each actual type argument provided to baseClass, determine (if possible)
	    // the raw class for that type argument.
	    Type[] actualTypeArguments;
	    if (type instanceof Class) {
	      actualTypeArguments = ((Class) type).getTypeParameters();
	    }
	    else {
	      actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
	    }
	    List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
	    // resolve types by chasing down type variables.
	    for (Type baseType: actualTypeArguments) {
	      while (resolvedTypes.containsKey(baseType)) {
	        baseType = resolvedTypes.get(baseType);
	      }
	      typeArgumentsAsClasses.add(getClass(baseType));
	    }
	    return typeArgumentsAsClasses;
	  } 	
	
	
	public static String rpad( Object value, int n, char ch ) { 
		String str = value != null ? value.toString() : "";
		if( str.length() >= n ) { 
			// nothing to do, just return
			return str;
		}
		
		char[] pad = new char[ n-str.length() ];
		Arrays.fill(pad, ch);

		return new String(pad) + str;
	}

	public static String pad( Object value, int n, char ch ) { 
		String str = value != null ? value.toString() : "";
		if( str.length() >= n ) { 
			// nothing to do, just return
			return str;
		}
		
		char[] pad = new char[ n-str.length() ];
		Arrays.fill(pad, ch);

		return str + new String(pad);
		
	}
	
	public static String read(File file) {
		Check.notNull(file, "File argument cannot be null");
		String name = null;
		try {
			name = file.getAbsolutePath();
			return read(new FileReader(file));
		} 
		catch (Exception e) {
			throw new QuickException(e, "Unable to read file: '%s'", name);
		}
	}
	
	public static void write( CharSequence value, File file ) {
		try {
			FileUtils.writeStringToFile(file, value.toString());
		} catch (IOException e) {
			throw new QuickException(e, "Unable to save file named: %s", file);
		}
	}
	
	/**
	 * Reads a readers retuning the content as a {@link String} object. 
	 * 
	 * The source reader is always closed, also if an exception raises.
	 * 
	 * @param stream The content source.
	 * @return The resulting string context. 
	 * 
	 * @throws IOException
	 */
	public static String read( InputStream stream )  { 
		return read( new InputStreamReader(stream) );
	}
	
	/**
	 * Reads a readers retuning the content as a {@link String} object. 
	 * 
	 * The source reader is always closed, also if an exception raises.
	 * 
	 * @param reader The content source
	 * @return the resulting content string 
	 * 
	 * @throws IOException
	 */
	public static String read( Reader reader ) { 
		StringBuilder result = new StringBuilder();

		BufferedReader in = new BufferedReader(reader);
		try { 
			String line;
			while( (line=in.readLine()) != null ) 
				result.append(line).append("\n");
			
			return result.toString();
			
		} 
		catch( IOException e ) {
			throw new QuickException(e);
		}
		finally { 
			if( in != null ) { 
				try { in.close(); } catch(IOException e ) { Logger.warn("Error on closing stream"); }
			}
		}
	}

	/**
	 * Reads an input stream and returns it as a byte[] 
	 * <p>
	 * Note: this method will close the source input stream at the end of reading 
	 * 
	 * @param inputStream the source stream.
	 * @return the converted byte array
	 * 
	 * @throws IOException
	 */
	public static byte[] readAsByteArray( InputStream inputStream ) throws IOException { 

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedInputStream in = new BufferedInputStream(inputStream);
		try { 
			byte[] buffer = new byte[1024];

			int c;
			while( (c=in.read(buffer))>0 ) { 
				out.write(buffer, 0, c);
			}
			
			return out.toByteArray();
			
		} finally { 
			if( in != null ) { 
				in.close();
			}
			
			if( out != null ) { 
				out.close();
			}
		}
	}	
	
	/**
	 * Remove all blank spaces from parameter String
	 * @param source
	 * @return
	 */
	public static String removeBlankSpaces(String source){
		return source != null ? source.replaceAll(" ", "")  : null;
	}
	
	public static void append( Appendable builder, CharSequence value) {
		append(builder,value,null);
	}

	public static void append( Appendable builder, CharSequence value, CharSequence separator ) { 
		if( Utils.isNotEmpty(value) ) {
			try { 
				if( Utils.isNotEmpty(separator)) { 
					builder.append(separator);
				}
				builder.append(value);
			}
			catch( IOException e ) { 
				throw new RuntimeException(String.format("Failing appending the value '%s'", value), e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T cast( Object obj ) { 
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T castIfEquals( T thisObj, Object thatObj ) { 
		boolean equals = isEqualsClass(thisObj, thatObj);
		return equals ? (T)thatObj : null;
	}

	/**
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved chars to their
	 * original forms.
	 * 
	 * @param escapedUnicodeString
	 *            escaped unicode string, like '\u4F60\u597D'.
	 * 
	 * @return The actual unicode. Can be used for instance with message bundles
	 */
	public static String fromEscapedUnicode(String escapedUnicodeString)
	{
		if( isEmpty(escapedUnicodeString) ) { 
			return escapedUnicodeString;
		}
		
		int off = 0;
		char[] in = escapedUnicodeString.toCharArray();
		int len = in.length;
		char[] convtBuf = new char[len];

		if (convtBuf.length < len)
		{
			int newLen = len * 2;
			if (newLen < 0)
			{
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;

		while (off < end)
		{
			aChar = in[off++];
			if (aChar == '\\')
			{
				aChar = in[off++];
				if (aChar == 'u')
				{
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++)
					{
						aChar = in[off++];
						switch (aChar)
						{
							case '0' :
							case '1' :
							case '2' :
							case '3' :
							case '4' :
							case '5' :
							case '6' :
							case '7' :
							case '8' :
							case '9' :
								value = (value << 4) + aChar - '0';
								break;
							case 'a' :
							case 'b' :
							case 'c' :
							case 'd' :
							case 'e' :
							case 'f' :
								value = (value << 4) + 10 + aChar - 'a';
								break;
							case 'A' :
							case 'B' :
							case 'C' :
							case 'D' :
							case 'E' :
							case 'F' :
								value = (value << 4) + 10 + aChar - 'A';
								break;
							default :
								throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char)value;
				}
				else
				{
					if (aChar == 't')
					{
						aChar = '\t';
					}
					else if (aChar == 'r')
					{
						aChar = '\r';
					}
					else if (aChar == 'n')
					{
						aChar = '\n';
					}
					else if (aChar == 'f')
					{
						aChar = '\f';
					}
					out[outLen++] = aChar;
				}
			}
			else
			{
				out[outLen++] = aChar;
			}
		}
		return new String(out, 0, outLen);
	}	
	
	public static String toEscapedUnicode(String value, int len) { 
		String result = toEscapedUnicode(value);
		if( result != null && result.length() > len ) { 
			Logger.warn(String.format("Escaped string exceeds maximum field length (%s). Orginal value: \"%s\"", len, value));
			result = result.substring(0, len);
		}
		return result;
	}
	
	public static String toEscapedUnicode(String unicodeString)
	{
		if ((unicodeString == null) || (unicodeString.length() == 0))
		{
			return unicodeString;
		}
		int len = unicodeString.length();
		int bufLen = len * 2;
		StringBuffer outBuffer = new StringBuffer(bufLen);
		for (int x = 0; x < len; x++)
		{
			char aChar = unicodeString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127))
			{
				if (aChar == '\\')
				{
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar)
			{
				case ' ' :
					if (x == 0)
					{
						outBuffer.append('\\');
					}
					outBuffer.append(' ');
					break;
				case '\t' :
					outBuffer.append('\\');
					outBuffer.append('t');
					break;
				case '\n' :
					outBuffer.append('\\');
					outBuffer.append('n');
					break;
				case '\r' :
					outBuffer.append('\\');
					outBuffer.append('r');
					break;
				case '\f' :
					outBuffer.append('\\');
					outBuffer.append('f');
					break;
				case '=' : // Fall through
				case ':' : // Fall through
				case '#' : // Fall through
				case '!' :
					outBuffer.append('\\');
					outBuffer.append(aChar);
					break;
				default :
					if ((aChar < 0x0020) || (aChar > 0x007e))
					{
						outBuffer.append('\\');
						outBuffer.append('u');
						outBuffer.append(toHex((aChar >> 12) & 0xF));
						outBuffer.append(toHex((aChar >> 8) & 0xF));
						outBuffer.append(toHex((aChar >> 4) & 0xF));
						outBuffer.append(toHex(aChar & 0xF));
					}
					else
					{
						outBuffer.append(aChar);
					}
			}
		}
		return outBuffer.toString();
	}
	
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
		'B', 'C', 'D', 'E', 'F' };

	private static char toHex(int nibble)
	{
		return hexDigit[(nibble & 0xF)];
	}
	
	/**
	 * Null safe object compare method. 
	 * 
	 * @param <T>
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static <T extends Comparable<T>> int compare( T o1, T o2 ) { 
		return o1==o2 ? 0 : ( o1 != null ? ( o2 != null ? o1.compareTo(o2) : 1 ) : -1 ); 
	}
	
	public static boolean isExceptionRaisedBy( Throwable e, Class<? extends Throwable> clazz ) { 
		while( e != null ) { 
			if( clazz.isInstance(e) ) { 
				return true;
			}
			e = e.getCause();
		}
		return false;
	}
	

	
	/**
	 * This method returns a String composed by each character of input String <i>name</i> in 
	 * Equivalent Class Regular Expression form (e.g. input name = 'franco' --> returned name = '[[=f=]][[=r=]][[=a=]][[=n=]][[=c=]][[=o=]]')
	 * In this way users can generalise strings with special characters (e.g. HERN�NDEZ, ROMA�A, etc.)
	 *   
	 * @param value The {@link Staff} last name
	 * @return the last name in Equivalent Class Regular Expression form 
	 */
	public static String toRegExpEquivalentClass(String value) {
		if( value == null ) { 
			return null;
		}
		
		StringBuilder result = new StringBuilder("^"); // <-- note: the start of the string
		for( int i=0, c=value.length(); i<c; i++ ) {
			result.append("[[=") .append(value.charAt(i)) .append("=]]");
		}
		return result.toString();
	}
	
	/**
	 * A shortcut method to retrieve the canonical path avoiding boring exception handling 
	 */
	public static String getCanonicalPath(File file) {
		try {
			return file != null ? file.getCanonicalPath() : null;
		} catch (IOException e) {
			Logger.error("Unable to retrieve canonical path for file: '%s'", file.toString());
			return null;
		}
	}

	

    static public boolean deleteFolder(File path) {
    	boolean result = true;
		if( path.exists() ) {
		      File[] files = path.listFiles();
		      for(int i=0; i<files.length; i++) {
		         if(files[i].isDirectory()) {
		           result = result && deleteFolder(files[i]);
		         }
		         else {
		           result = result && files[i].delete();
		           if(!result) {
		        	   Logger.warn("Cannot delete file: %s", files[i]);
		           }
		         }
		      }
		 }
		
		result = result && path.delete();
		if(!result) {
     	   Logger.warn("Cannot delete folder: %s", path);
		}
		return result;
	}	
    
	
	
	public static String[] asStringArray(Map<String,String> map) {
		if( map == null) { 
			return null;
		}
		
		String[] result = new String[map.size()];
		int i=0;
		for( Map.Entry<String, String> item : map.entrySet()) {
			result[i++] = String.format("%s=%s", item.getKey(), item.getValue());
		}
		
		return result;
	}
	
	public static Map<String,String> asStringMap(String...values) {
		if( values == null ) return null;
		
		Map<String,String> result = new HashMap<String, String>();
		for( String pair : values ) {
			if( pair == null ) { continue; }
			
			String[] items = pair.split("=");
			result.put(items[0], items.length>1 ? items[1] : null );
		}
	
		return result;
	}
	
	/**
	 * Discover all classes in the specified package. 
	 * <p>
	 * See http://snippets.dzone.com/posts/show/4831
	 * 
	 * @param packageName
	 * @return
	 */
    public static Class[] getClasses(String packageName) {
    	Check.notNull(packageName);
    	
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources;
		try {
			resources = Play.classloader.getResources(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
		    URL resource = resources.nextElement();
			Logger.debug("url: %s", resource);
		    dirs.add(new File(resource.getFile()));
		}
		
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
		    try {
				classes.addAll(findClasses(directory, packageName));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return classes.toArray(new Class[classes.size()]);
	}
	
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        
        // Well play in development mode compile classes on-memory, so .class files does not exists 
        //TODO to be improved
        File[] files = directory.listFiles();
        final String SUFFIX = ".java";
        
        for (File file : files) {
            if (file.isDirectory()) {
            	if( !file.getName().contains(".") ) {
            		classes.addAll(findClasses(file, packageName + "." + file.getName()));
            	}
            } 
            else if (file.getName().endsWith(SUFFIX)) {
            	String clazzName = packageName + '.' + file.getName().substring(0, file.getName().length() - SUFFIX.length());
                classes.add(Class.forName(clazzName));
            }
        }
        return classes;
    }

    
    public static <T> List<T> copy(Collection<T> list) {
    	if( list == null ) {
    		return null;
    	}
    	
    	List<T> result = new ArrayList<T>();
    	for( T item : list ) {
			result.add(copy(item));
    	}
    	return result;
    } 
    
    public static <T> T[] copy(T[] array) {
    	if( array == null ) {
    		return null;
    	}
    	
    	T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length);
    	for( int i=0, c=array.length; i<c; i++ ) {
			result[i] = copy(array[i]);
    	}
    	return result;
    	
    }

    public static <T> T create(Class<T> clazz) {
    	return create(clazz,(Object[])null);
    }
    
    public static <T> T create(Class<T> clazz, Object... args) {
		Check.notNull("Argument clazz cannot be null");
    	
    	try {
    		/*
    		 * quick create if now args are required
    		 */
    		if( args == null || args.length==0 ) {
    			return clazz.newInstance();
    		}
    		
    		/*
    		 * otherwise it is required to get a specified constructor 
    		 */
    		Class[] types = new Class[args.length];
    		for( int i=0; i<args.length; i++ ) {
    			types[i] = args[i].getClass(); 
    		}
    		Constructor<?> constructor = clazz.getConstructor(types);
    		return (T)constructor.newInstance(args);
    		
		} catch (Exception e) {
			throw new QuickException(e, "Unable to instantiate an instance of class: %s", clazz.getName());
		}
    }

    
    
    public static Map copy( Map<?,?> map ) {
    	if( map == null ) {
    		return null;
    	}
    	
    	Map result = create(map.getClass());
		
    	for( Map.Entry entry : map.entrySet() ) {
    		result.put(entry.getKey(), entry.getValue());
    	}
    	
    	return result;
    } 
    
    public static <T> T copy( T item ) {
    	if( item == null ) {
    		return null;
    	}
    	
		Class clazz = item.getClass();
		Constructor<?> c;
		try {
			c = clazz.getConstructor(clazz);
			return (T) c.newInstance(item);
		} 
		catch (Exception e) {
			throw new QuickException(e, "Unable to create a copy of class: '%s'. Unsure that has a copy constructor defined as \"public %s(%s item)\"", clazz.getName(), clazz.getSimpleName(), clazz.getSimpleName() );
		}
    }
    
    public static Boolean copy(Boolean value) {
    	if( value == null ) {
    		return null;
    	}

    	return Boolean.valueOf( value.booleanValue() );
    }
    
    public static Character copy(Character value) {
    	if( value == null ) {
    		return null;
    	}

    	return Character.valueOf(value.charValue());
    }

    public static Byte copy(Byte value) {
    	if( value == null ) {
    		return null;
    	}

    	return Byte.valueOf(value.byteValue());
    }    
    
    public static Integer copy(Integer value) {
    	if( value == null ) {
    		return null;
    	}

    	return Integer.valueOf(value.intValue());
    }        

    public static Long copy(Long value) {
    	if( value == null ) {
    		return null;
    	}

    	return Long.valueOf(value.longValue());
    }   

    public static Float copy(Float value) {
    	if( value == null ) {
    		return null;
    	}

    	return Float.valueOf(value.floatValue());
    }   
      
    public static Double copy(Double value) {
    	if( value == null ) {
    		return null;
    	}

    	return Double.valueOf(value.doubleValue());
    }   

 
    
    /**
	 * Null-safe {@link Date} copy helper method
	 * 
	 * @param obj the date value to copy, null is a valid value
	 * @return a cloned instance on <code>obj</code> or <code>null</code> if the parameter is <code>null</code>
	 */
	public static Date copy( Date obj ) { 
		return obj != null ? new Date(obj.getTime()) : null;
	}    
	
	/**
	 * Replace in the specified text variables following the syntax: <code>${varname}</code>
	 * 
	 * @param text a generic string value that can cointains one or more variables 
	 * @param vars a mp containing the associated value for the expected variables 
	 * @return the resulting text with all variables values replaced
	 */
	public static String replaceVars(final CharSequence text, final Map<String,?> vars) {
		return replaceVars(text, new MapValue<String,Object>() {
			public Object get(String key) {
				return vars.get(key);
			};
		} ); 
	}	
	
	public static <V> String replaceVars(final CharSequence text, final MapValue<String,V> mapper) {
		Matcher m = VAR_PATTERN.matcher(text);
		StringBuffer result = new StringBuffer();
		while (m.find()) {
			String variable = m.group(1);
			Object value = mapper.get(variable);
			m.appendReplacement(result, value != null ? value.toString() : "");
		}
		
		m.appendTail(result);
		return result.toString();
	}	

	public static String match( final String text, final String re, final MatchAction action ) { 
		return match(text, Pattern.compile(re), action);
	}
	
	public static String match( final String text, final Pattern re, final MatchAction action ) { 
		Matcher matcher = re.matcher(text);
		StringBuffer result = new StringBuffer();

		while (matcher.find()) {

			List<String> groups = new ArrayList<String>(matcher.groupCount()+1);
			for( int i=0, c=matcher.groupCount(); i<=c; i++ ) { 
				groups.add(matcher.group(i));
			}
			String value = action.replace(groups);
			
			if( value != null ) { 
				matcher.appendReplacement(result, value != null ? value : "");
			}
		}
		
		matcher.appendTail(result);
 		return result.toString();
	}

	public interface MatchAction { 
		String replace( List<String> groups );
	}
	
	/**
	 * Find out all the items in an array that match against a specified attribute value
	 * 
	 * @param <T> the array component type
	 * @param array a not <code>null</code> array of objects instances 
	 * @param property the name of the component property 
	 * @param value the value against to match 
	 * @return a list containing all the objects in the array that satisfy the match 
	 */
	public static <T> List<T> getItems(final T[] array, final String property, final String value) {
		Check.notNull(array, "Argument list cannot be null");
		Check.notEmpty(property, "Argument propertyName cannot be empty");
		
		List<T> result = new ArrayList<T>(0);
		
		for( T item : array ) {
			if( item != null && isEquals(value, getProperty(item, property)) ) {
				result.add(item);
			}
		}
		
		return result;
	}
	
	/**
	 * Find out all the items in a colelction that match against a specified attribute value
	 * 
	 * @param <T> the collection component type
	 * @param list a not <code>null</code> collection of objects instances 
	 * @param property the name of the component property 
	 * @param value the value against to match 
	 * @return a list containing all the objects in the collection that satisfy the match 
	 */
	public static <T> List<T> getItems(final Collection<T> list, final String property, final String value ) {
		T[] array = list != null ? (T[])list.toArray() : null;
		return getItems(array,property,value);
	}
	
	
	public static <T> T firstItem(final Collection<T> list, final String property, final String value ) {
		List<T> result = getItems(list,property,value);
		if( result.size() == 0 ) {
			return null;
		}
		return result.get(0);
 	}
	
	public static <T> boolean contains(T[] array, T value) {
		if( array == null ) return false;
		
		for( T item : array ) {
			if( isEquals(item, value) ) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean contains(char[] array, char value ) {
		if( array == null ) return false;
		
		for( char item : array ) {
			if( item == value ) return true;
		}
		
		return false;
	}
	
	public static boolean contains(int[] array, int value ) {
		if( array == null ) return false;
		
		for( int item : array ) {
			if( item == value ) return true;
		}
		
		return false;
	}
	
	public static boolean contains(long[] array, long value ) {
		if( array == null ) return false;
		
		for( long item : array ) {
			if( item == value ) return true;
		}
		
		return false;
	}
	
	
	public static boolean contains(float[] array, float value ) {
		if( array == null ) return false;
		
		for( float item : array ) {
			if( item == value ) return true;
		}
		
		return false;
	}
	
	public static boolean contains(double[] array, double value ) {
		if( array == null ) return false;
		
		for( double item : array ) {
			if( item == value ) return true;
		}
		
		return false;
	}
	
	public static String asTimeString( long millis ) {
		
		if( millis < 1000 ) {
			return millis + " ms";
		}
		
		double secs = millis / (double)1000;
		if( secs < 60 ) {
			return String.format("%.0f secs", secs);
		}
		
		double mins = secs / 60;
		if( mins < 60 ) {
			return String.format("%.0f mins", mins);
		}
		
		double hours = mins / 60;
		if( hours < 24 ) {
			return String.format("%.0f hours", hours);
		}
		
		double days = hours / 24;
		return String.format("%.0f days", days);
	}

	public static String camelize( String str ) {
		return camelize(str,"_.: \t\n\r\f");
	}
	
	public static String camelize( String str, String separators ) {
		
		if( isEmpty(str) ) return str;
		
		StringBuilder result = new StringBuilder ();
		StringTokenizer tokens = separators != null ? new StringTokenizer(str,separators) : new StringTokenizer(str);
		/* the first token is not touched */ 
		result.append( tokens.nextElement() );
		while( tokens.hasMoreElements()) {
			result.append( firstCharUp( tokens.nextToken() ) );
		}
		
		return result.toString();
	}

	public static File nextUniqueFile(File target) {
		Check.notNull(target, "Argument target cannot be null");
		
		int p = target.getName().indexOf(".");
		if( p == -1 ) { 
			return new File(target+".1");
		}

		String _name = target.getName().substring(0,p);
		String _ext = target.getName().substring(p+1);;
		String _parent = target.getPath();  
		p = _parent.indexOf(target.getName());
		_parent = _parent.substring(0,p);
		
		try {
			int val = Integer.parseInt(_ext);
			return new File(_parent + _name + "." + (val+1));
		} 
		catch( NumberFormatException e ) { 
			return new File(_parent + _name + "." + incSuffix(_ext) );
		}
	} 
	
	static String incSuffix( String str ) { 

		String name;
		String ext;
		
		int p = str.indexOf(".");
		if( p == -1 ) { 
			name = str;
			ext = "";
		}
		else { 
			name = str.substring(0,p);
			ext = str.substring(p);
		}


		try {
			int val = Integer.parseInt(name);
			return (val+1) +  ext ;
			
		} 
		catch( NumberFormatException e ) { 
			return "1." + name + ext;
		}

	}


	public static String cause( Throwable e ) { 
		String result = null;

		if( e == null ) { 
			return result;
		}
		
		if( e.getCause() != null ) { 
			result = e.getCause().getMessage();
		}
		if( result == null ) { 
			result = e.getMessage();
		}		
		
		return result;
	}
	
	
	/**
	*
	* PL/SQL decode like function
	*
	* Evaluate the first argument and compare it with second argument, if they are equals will return the third one,
	* otherwise compare it with the forth one and will return the fifth, and so on.
	* If all the comparisons fails will return the last one, if present, or null.
	*
	* @param args x, y0, v0, y1, v1, ...., [default]
	* @return
	*/
	public Object decode( Object... args )
	{
		Object pivot = args[0];
	
		int max = args.length-1;
	
		int i=1;
		for ( ; i< max; i+=2 )
		{
			if ( ObjectUtils.equals(pivot,args[i]) ) { 
				return args[i+1];
			}
		}
	
		// default value
		return i==max ? args[i] : null;
	}
	
}
 
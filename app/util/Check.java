package util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;


/**
 * Check utility class that assists in validating arguments.
 * Useful for identifying programmer errors early and clearly at runtime.
 *
 * <p>For example, if the contract of a public method states it does not
 * allow <code>null</code> arguments, Assert can be used to validate that
 * contract. Doing this clearly indicates a contract violation when it
 * occurs and protects the class's invariants.
 *
 * <p>Typically used to validate method arguments rather than configuration
 * properties, to check for cases that are usually programmer errors rather than
 * configuration errors. In contrast to config initialization code, there is
 * usally no point in falling back to defaults in such methods.
 *
 * <p>This class is similar to JUnit's assertion library. If an argument value is
 * deemed invalid, an {@link IllegalArgumentException} is thrown (typically).
 * For example:
 *
 * <pre >
 * Assert.notNull(clazz, "The class must not be null");
 * Assert.isTrue(i > 0, "The value must be greater than zero. Current value", i);</pre>
 *
 * @author Paolo Di Tommaso (inspired by {@link Assert) 
 * 
 *
 */
public class Check {

	/**
	 * Assert a boolean expression, throwing <code>IllegalArgumentException</code>
	 * if the test result is <code>false</code>.
	 * <pre class="code">Assert.isTrue(i &gt; 0, "The value must be greater than zero");</pre>
	 * @param expression a boolean expression
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if expression is <code>false</code>
	 */
	public static void isTrue(boolean expression, String message, Object ... messageParams) {
		if (!expression) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert a boolean expression, throwing <code>IllegalArgumentException</code>
	 * if the test result is <code>false</code>.
	 * <pre class="code">Assert.isTrue(i &gt; 0);</pre>
	 * @param expression a boolean expression
	 * @throws IllegalArgumentException if expression is <code>false</code>
	 */
	public static void isTrue(boolean expression) {
		isTrue(expression, "[Check failed] - this expression must be true");
	}

	public static void isFalse(boolean expression, String message, Object ... messageParams) {
		if (expression) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}	

	public static void isFalse(boolean expression) {
		isFalse(expression, "[Check failed] - this expression must be false");
	}	
	
	/**
	 * Assert that an object is <code>null</code> .
	 * <pre class="code">Assert.isNull(value, "The value must be null");</pre>
	 * @param object the object to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object is not <code>null</code>
	 */
	public static void isNull(Object object, String message, Object ... messageParams) {
		if (object != null) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert that an object is <code>null</code> .
	 * <pre class="code">Assert.isNull(value);</pre>
	 * @param object the object to check
	 * @throws IllegalArgumentException if the object is not <code>null</code>
	 */
	public static void isNull(Object object) {
		isNull(object, "[Check failed] - the object argument must be null");
	}

	/**
	 * Assert that an object is not <code>null</code> .
	 * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
	 * @param object the object to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object is <code>null</code>
	 */
	public static void notNull(Object object, String message, Object ... messageParams) {
		if (object == null) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert that an object is not <code>null</code> .
	 * <pre class="code">Assert.notNull(clazz);</pre>
	 * @param object the object to check
	 * @throws IllegalArgumentException if the object is <code>null</code>
	 */
	public static void notNull(Object object) {
		notNull(object, "[Check failed] - this argument is required; it must not be null");
	}

	/**
	 * Assert that the given String is not empty; that is,
	 * it must not be <code>null</code> and not the empty String.
	 * <pre class="code">Assert.hasLength(name, "Name must not be empty");</pre>
	 * @param text the String to check
	 * @param message the exception message to use if the assertion fails
	 * @see StringUtils#hasLength
	 */
	public static void hasLength(String text, String message, Object ... messageParams) {
		if ( text==null || text.length()==0 ) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert that the given String is not empty; that is,
	 * it must not be <code>null</code> and not the empty String.
	 * <pre class="code">Assert.hasLength(name);</pre>
	 * @param text the String to check
	 * @see StringUtils#hasLength
	 */
	public static void hasLength(String text) {
		hasLength(text,
				"[Check failed] - this String argument must have length; it must not be null or empty");
	}

	/**
	 * Assert that the given String has valid text content; that is, it must not
	 * be <code>null</code> and must contain at least one non-whitespace character.
	 * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
	 * @param text the String to check
	 * @param message the exception message to use if the assertion fails
	 * @see StringUtils#hasText
	 */
	public static void hasText(String text, String message, Object ... messageParams) {
		if ( text==null || text.trim().length()==0 ) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert that the given String has valid text content; that is, it must not
	 * be <code>null</code> and must contain at least one non-whitespace character.
	 * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
	 * @param text the String to check
	 * @see StringUtils#hasText
	 */
	public static void hasText(String text) {
		hasText(text,
				"[Check failed] - this String argument must have text; it must not be null, empty, or blank");
	}

	/**
	 * Assert that the given text does not contain the given substring.
	 * <pre class="code">Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");</pre>
	 * @param textToSearch the text to search
	 * @param substring the substring to find within the text
	 * @param message the exception message to use if the assertion fails
	 */
	public static void doesNotContain(String textToSearch, String substring, String message, Object ... messageParams) {
		hasLength(textToSearch);
		hasLength(substring);
		if ( textToSearch.indexOf(substring) != -1) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert that the given text does not contain the given substring.
	 * <pre class="code">Assert.doesNotContain(name, "rod");</pre>
	 * @param textToSearch the text to search
	 * @param substring the substring to find within the text
	 */
	public static void doesNotContain(String textToSearch, String substring) {
		doesNotContain(textToSearch, substring,
				"[Check failed] - this String argument must not contain the substring [" + substring + "]");
	}


	/**
	 * Assert that an array has elements; that is, it must not be
	 * <code>null</code> and must have at least one element.
	 * <pre class="code">Assert.notEmpty(array, "The array must have elements");</pre>
	 * @param array the array to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object array is <code>null</code> or has no elements
	 */
	public static void notEmpty(Object[] array, String message, Object ... messageParams) {
		if (array==null || array.length==0) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert that an array has elements; that is, it must not be
	 * <code>null</code> and must have at least one element.
	 * <pre class="code">Assert.notEmpty(array);</pre>
	 * @param array the array to check
	 * @throws IllegalArgumentException if the object array is <code>null</code> or has no elements
	 */
	public static void notEmpty(Object[] array) {
		notEmpty(array, "[Check failed] - this array must not be empty: it must contain at least 1 element");
	}

	public static void notEmpty( CharSequence value ) { 
		notEmpty(value, "[Check failed] - argument must not be empty");
	}
	
	public static void notEmpty( CharSequence value, String message, Object ... messageParams) {
		if( value == null || value.length()==0 ) { 
			throw new IllegalArgumentException(String.format(message, messageParams));
		}
	}
	
	
	/**
	 * Assert that an array has no null elements.
	 * Note: Does not complain if the array is empty!
	 * <pre class="code">Assert.noNullElements(array, "The array must have non-null elements");</pre>
	 * @param array the array to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object array contains a <code>null</code> element
	 */
	public static void noNullElements(Object[] array, String message, Object ... messageParams) {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null) {
					throw new IllegalArgumentException(String.format(message,messageParams));
				}
			}
		}
	}

	/**
	 * Assert that an array has no null elements.
	 * Note: Does not complain if the array is empty!
	 * <pre class="code">Assert.noNullElements(array);</pre>
	 * @param array the array to check
	 * @throws IllegalArgumentException if the object array contains a <code>null</code> element
	 */
	public static void noNullElements(Object[] array) {
		noNullElements(array, "[Check failed] - this array must not contain any null elements");
	}

	/**
	 * Assert that a collection has elements; that is, it must not be
	 * <code>null</code> and must have at least one element.
	 * <pre class="code">Assert.notEmpty(collection, "Collection must have elements");</pre>
	 * @param collection the collection to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the collection is <code>null</code> or has no elements
	 */
	public static void notEmpty(Collection<?> collection, String message, Object ... messageParams) {
		if (collection==null || collection.size()==0) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert that a collection has elements; that is, it must not be
	 * <code>null</code> and must have at least one element.
	 * <pre class="code">Assert.notEmpty(collection, "Collection must have elements");</pre>
	 * @param collection the collection to check
	 * @throws IllegalArgumentException if the collection is <code>null</code> or has no elements
	 */
	public static void notEmpty(Collection<?> collection) {
		notEmpty(collection,
				"[Check failed] - this collection must not be empty: it must contain at least 1 element");
	}

	/**
	 * Assert that a Map has entries; that is, it must not be <code>null</code>
	 * and must have at least one entry.
	 * <pre class="code">Assert.notEmpty(map, "Map must have entries");</pre>
	 * @param map the map to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the map is <code>null</code> or has no entries
	 */
	public static void notEmpty(Map<?,?> map, String message, Object ... messageParams) {
		if (map==null || map.size()==0) {
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}

	/**
	 * Assert that a Map has entries; that is, it must not be <code>null</code>
	 * and must have at least one entry.
	 * <pre class="code">Assert.notEmpty(map);</pre>
	 * @param map the map to check
	 * @throws IllegalArgumentException if the map is <code>null</code> or has no entries
	 */
	public static void notEmpty(Map<?,?> map) {
		notEmpty(map, "[Check failed] - this map must not be empty; it must contain at least one entry");
	}


	/**
	 * Assert that the provided object is an instance of the provided class.
	 * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
	 * @param clazz the required class
	 * @param obj the object to check
	 * @throws IllegalArgumentException if the object is not an instance of clazz
	 * @see Class#isInstance
	 */
	public static void isInstanceOf(Class<?> clazz, Object obj) {
		isInstanceOf(clazz, obj, "");
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
	 * @param type the type to check against
	 * @param obj the object to check
	 * @param message a message which will be prepended to the message produced by
	 * the function itself, and which may be used to provide context. It should
	 * normally end in a ": " or ". " so that the function generate message looks
	 * ok when prepended to it.
	 * @throws IllegalArgumentException if the object is not an instance of clazz
	 * @see Class#isInstance
	 */
	public static void isInstanceOf(Class<?> type, Object obj, String message, Object ... messageParams) {
		notNull(type, "Type to check against must not be null");
		if (!type.isInstance(obj)) {
			throw new IllegalArgumentException(message +
					"Object of class [" + (obj != null ? obj.getClass().getName() : "null") +
					"] must be an instance of " + type);
		}
	}

	/**
	 * Assert that <code>superType.isAssignableFrom(subType)</code> is <code>true</code>.
	 * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
	 * @param superType the super type to check
	 * @param subType the sub type to check
	 * @throws IllegalArgumentException if the classes are not assignable
	 */
	public static void isAssignable(Class<?> superType, Class<?> subType) {
		isAssignable(superType, subType, "");
	}

	/**
	 * Assert that <code>superType.isAssignableFrom(subType)</code> is <code>true</code>.
	 * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
	 * @param superType the super type to check against
	 * @param subType the sub type to check
	 * @param message a message which will be prepended to the message produced by
	 * the function itself, and which may be used to provide context. It should
	 * normally end in a ": " or ". " so that the function generate message looks
	 * ok when prepended to it.
	 * @throws IllegalArgumentException if the classes are not assignable
	 */
	public static void isAssignable(Class<?> superType, Class<?> subType, String message, Object ... messageParams) {
		notNull(superType, "Type to check against must not be null");
		if (subType == null || !superType.isAssignableFrom(subType)) {
			throw new IllegalArgumentException(message + subType + " is not assignable to " + superType);
		}
	}

	/**
	 * Assert that the specified collection have to contain the specified value
	 *  
	 * @param <T> the type of the collection items
	 * @param collection the collection under to be tested
	 * @param value the value that the collection have to contains
	 * @param message
	 * @param messageParams
	 */
	public static <T> void contains( Collection<T> collection, T value, String message, Object ... messageParams ) { 
		notNull(collection,"[Check failed] - The collection object cannot be null");
		if( !collection.contains(value) ) { 
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
	}
	
	/**
	 * Assert that the specified collection have to contain the specified value 
	 * 
	 * @param <T> the type of the collection items
	 * @param collection the collection under to be tested
	 * @param value the value that the collection have to contains
	 * @param message the string in the raised exception 
	 * @param messageParams open arguments array used by <code>message</code> argument following the {@link String#format(String, Object...)} syntax/semantics 
	 * 
	 * @throws IllegalArgumentException when the collection does NOT contains the specified value
	 */
	public static <T> void contains( Collection<T> collection, T value ) { 
		contains(collection, value, "[Check failed] - Missing value '%s' in collection", value);
	}
	
	/**
	 * Check that a specified key exists in the map object 
	 * 
	 * @param <K> the key type 
	 * @param <V> the value type 
	 * @param map the map under check
	 * @param key the for which we check the existance
	 * @return the value in the map for the specified <code>key</code>. Note: <code>null</code> CAN be a valid returned value 
	 * @throws IllegalArgumentException if the map does not contains the specified <code>key</code>
	 */
	public static <K,V> V contains( Map<K,V> map, K key ) { 
		return contains(map,key, "[Check failed] - Missing key '%s' in map", key);
	}
	
	/**
	 * Check that a specified key exists in the map object 
	 * 
	 * @param <K> the key type 
	 * @param <V> the value type 
	 * @param map the map under check
	 * @param key the for which we check the existance
	 * @param message the string in the raised exception 
	 * @param open arguments array used by <code>message</code> argument following the {@link String#format(String, Object...)} syntax/semantics 
	 * @return the value in the map for the specified <code>key</code>. Note: <code>null</code> CAN be a valid returned value 
	 * @throws IllegalArgumentException if the map does not contains the specified <code>key</code>
	 */
	public static <K,V> V contains( Map<K,V> map, K key, String message, Object ... messageParams ) { 
		notNull(map,"[Check failed] - The map object cannot be null");
		if( !map.containsKey(key) ) { 
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
		return map.get(key);
			 
	}
	
	/**
	 * Check if a map object contains a value with the named specified by the <code>key</code> parameter.
	 * <p>Entry with <code>null</code> value are not accepted, so an exception will be raised in that case
	 * <p>Note: when the entry value is an array containing just ONE value, just the single array item is returned.   
	 * 
	 * @param <K> the key type 
	 * @param <V> the value type 
	 * @param map the map under check
	 * @param key the for which we check the existance
	 * @param message the string in the raised exception 
	 * @param open arguments array used by <code>message</code> argument following the {@link String#format(String, Object...)} syntax/semantics 
	 * @return the not null value in the map for the specified <code>key</code>.  
	 * @throws IllegalArgumentException if the map does not contains the specified <code>key</code> or the value is <code>null</code> 
	 */
	@SuppressWarnings("unchecked")
	public static <K,V> V param( Map<K,V> map, K key, String message, Object ... messageParams ) { 
		Object result = contains(map,key,message,messageParams);
		if( result == null ) { 
			throw new IllegalArgumentException(String.format(message,messageParams));
		}
		if( result.getClass().isArray() && Array.getLength(result) == 1 ) { 
			return (V) Array.get(result, 0);
		}
		return (V) result;
	}
	
	/**
	 * Check if a map object contains a value with the named specified by the <code>key</code> parameter.
	 * <p>Entry with <code>null</code> value are not accepted, so an exception will be raised in that case
	 * <p>Note: when the entry value is an array containing just ONE value, just the single array item is returned.   
	 * 
	 * @param <K> the key type 
	 * @param <V> the value type 
	 * @param map the map under check
	 * @param key the for which we check the existance
	 * @return the not null value in the map for the specified <code>key</code>.  
	 * @throws IllegalArgumentException if the map does not contains the specified <code>key</code> or the value is <code>null</code> 
	 */
	public static <K,V> V param( Map<K,V> map, K key) { 
		return param( map, key, String.format("[Check failed] - The param '%s' cannot be null", key));
	}
	
	
}

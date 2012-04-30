package util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Generic type wrapper. Make it possible to create instances of generic type 
 * <p>
 * Example: 
 * <pre>
 * String hola = new TypeWrapper<String>(){}.newInstance("Hola")
 *
 * </pre>
 * 
 * 
 * @author Paolo Di Tommaso
 *
 * @param <T>
 */
public abstract class TypeLiteral<T> implements Serializable
{

   private transient Type actualType;
   
   protected TypeLiteral() {}

   /**
    * @return the actual type represented by this object
    */
   public final Type getType() 
   {
      if (actualType==null) 
      {
         Class<?> typeLiteralSubclass = getTypeLiteralSubclass(this.getClass());
         if (typeLiteralSubclass == null) 
         {
            throw new RuntimeException(getClass() + " is not a subclass of TypeLiteral");
         }
         actualType = getTypeParameter(typeLiteralSubclass);
         if (actualType == null)
         {
            throw new RuntimeException(getClass() + " does not specify the type parameter T of TypeLiteral<T>");
         }
      }
      return actualType;
   }

   /**
    * @return the raw type represented by this object
    */
   @SuppressWarnings("unchecked")
   public final Class<T> getRawType() {
      Type type = getType();
      if (type instanceof Class) 
      {
         return (Class<T>) type;
      }
      else if (type instanceof ParameterizedType) 
      {
         return (Class<T>) ((ParameterizedType) type).getRawType();
      }
      else if (type instanceof GenericArrayType) 
      {
         return (Class<T>) Object[].class;
      }
      else 
      {
         throw new RuntimeException("Illegal type");
      }
   }
   
   private static Class<?> getTypeLiteralSubclass(Class<?> clazz)
   {
      Class<?> superclass = clazz.getSuperclass();
      if (superclass.equals(TypeLiteral.class))
      {
         return clazz;
      }
      else if (superclass.equals(Object.class))
      {
         return null;
      }
      else
      {
         return (getTypeLiteralSubclass(superclass));
      }
   }
   
   private static Type getTypeParameter(Class<?> superclass)
   {
      Type type = superclass.getGenericSuperclass();
      if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         if (parameterizedType.getActualTypeArguments().length == 1)
         {
            return parameterizedType.getActualTypeArguments()[0];
         }
      }
      return null;
   }
   
   @Override
   public boolean equals(Object obj) {
      if (obj instanceof TypeLiteral<?>)
      {
         TypeLiteral<?> that = (TypeLiteral<?>) obj;
         return this.getType().equals(that.getType());
      }
      return false;
   }
   
   @Override
   public int hashCode() {
      return getType().hashCode();
   }

   @Override
   public String toString()
   {
      return getType().toString();
   }
   
   
	public T newInstance() {
		Class<T> clazz = getRawType();
		try {
			return clazz.newInstance();
		} 
		catch (Exception e) {
			throw new RuntimeException(String.format("Cannot create a new instance for type: %s", clazz != null ? clazz.getName() : "null"));
		}
	} 
	
	public T newInstance( Object... args ) {
		Class<T> clazz = getRawType();

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
		} 
		catch (Exception e) {
			throw new RuntimeException(String.format("Cannot create a new instance for type: %s", clazz != null ? clazz.getName() : "null"));
		}
	}  
   
}

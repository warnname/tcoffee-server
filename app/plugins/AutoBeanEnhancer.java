package plugins;

import java.lang.reflect.Modifier;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import play.Logger;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;
import util.Utils;


public class AutoBeanEnhancer extends Enhancer {
	
	private CtClass ctClass;
	private String fCommaSeparatedFieldNames;
	private final String thisClazzName;
	private List<String> fFields;
	
	{
		thisClazzName = this.getClass().getName();
	}


	   
	@Override
	public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {
		
        ctClass = makeClass(applicationClass);
        if (ctClass.isInterface() || ctClass.isEnum() || ctClass.isAnnotation() ) {
            return;
        }

        if( !JavassistHelper. hasAnnotation(ctClass,AutoBean.class) ) { 
        	/* nothing to do - just return */
        	return ;
        }
        
		Logger.debug("Enhancing clazz: %s", applicationClass);
        addCopyConstructor();

        fFields = getProperties();
    	fCommaSeparatedFieldNames = Utils.asString(fFields.toArray(), ",", null);

        addToString();
        addEquals();
        addHashCode();
        
        
        // Done.
        applicationClass.enhancedByteCode = ctClass.toBytecode();
        ctClass.defrost();	        
      
	}

	private void addCopyConstructor() throws Exception {
		// 1. check if the copy constructor already exists 
		try { 
			ctClass.getDeclaredConstructor(new CtClass[]{ctClass});
			// if found just skip this method 
			return;
		}
		catch( NotFoundException e ) { 
			/* ok. go on */
		}
		
		// 3. the main copy constructor body 
		StringBuilder _copy = new StringBuilder();
		_copy.append("public ") .append(ctClass.getSimpleName()) 
			.append("(") .append(ctClass.getName()) .append(" that) ")
			.append("{");
		
		// 4. copy each property 
	   	for (CtField ctField : ctClass.getFields()) {
    		if( isProperty(ctField)) {
    			appendCopyField(_copy, ctField);
    		}
    	}
 		
		_copy.append("}");

		// 5. add to class
		CtConstructor copy = CtNewConstructor.make(_copy.toString(), ctClass);
		ctClass.addConstructor(copy);
		
	}
	
	private void appendCopyField( StringBuilder script, CtField ctField ) throws Exception { 
		script.append("this.") .append(ctField.getName() ) .append("=");
		
		if( ctField.getType().isPrimitive() ) { 
			 script.append("that.") .append(ctField.getName());
		}
		else { 
			script	.append("(") .append(ctField.getType().getName()) .append(")")
					.append(thisClazzName) .append("._deepCopy(that.") .append(ctField.getName()) .append(")");
		}
		
		script.append(";");
	}

	private void addToString() throws Exception {

		if( fFields.size()==0 ) return;
		
		try {
			ctClass.getDeclaredMethod("toString", new CtClass[] {});
			// if has this method do nothing just return;
			return;
		} 
		catch (NotFoundException e) {
			/* go on */
		}
		
		
		String script = 
			"public String toString() { " +
			"return "+thisClazzName+"._toString(this,\""+fCommaSeparatedFieldNames+"\"); " +
			"}"; 
		CtMethod method = CtMethod.make(script.toString(), ctClass);
		ctClass.addMethod(method);
	}
	

	private void addHashCode() throws Exception {
		if( fFields.size()==0 ) return;

		try {
			ctClass.getDeclaredMethod("hashCode", new CtClass[] {});
			// if has this method do nothing just return;
			return;
		} 
		catch (NotFoundException e) {
			/* go on */
		}
		

		String script = 
			"public int hashCode() { " +
			"return "+thisClazzName+"._hash(this,\""+fCommaSeparatedFieldNames+"\"); " +
			"}"; 
		CtMethod method = CtMethod.make(script.toString(), ctClass);
		ctClass.addMethod(method);
	}

	private void addEquals() throws Exception {
		if( fFields.size()==0 ) return;

		try {
			CtClass ctObject = classPool.get("java.lang.Object");
			ctClass.getDeclaredMethod("equals", new CtClass[] {ctObject});
			// if has this method do nothing just return;
			Logger.debug("%s.equals(Object) found", ctClass.getName());
			return;
		} 
		catch (NotFoundException e) {
			/* go on */
		}
		

		String script = 
			"public boolean equals(Object that) { " +
			"return "+thisClazzName+"._isEquals(this,that,\""+fCommaSeparatedFieldNames+"\"); " +
			"}"; 
		CtMethod method = CtMethod.make(script.toString(), ctClass);
		ctClass.addMethod(method);
	}
	
	
    /**
     * Is this field a valid javabean property ?
     */
    boolean isProperty(CtField ctField) {
        if (ctField.getName().equals(ctField.getName().toUpperCase()) || ctField.getName().substring(0, 1).equals(ctField.getName().substring(0, 1).toUpperCase())) {
            return false;
        }
        return Modifier.isPublic(ctField.getModifiers()) &&
                !Modifier.isFinal(ctField.getModifiers()) &&
                !Modifier.isStatic(ctField.getModifiers());
    }
    
    List<String> getProperties() {
    	List<String> result = new ArrayList<String>();
    	
    	for (CtField ctField : ctClass.getFields()) { // <-- look that all field, not just the ones declared in this class  
    		if( isProperty(ctField)) {
    			result.add(ctField.getName());
    		}
    	}
    	return result;
    }
    
    static <T> boolean hasParentMethod(Object obj, String name, Class<?>... types) {
    	Class clazz = obj.getClass().getSuperclass();
    	if( clazz==null || clazz.equals(Object.class)) { return false; };

    	try {
			clazz.getMethod(name,types);
			return true;
		} catch (NoSuchMethodException e) {
			/* does have that method - just return */
			return false;
		}
		
    }
    
	public static int _hash( Object obj, String props ) {
		
		String[] names =  props.split(",");
		return Utils.hash(obj, names);

	}
	
	public static boolean _isEquals( Object obj1, Object obj2, String props ) {
		String[] names =  props.split(",");
		return Utils.isEquals(obj1, obj2, names);
	}
	
	public static String _toString(Object obj, String props) {
		String[] names = Utils.isNotEmpty(props) ? props.split(",") : new String[0];
		return Utils.dump(obj, names);
	} 
	
	public static Object _deepCopy(Object obj) {
		if( obj == null ) { 
			return null;
		}
		
		Class clazz = obj.getClass();
		if( clazz.equals(String.class) ) { 
			return obj;
		}
		
		if( Date.class.isAssignableFrom(clazz)) { 
			return Utils.copy((Date)obj);
		}
		
		if( Boolean.class.isAssignableFrom(clazz)) {
			return ((Boolean)obj).booleanValue();
		}	
		
		if( Character.class.isAssignableFrom(clazz)) {
			return ((Character)obj).charValue();
		}
		
		if( Byte.class.isAssignableFrom(clazz)) {
			return ((Byte)obj).byteValue();
		}		
		
		if( Integer.class.isAssignableFrom(clazz)) {
			return ((Integer)obj).intValue();
		}		
	
		if( Long.class.isAssignableFrom(clazz)) {
			return ((Long)obj).longValue();
		}		
		
		if( Float.class.isAssignableFrom(clazz)) {
			return ((Float)obj).floatValue();
		}	
		
		if( Double.class.isAssignableFrom(clazz)) {
			return ((Double)obj).doubleValue();
		}		
		
		if( clazz.isArray() ) { 
			return Utils.copy((Object[])obj);
		}
		
		if( Collection.class.isAssignableFrom(clazz) ) { 
			return Utils.copy((Collection)obj);
		}
		
		if( Map.class.isAssignableFrom(clazz) ) { 
			return Utils.copy((Map)obj);
			
		}
		
		if( clazz.isEnum() ) {
			return obj;
		}
		
		return Utils.copy(obj);
	}
	
	

}

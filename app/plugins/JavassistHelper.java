package plugins;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

public class JavassistHelper {

	   /* This method has been added to Javassist 3.11 
	    * Can ben removed using that version 
	    */
	   public static boolean hasAnnotation(CtClass ctClass, Class annotation) {
	        ClassFile cf = ctClass.getClassFile2();
	        AnnotationsAttribute ainfo = (AnnotationsAttribute)
	                cf.getAttribute(AnnotationsAttribute.invisibleTag);  
	        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)
	                cf.getAttribute(AnnotationsAttribute.visibleTag);  
	        return hasAnnotationType(annotation, ctClass.getClassPool(), ainfo, ainfo2);
	    }	
	
	   
	   static boolean hasAnnotationType(Class clz, ClassPool cp, AnnotationsAttribute a1, AnnotationsAttribute a2)
		{
			Annotation[] anno1, anno2;
			
			if (a1 == null)
			anno1 = null;
			else
			anno1 = a1.getAnnotations();
			
			if (a2 == null)
			anno2 = null;
			else
			anno2 = a2.getAnnotations();
			
			String typeName = clz.getName();
			if (anno1 != null)
			for (int i = 0; i < anno1.length; i++)
			if (anno1[i].getTypeName().equals(typeName))
			return true;
			
			if (anno2 != null)
			for (int i = 0; i < anno2.length; i++)
			if (anno2[i].getTypeName().equals(typeName))
			return true;
			
			return false;
		}	     	
	
}

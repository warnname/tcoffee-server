package plugins;

import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import util.Utils;
import exception.QuickException;

/** 
 * Add the following utility method on all class that does not provide a custom implementation 
 * <li>toString()</li>
 * <li>equals(Object)</li>
 * <li>hashCode()</li>
 * <li><i>a copy constructor()</i></li>
 * 
 * @author Paolo Di Tommaso
 *
 */
public class AutoBeanPlugin extends PlayPlugin {

	/* NOTE!
	 * Statically reference this to force preload, otherwise a ClassCircularityException is raised
	 */
	private static Class _clazz1 = AutoBeanEnhancer.class;
	private static Class _clazz2 = Utils.class;
	private static Class _clazz3 = QuickException.class;
	private static Class _clazz4 = AutoBean.class;
	private static Class _clazz5 = JavassistHelper.class;

	public void enhance(ApplicationClass applicationClass) throws Exception {
		new AutoBeanEnhancer().enhanceThisClass(applicationClass);
	}
}


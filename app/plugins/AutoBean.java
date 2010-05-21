package plugins;

/**
 * Marks a Java class as an <i>Auto bean</i>.
 * <p>
 * Autobeans extends the Play! properties mechanism adding to each class (marked with this annotation) 
 * the following methods automatically 
 * <li><i>Copy constructor</i></li>
 * <li><i>hashCode()</i></li>
 * <li><i>equals(Object)</i></li>
 * <li><i>toString()</i></li>
 * 
 * @see {@link AutoBeanPlugin}
 * 
 * @author Paolo Di Tommas
 *
 */
public @interface AutoBean {

}

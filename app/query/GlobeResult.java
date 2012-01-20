package query;

import java.io.Serializable;
import java.util.List;

/**
 * Wrap result for globe data statistics 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class GlobeResult implements Serializable {

	/**
	 * The number of maximum occurences 
	 */
	public long max;
	
	/**
	 * The data result set. Each item is an array of 3 element, like the following
	 * <pre>
	 * [ latitude, longitude, num of occurences ]
	 * </pre>
	 */
	public List<Object[]> items;
}

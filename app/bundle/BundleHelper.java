package bundle;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class BundleHelper {

	/**
	 * Find out the set of new bundles i.e. the ones in 'found' set not existing 'installed' set
	 * 
	 * @param installed the set of all current installed bundles path
	 * @param found the set of all discovered paths on the file system
	 * @return the new bundles path to be installed
	 */
	public static Set<File> getNewBundlesPath(Set<File> installed, Set<File> found ) {
		Set<File> result = new HashSet<File>(found);
		result.removeAll( installed );
		return result;
	}

	/**
	 * Find out the bundles that have been dropped from the current installation 
	 * 
	 * @param installed
	 * @param found
	 * @return
	 */
	public static Set<File> getDroppedBundlesPath(Set<File> installed, Set<File> found ) {
		Set<File> result = new HashSet<File>(installed);
		result.removeAll( found );
		return result;
	}	
	
	public static Set<File> getExistingBundlesPath(Set<File> installed, Set<File> found ) {
		Set<File> result = new HashSet<File>(installed);
		result.retainAll( found );
		return result;
	}
	
	

	
	
}

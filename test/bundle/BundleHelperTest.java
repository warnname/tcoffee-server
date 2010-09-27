package bundle;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class BundleHelperTest extends UnitTest {

	private HashSet<File> installed;
	private HashSet<File> found;

	@Before
	public void init() { 
		installed = new HashSet<File>();
		installed.add( new File("a") );
		installed.add( new File("b") );
		installed.add( new File("c") );
		
		found = new HashSet<File>();
		found.add( new File("b") );
		found.add( new File("c") );
		found.add( new File("d") );
		found.add( new File("e") );
		
		
	}
	
	@Test 
	public void testGetNewBundlesPath() {
		
		Set<File> result = BundleHelper.getNewBundlesPath(installed, found);
		
		/* 
		 * in the new bundles set should find files: 'd' and 'e' 
		 */
		
		Set<File> expected = new HashSet<File>(1);
		expected.add(new File("d"));
		expected.add(new File("e"));
		assertEquals( expected, result );
	} 
	
	@Test 
	public void testDroppedBundlesPath() {
		
		Set<File> result = BundleHelper.getDroppedBundlesPath(installed, found);
		
		/* 
		 * in the new bundles set should find files: 'd' and 'e' 
		 */
		
		Set<File> expected = new HashSet<File>(1);
		expected.add(new File("a"));
		assertEquals( expected, result );
	} 
	

	@Test 
	public void testExistingBundlesPath() {
		
		Set<File> result = BundleHelper.getExistingBundlesPath(installed, found);
		
		/* 
		 * in the new bundles set should find files: 'd' and 'e' 
		 */
		
		Set<File> expected = new HashSet<File>(1);
		expected.add(new File("b"));
		expected.add(new File("c"));
		assertEquals( expected, result );
	} 
	
}

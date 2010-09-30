package bundle;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
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
	
	
	@Test 
	public void testGetServiceXml() {
		String bundle =
			"<bundle>" +
			"<service name='test'>" +
			"<elem>xxx</elem>" +
			"</service>" +
			"<service name='more'>" +
			"</service>" +
			"</bundle>";

		String result  = BundleHelper.getServiceXml(bundle, "test");
		
		assertEquals("<service name=\"test\"><elem>xxx</elem></service>", result);
	}
	
	@Test 
	public void testServiceXmlReplace( ) { 
		String bundle =
			"<bundle>" +
			"<service name='test'>" +
			"<elem></elem>" +
			"</service>" +
			"<service name='more'>" +
			"</service>" +
			"</bundle>";

		String service = 
			"<service name='test'>" +
			"<content attr='1'>" +
			"<test>elem</test>" +
			"</content>" +
			"</service>";
		
		String xml = BundleHelper.replaceServiceXml(bundle, service);
		
		assertTrue( xml.contains("<content attr=\"1\">") );
		assertTrue( xml.contains("<test>elem</test>") );
	}
	
	
	@Test 
	public void testRemoveServiceXml() throws DocumentException { 
		String bundle =
			"<bundle>" +
			"<service ID='1' name='test'>" +
			"<elem></elem>" +
			"</service>" +
			"<service ID='2' name='more'>" +
			"</service>" +
			"</bundle>";
		
		String xml = BundleHelper.deleteServiceXml(bundle, "test");
		
		Document doc= DocumentHelper.parseText(xml);
		assertTrue( doc.elementByID("1") == null  );
		assertTrue( doc.elementByID("2") != null  );
		
	}
	
	@Test
	public void testAddServiceXml() throws DocumentException { 
		String bundle =
			"<bundle>" +
			"<service ID='1' name='test'>" +
			"<elem></elem>" +
			"</service>" +
			"<service ID='2' name='more'>" +
			"</service>" +
			"</bundle>";
		
		String toAdd =  "<service ID='3' ><elem>content</elem></service>" ;
		
		String result = BundleHelper.addServiceXml(bundle, toAdd);
		
		Document doc= DocumentHelper.parseText(result);
		assertTrue( doc.elementByID("1") != null  );
		assertTrue( doc.elementByID("2") != null  );
		assertTrue( doc.elementByID("3") != null  );
		
		
	}
}

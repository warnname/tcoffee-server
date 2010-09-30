package bundle;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import util.Utils;

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
	
	public static Element getServiceElement( String bundleXml, String serviceName ) { 
		try {
			Document doc = DocumentHelper.parseText(bundleXml);
			Element elem = (Element) doc.selectSingleNode(String.format("/bundle/service[@name='%s']", serviceName));
			return elem;
		} 
		catch (DocumentException e) {
			throw new BundleException(e);
		}
	}
	
	public static String getServiceXml( String bundleXml, String serviceName ) { 

		Element elem = getServiceElement(bundleXml, serviceName);
		if( elem == null ) { 
			throw new BundleException("Missing service '%s' on bundle configuration", serviceName);
		}
		return elem.asXML();
		
	}
	
	public static String deleteServiceXml( String bundleXml, String serviceName ) { 
	
		/* parse the bundle xml */
		try { 
			Element elem = getServiceElement(bundleXml, serviceName);
			if( elem == null ) { 
				throw new BundleException("Missing service '%s' on bundle configuration", serviceName);
			}

			Document doc = elem.getDocument();
			elem.getParent().remove( elem );
			
			/* save to temporary file */
			StringWriter buffer = new StringWriter();
			
			// Pretty print the document to System.out
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        XMLWriter writer = new XMLWriter( buffer, format );
	        writer.write( doc );		

						
			return buffer.toString();		
		}
		catch( Exception e ) { 
			throw new BundleException(e);
		}
		
	}

	public static String replaceServiceXml( String bundleXml, String serviceXml ) { 
		try {
			/* parse the fragment and extract the name attribute */
			Document fragment = DocumentHelper.parseText(serviceXml);
			Element fragRoot = fragment.getRootElement();
			String serviceName = fragRoot.attributeValue("name");
			if( Utils.isEmpty(serviceName)) { 
				throw new BundleException("Missing 'name' attribute on service xml fragment");
			}
			
			/* parse the bundle xml */
			SAXReader reader = new SAXReader();
			Document doc = reader.read( new StringReader(bundleXml) );
			Element elem = (Element) doc.selectSingleNode(String.format("/bundle/service[@name='%s']", serviceName));
			if( elem == null ) { 
				throw new BundleException("Missing service '%s' on bundle configuration", serviceName);
			}
			
			/* empty the target service element */
			elem.clearContent();
				
			/* add the new content */
			Iterator content = fragment.getRootElement().elementIterator();
			while( content.hasNext() ) { 
				Element e = (Element) content.next();
				elem.add( e.createCopy() );
			}
		
			/* save to temporary file */
			StringWriter buffer = new StringWriter();
			
			// Pretty print the document to System.out
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        XMLWriter writer = new XMLWriter( buffer, format );
	        writer.write( doc );		

						
			return buffer.toString();			
		}
		catch( Exception e) { 
			throw new BundleException(e);
		}
	}
	
	public static String addServiceXml( String bundleXml, String serviceXml ) { 
	
		try {
			/* parse the fragment and extract the name attribute */
			Document fragment = DocumentHelper.parseText(serviceXml);
			
			/* parse the bundle xml */
			Document doc = DocumentHelper.parseText(bundleXml);
			
			/* empty the target service element */
			doc.getRootElement().add( fragment.getRootElement().createCopy() );
				

			/* save to temporary file */
			StringWriter buffer = new StringWriter();
			
			// Pretty print the document to System.out
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        XMLWriter writer = new XMLWriter( buffer, format );
	        writer.write( doc );		

						
			return buffer.toString();			
			
		}
		catch( Exception e ) {
			throw new BundleException(e);
		}
	}
}

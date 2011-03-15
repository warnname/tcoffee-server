package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import models.Bundle;
import models.Field;
import models.Fieldset;
import models.Input;
import models.Service;
import play.Play;
import play.libs.IO;
import play.mvc.Scope;
import exception.QuickException;

/**
 * Test helper class 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class TestHelper {

	public static Service init() {
		return init(new String[]{});
	}
	
	public static Service init(String ... fieldsAndValues) {
		Scope.Params.current().put("bundle", "tcoffee");
		Service service = new Service();

		Bundle bundle = Bundle.read( new File(Play.applicationPath,"bundles/tcoffee") );
		
		bundle.services = new ArrayList<Service>();
		bundle.services.add(service);
		service.bundle = bundle;
	
		Fieldset set = new Fieldset();

		for( String pair : fieldsAndValues ) {
			String[] items = pair.split("=");
			set.add(new Field("text",items[0], items.length>1 ? items[1] : ""));
		}
		
		service.input = new Input();
		service.input.fieldsets().add(set);

		service.init( );
		
		
		
		return Service.current(service);		
	}

	public static File file(String file) {
		return new File(TestHelper.class.getResource(file).getFile());
	}

	public static File sampleLog() {
		return file("/sample-tcoffee.log");
	}

	public static File sampleFasta() {
		return file("/sample.fasta");
	}
	
	public static File sampleClustal() {
		return file("/sample-clustalw.txt");
	}
	
	public static void copy( File source, File target ) {
		Check.isTrue(source.exists(), "The source file does not exists: %s", source);
		Check.isTrue(source.isFile(), "The source is not a file: %s", source);

		File folder; 
		if( target.isDirectory() ) {
			folder = target;
		}
		else {
			folder = target.getParentFile();
		}
		
		if( !folder.exists() ) {
			Check.isTrue( folder.mkdirs(), "Unable to create destination folder: %s", folder );
		}
		
		if( target.isDirectory() ) {
			target = new File(target, source.getName());
		}

		try {
			InputStream in = new FileInputStream(source);
			OutputStream out = new FileOutputStream(target);
			IO.write(in, out);

		} catch( IOException e ) {
			throw new QuickException(e, "Unable to copy '%s' --> '%s'", source,target);
		}

	}
	
	public static int randomHash() {
		return new Double(Math.random()).hashCode();
	}
	
	public static String randomHashString() {
		return Integer.toHexString(randomHash());
	}
	
	public static void sleep( long millis ) {
		try {
			Thread.currentThread().sleep(millis);
		} catch (InterruptedException e) { }
	}
	
	public static Bundle bundle() { 
		return Bundle.read( new File(Play.applicationPath, "test/test-bundle") );
	}
}

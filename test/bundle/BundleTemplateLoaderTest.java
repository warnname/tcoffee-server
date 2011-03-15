package bundle;

import models.Bundle;

import org.junit.Test;

import play.templates.Template;
import play.test.UnitTest;
import util.TestHelper;
import util.Utils;

public class BundleTemplateLoaderTest extends UnitTest {

	private Bundle bundle;

	@org.junit.Before
	public void before() { 
		bundle = TestHelper.bundle();
	}
	
	@Test
	public void testLoadMailTemplate() { 
		Template template = BundleTemplateLoader.mail(bundle, "result-ok.txt");
		String result = template.render(Utils.asMap("url=http://localhost/somewhere"));
		assertTrue(result.contains("http://localhost/somewhere"));
	}
	
	@Test
	public void testLoadPageTemplate() { 
		Template template = BundleTemplateLoader.load(bundle, "index.html");
		assertNotNull(template);
	}

}

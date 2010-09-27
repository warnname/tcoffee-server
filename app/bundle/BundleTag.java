package bundle;

import exception.QuickException;
import groovy.lang.Closure;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import models.Bundle;
import play.mvc.Scope;
import play.templates.FastTags;
import play.templates.Template;
import play.templates.Template.ExecutableTemplate;
import play.vfs.VirtualFile;
import util.Check;

public class BundleTag extends FastTags {

	
	/**
	 * Renders a bundle provided page
	 * 
	 * @param args
	 * @param body
	 * @param out
	 * @param template
	 * @param fromLine
	 */
	public static void _include_bundle(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {

		if (!args.containsKey("arg") || args.get("arg") == null) {
			throw new QuickException("Missing bundle page name. You should provide a page name to render like {include_bundle 'page-to-render.html' /}");
        }

		/* check that all required parameters have been specified */
		Object _arg = args.get("arg");
        Check.notNull(_arg, "Missing parameter on {include_bundle /} tag");
        
        /* retrieve the current bundle */
        Bundle bundle = (Bundle) Scope.RenderArgs.current().get("_bundle");
        Check.notNull(bundle, "Missing bundle object invoking {include_bundle /} tag");

        Template t;
        if( _arg instanceof File ) { 
        	t = BundlePageLoader.load(bundle, VirtualFile.open((File)_arg));
        }
        else if (_arg instanceof VirtualFile) { 
        	t = BundlePageLoader.load(bundle, (VirtualFile)_arg);
        }
        else { 
        	t = BundlePageLoader.load(bundle, _arg.toString());
        }
        
        /* invoke the page template */
        Map newArgs = new HashMap();
        newArgs.putAll(template.getBinding().getVariables());
        newArgs.put("_isInclude", true);
        t.render(newArgs);
		
	}

		
	
}

package bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Bundle;
import play.exceptions.TemplateNotFoundException;
import play.templates.Template;
import play.templates.TemplateCompiler;
import play.templates.TemplateLoader;
import play.vfs.VirtualFile;

public class BundlePageLoader extends TemplateLoader {

	/** 
	 * maintains a list of keys for cached template to be able to free the cached for a specified template 
	 */
	static final Map<Bundle, List<String>> mapOfTemplateKeyByBundle = new HashMap<Bundle,List<String>>();

	public static void cleanCacheForBundle(Bundle bundle) { 
		List<String> keys = mapOfTemplateKeyByBundle.get(bundle);
		if( keys != null ) for( String templateKey : keys ) { 
			templates.remove(templateKey);
		}
		mapOfTemplateKeyByBundle.remove(bundle);
	}
	
	static protected void putTemplateKeyForBundle( Bundle bundle, String keyToStore ) { 
		List<String> list = mapOfTemplateKeyByBundle.get(bundle);
		if( list == null ) { 
			list = new ArrayList<String>();
			mapOfTemplateKeyByBundle.put(bundle,list);
		}
		
		if( !list.contains(keyToStore)) { 
			list.add(keyToStore);
		}
	}
	
	
	public static Template load( Bundle bundle, String page ) { 
		if( bundle.pagesPath == null ) { 
			throw new TemplateNotFoundException(page);
		}
		
		VirtualFile file = bundle.pagesPath.child(page);
		if( !file.exists() ) { 
			throw new TemplateNotFoundException(page);
		}
		
		return load(bundle,file);
	}	
	
	/**
	 * Override this to make it able to re-compile bundle provided template at runtime
	 * 
	 */
    public static Template load(Bundle bundle, VirtualFile file) {
    	
        if( templates == null ) { 
    		templates = new HashMap<String, Template>();
        }
		
		String key = (file.relativePath().hashCode()+"").replace("-", "M");
        if (!templates.containsKey(key) || templates.get(key).compiledTemplate == null) {
            Template template = new Template(file.relativePath(), file.contentAsString());
            if(template.loadFromCache()) {
                templates.put(key, template);
            } else {
                templates.put(key, TemplateCompiler.compile(file));
            }
        } else {
            Template template = templates.get(key);
            if (template.timestamp < file.lastModified()) {
                templates.put(key, TemplateCompiler.compile(file));
            }
        }
        if (templates.get(key) == null) {
            throw new TemplateNotFoundException(file.relativePath());
        }
        
        /* store also the key for this bundle */
        putTemplateKeyForBundle(bundle,key);
        
        return templates.get(key);
     }	
}

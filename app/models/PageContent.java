package models;

import java.util.ArrayList;
import java.util.List;

import play.mvc.Router;
import util.Check;

/**
 * Models the content a of generic page. See <code>page.html</code>
 * @author Paolo Di Tommaso
 *
 */
public class PageContent {
	
	public String title;
	public String description;
	
	public List<String> paragraphs = new ArrayList<String>(); 

	public List<Link> links = new ArrayList<Link>();
	
	public AutoLink redirect;
	
	public void addParagraph( String text ) {
		paragraphs.add(text);
	}
	
	public void addParagraph( String text, Object... args) {
		paragraphs.add(String.format(text, args));
	}
	
	public void addLink( String href ) {
		Check.notEmpty("Argument 'href' cannot be empty");
		Link link = new Link();
		link.href = href;
		addLink(link);
	}
	
	public void addLink( String href, String label ) {
		Check.notEmpty("Argument 'href' cannot be empty");
		Link link = new Link();
		link.href = href;
		link.label = label;
		addLink(link);
	}
	
	public void addAutoLink( String href ) {
		Check.notEmpty("Argument 'href' cannot be empty");
		Link link = new AutoLink();
		link.href = href;
		addLink(link);
	}
	
	public void addAutoLink( String href, String label ) {
		Check.notEmpty("Argument 'href' cannot be empty");
		Link link = new AutoLink();
		link.href = href;
		link.label = label;
		addLink(link);
	}
	
	public void addLink( Link link ) {
		Check.notNull("Argument 'link' cannot be null");
		links.add(link);
		
		if( link instanceof AutoLink ) {
			redirect = (AutoLink) link;
		}
	}
	
	/**
	 * Model a html link 
	 * @author Paolo Di Tommaso
	 *
	 */
	public class Link {
		public String href;
		public String label;
		public String target;
		
		public String getHref() {
			if( href == null ) return null;
			
			if( href.startsWith("@") ) {
				return Router.reverse(href.substring(1)).toString();
			}
			else {
				return href;
			}
		}
		
		public String getLabel() {
			return label!=null ? label : href;
		}
	}
	
	/**
	 * Model an html link that will be used also as content of the meta-refersh directive
	 * 
	 * @author Paolo Di Tommaso
	 *
	 */
	public class AutoLink extends Link  {
		/** The deplay (in secs) before the refresh happen */ 
		public int delay = 10;
	}
}

package util;

import exception.QuickException;
import groovy.lang.Closure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import models.Field;
import play.Logger;
import play.templates.FastTags;
import play.templates.JavaExtensions;
import play.templates.Template.ExecutableTemplate;
import util.TcoffeeHelper.ResultHtml;

public class Tags extends FastTags {
	
	public static void _render(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Object _arg = args.get("arg");

		if( _arg == null ) {
			Logger.info("Nothing to render. Did you specify argument on #{render} tag?");
		}
		else if( _arg instanceof Field ) {
			template.invokeTag(fromLine, "field", (Map<String, Object>) args, body );
		}
		else {
			Logger.info("Nothing to render. Unknow type of argument: %s", _arg);
		}
	}
		
	
	public static void _listwrap(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		/* the list of items to wrap */
		List<Object> list = null;
		Object value = args.get("list");
		if( value == null ) {
			list = new ArrayList(0);
		}
		else if( value.getClass().isArray() ) {
			list = Arrays.asList( (Object[])value );
		}
		else if( value instanceof List ) {
			list = (List)value;
		}
		else if( value instanceof Collection ) {
			list = new ArrayList((Collection)value);
		}
		else if( value instanceof Map ) {
			list = new ArrayList( ((Map)value).entrySet() );
		}
		else {
			list = new ArrayList(1);
			list.add( value );
		}
 		
		/* class to stylize the table */
		String clazz = (String) args.get("class");
		
		
		/* the number of cols */
		Integer cols = (Integer) args.get("cols");
		if( cols == null ) {
			cols = list.size()+1; // to render just one line
		}
		
		String as = (String) args.get("as");
		
		/* number of rows */
		int rows = (int)Math.floor(list.size() / cols) +1;
		int index = 0;
		
		out.append("<table");
		if( Utils.isNotEmpty(clazz)) {
			out.append(" class=\"") .append(clazz) .append("\" ");
		}
		out.append("><tbody>");
		
		for( int x=0; x<rows; x++  ) {
			out.append("<tr>");
			for( int y=0; y<cols && index<list.size(); y++ ) {
				out.append("<td>");
				if( Utils.isNotEmpty(as) ) {
					body.setProperty(as, list.get(index));
				}
				body.call();
				out.append("</td>");
				index++;
			} 
			out.append("</tr>");
		} 
		
		out.append("</tbody></table>");
		
	} 
	

	public static void _includefile(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws IOException {
		Check.notEmpty(args,"You must provide the file to include");
		
		File file;
		
		Object _arg = args.values().iterator().next();
		if( _arg instanceof File ) {
			file = (File)_arg;
		}
		else {
			throw new QuickException("#{includefile /} requires to specify a File instance as argument");
		}
		
		boolean escape = Boolean.TRUE.equals(args.get("escapeHtml"));
	
		for( String line : new FileIterator(file)) {
			if( escape ) {
				line = JavaExtensions.escapeHtml(line);
			}
			out.println(line);
		}
	
	}
	
	public static void _tcoffeeHtml(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws IOException {

		Check.notEmpty(args,"You must provide the t-coffee html file to render");
		
		File file;
		
		Object _arg = args.values().iterator().next();
		if( _arg instanceof File ) {
			file = (File)_arg;
		}
		else {
			throw new QuickException("#{tcoffeeHtml /} requires to specify the html file as argument");
		}

		ResultHtml result = TcoffeeHelper.parseHtml(file);
		if( result == null ) {
		
			return;
		}
		
		out.println("<style type='text/css'>");
		BufferedReader reader = new BufferedReader(new StringReader(result.style));
		String line;
		while((line=reader.readLine())!=null) {
			if(Utils.isNotEmpty(line)) {
				out.print("#result ");
				out.println(line);
			}
		}
		out.println("</style>");
		
		out.print("<div>");
		out.println( result.body );
		out.println("</div>");
		
	}
	

}

package util;

import exception.QuickException;
import groovy.lang.Closure;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.Field;
import play.Logger;
import play.templates.FastTags;
import play.templates.JavaExtensions;
import play.templates.Template.ExecutableTemplate;
import util.TcoffeeHelper.ResultHtml;

public class ServerTags extends FastTags {

	public static void _render(Map<?, ?> args, Closure body, PrintWriter out,
			ExecutableTemplate template, int fromLine) {
		Object _arg = args.get("arg");

		if (_arg == null) {
			Logger.info("Nothing to render. Did you specify argument on #{render} tag?");
		} else if (_arg instanceof Field) {
			template.invokeTag(fromLine, "field", (Map<String, Object>) args,
					body);
		} else {
			Logger.info("Nothing to render. Unknow type of argument: %s", _arg);
		}
	}

	public static void _listwrap(Map<?, ?> args, Closure body, PrintWriter out,
			ExecutableTemplate template, int fromLine) {
		/* the list of items to wrap */
		List<Object> list = null;
		Object value = args.get("list");
		if (value == null) {
			list = new ArrayList(0);
		} else if (value.getClass().isArray()) {
			list = Arrays.asList((Object[]) value);
		} else if (value instanceof List) {
			list = (List) value;
		} else if (value instanceof Collection) {
			list = new ArrayList((Collection) value);
		} else if (value instanceof Map) {
			list = new ArrayList(((Map) value).entrySet());
		} else {
			list = new ArrayList(1);
			list.add(value);
		}

		/* class to stylize the table */
		String clazz = (String) args.get("class");

		/* the number of cols */
		Integer cols = (Integer) args.get("cols");
		if (cols == null) {
			cols = list.size() + 1; // to render just one line
		}

		String as = (String) args.get("as");

		/* number of rows */
		int rows = (int) Math.floor(list.size() / cols) + 1;
		int index = 0;

		out.append("<table");
		if (Utils.isNotEmpty(clazz)) {
			out.append(" class=\"").append(clazz).append("\" ");
		}
		out.append("><tbody>");

		for (int x = 0; x < rows; x++) {
			out.append("<tr>");
			for (int y = 0; y < cols && index < list.size(); y++) {
				out.append("<td>");
				if (Utils.isNotEmpty(as)) {
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

	public static void _includefile(Map<?, ?> args, Closure body,
			PrintWriter out, ExecutableTemplate template, int fromLine)
			throws IOException {
		Check.notEmpty(args, "You must provide the file to include");

		File file;

		Object _arg = args.values().iterator().next();
		if (_arg instanceof File) {
			file = (File) _arg;
		} else {
			throw new QuickException(
					"#{includefile /} requires to specify a File instance as argument");
		}

		boolean escape = Boolean.TRUE.equals(args.get("escapeHtml"));

		for (String line : new FileIterator(file)) {
			if (escape) {
				line = JavaExtensions.escapeHtml(line);
			}
			out.println(line);
		}

	}

	public static void _tcoffeeHtml(Map<?, ?> args, Closure body,
			PrintWriter out, ExecutableTemplate template, int fromLine)
			throws IOException {

		Check.notEmpty(args,
				"You must provide the t-coffee html file to render");

		File file;

		Object _arg = args.values().iterator().next();
		if (_arg instanceof File) {
			file = (File) _arg;
		} else {
			throw new QuickException(
					"#{tcoffeeHtml /} requires to specify the html file as argument");
		}

		ResultHtml result = TcoffeeHelper.parseHtml(file);
		if (result == null) {

			return;
		}

		out.println("<style type='text/css'>");
		BufferedReader reader = new BufferedReader(new StringReader(
				result.style));
		String line;
		while ((line = reader.readLine()) != null) {
			if (Utils.isNotEmpty(line)) {
				out.print("#result ");
				out.println(line);
			}
		}
		out.println("</style>");

		out.print("<div>");
		out.println(result.body);
		out.println("</div>");

	}

	/**
	 * another try to create a simple "each" tag as in #{each cities} .... ${_}
	 * #{/each}
	 * 
	 * @author bran
	 * 
	 * @param args
	 * @param body
	 * @param out
	 * @param template
	 * @param fromLine
	 */
	public static void _each(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Object x = args.get("arg");
		if (x instanceof Collection) {
			Collection items = (Collection) x;
			Iterator it = items.iterator();
			iteratorOnBody(body, it);
		} else if (x instanceof Iterator) {
			Iterator it = (Iterator) x;
			iteratorOnBody(body, it);
		} else if (x instanceof Iterable) {
			Iterator it = ((Iterable) x).iterator();
			iteratorOnBody(body, it);
		} else if (x instanceof Map) {
			int start = 0, end = 0;
			Set<Entry> entrySet = ((Map) x).entrySet();
			Iterator<Entry> it = entrySet.iterator();
			end = entrySet.size();
			int i = 0;
			while (it.hasNext()) {
				Entry o = it.next();
				// body.setProperty("it", o); // 'it' is reserved somewhere
				body.setProperty("_", o);
				body.setProperty("_key", o.getKey());
				body.setProperty("_value", o.getValue());
				body.setProperty("_index", i + 1);
				body.setProperty("_isLast", (i + 1) == end);
				body.setProperty("_isFirst", i == start);
				body.setProperty("_parity", (i + 1) % 2 == 0 ? "even" : "odd");
				body.call();
				i++;
			}
		} else if (x instanceof Object[]) {
			Object[] oa = ((Object[]) x);
			arrayOnBody(body, oa);
		} else if (x instanceof boolean[]) {
			boolean[] ba = ((boolean[]) x);
			Object[] oa = new Object[ba.length];
			for (int i = 0; i < ba.length; i++) {
				oa[i] = ba[i];
			}
			arrayOnBody(body, oa);
		} else if (x instanceof char[]) {
			char[] ba = ((char[]) x);
			Object[] oa = new Object[ba.length];
			for (int i = 0; i < ba.length; i++) {
				oa[i] = ba[i];
			}
			arrayOnBody(body, oa);
		} else if (x instanceof int[]) {
			int[] ba = ((int[]) x);
			Object[] oa = new Object[ba.length];
			for (int i = 0; i < ba.length; i++) {
				oa[i] = ba[i];
			}
			arrayOnBody(body, oa);
		} else if (x instanceof long[]) {
			long[] ba = ((long[]) x);
			Object[] oa = new Object[ba.length];
			for (int i = 0; i < ba.length; i++) {
				oa[i] = ba[i];
			}
			arrayOnBody(body, oa);
		} else if (x instanceof float[]) {
			float[] ba = ((float[]) x);
			Object[] oa = new Object[ba.length];
			for (int i = 0; i < ba.length; i++) {
				oa[i] = ba[i];
			}
			arrayOnBody(body, oa);
		} else if (x instanceof double[]) {
			double[] ba = ((double[]) x);
			Object[] oa = new Object[ba.length];
			for (int i = 0; i < ba.length; i++) {
				oa[i] = ba[i];
			}
			arrayOnBody(body, oa);
		} else {
			// perhaps we need to handle array of primitive types such as [C,
			// [I, [L, [F, etc
			throw new play.exceptions.TagInternalException(
					"the each tag requires a Collection, Map, or an array object.");
		}
	}

	/**
	 * @param body
	 * @param it
	 */
	private static void iteratorOnBody(Closure body, Iterator it) {
		int start = 0;
		int i = 0;
		while (it.hasNext()) {
			Object o = it.next();
			// body.setProperty("it", o); // 'it' is reserved somewhere
			body.setProperty("_", o);
			body.setProperty("_index", i + 1);
			if (!it.hasNext())
				body.setProperty("_isLast", true);
			body.setProperty("_isFirst", i == start);
			body.setProperty("_parity", (i + 1) % 2 == 0 ? "even" : "odd");
			body.call();
			i++;
		}
	}

	/**
	 * @param body
	 * @param oa
	 */
	private static <T> void arrayOnBody(Closure body, T[] oa) {
		int start = 0, end = oa.length;
		int i = 0;
		for (Object o : oa) {
			// body.setProperty("it", o); // 'it' is reserved somewhere
			body.setProperty("_", o);
			body.setProperty("_index", i + 1);
			body.setProperty("_isLast", (i + 1) == end);
			body.setProperty("_isFirst", i == start);
			body.setProperty("_parity", (i + 1) % 2 == 0 ? "even" : "odd");
			body.call();
			i++;
		}
	}

}

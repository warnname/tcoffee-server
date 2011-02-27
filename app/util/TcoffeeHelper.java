package util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Logger;
import play.libs.IO;

/**
 * Utility class for T-Coffee related operations 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class TcoffeeHelper {
	
	public static class ResultHtml {
		public String style;
		public String body;
	}
	

	public static ResultHtml parseHtml( File file )  
	{
		return parseHtml(IO.readContentAsString(file));
	}
	
	public static ResultHtml parseHtml(String html) {
		Check.notNull(html, "Argument 'html' cannot be null");
		Pattern RE_TCOFFEE_HTML = Pattern.compile("^[\\s\\S]*<style>([\\s\\S]*)</style>[\\s\\S]*<body>([\\s\\S]*)</body>[\\s\\S]*$");
		ResultHtml result = null; 
		Matcher match = RE_TCOFFEE_HTML.matcher(html);
		if(match.matches()) {
			String style = match.group(1);
			String body = match.group(2);

			/* remove the cpu time */
			body = body.replaceFirst("<span[^>]*>CPU&nbsp;TIME:[^<]*</span><br>","");
			
			/* final assignment */
			result = new ResultHtml();
			result.style = style;
			result.body = body;
			
		}
		
		return result;
	}

	public static List<Integer> parseConsensus( File html ) { 
		return parseConsensus(IO.readContentAsString(html));
	}
	
	public static List<Integer> parseConsensus( String html ) { 
		
		Map<String,Integer> convert = new HashMap<String,Integer>();
		convert.put("0", 0);
		convert.put("1", 1);
		convert.put("2", 2);
		convert.put("3", 3);
		convert.put("4", 4);
		convert.put("5", 5);
		convert.put("6", 6);
		convert.put("7", 7);
		convert.put("8", 8);
		convert.put("9", 9);
		convert.put("default", -1);
		convert.put("gap", -2);
		convert.put("ink", -3);
		
		List<Integer> result = new ArrayList<Integer>(10);
		
		Pattern patternRow = Pattern.compile("<span class=valuedefault>cons&nbsp;&nbsp;&nbsp;&nbsp;</span>(<span class=[^<>]+>[^<>]+</span>)+<br><br><br>");
		Pattern patternValue = Pattern.compile("<span class=value(\\d|default|gap|ink)>((?:&nbsp;|\\.|\\:|\\*)+)</span>");
		Matcher matcherRow = patternRow.matcher(html);

		while( matcherRow.find() ) { 
			String row = matcherRow.group(0);
			
			Matcher matcherValue = patternValue.matcher(row);
			while( matcherValue.find() ) { 
				String sVal = matcherValue.group(1);
				Integer val = convert.get(sVal);
				if( val == null ) { 
					Logger.warn("Unknow consensus class value: \"%s\"", sVal);
					continue;
				}
				
				String sItems = matcherValue.group(2).replace("&nbsp;","_"); 
				for( int i=0, c=sItems.length(); i<c; i++ ) { 
					result.add(val);
				}
			}
		}
		
		
		return result;
	}
	
}

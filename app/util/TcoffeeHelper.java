package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		List<Integer> result = new ArrayList<Integer>(10);
		
		Pattern REGION = Pattern.compile("[\\s\\S]*<span class=valuedefault>cons&nbsp;&nbsp;&nbsp;&nbsp;</span>(.+class=value\\d.+)+.+<br><br><br>");
		Matcher matcher = REGION.matcher(html);
		while( matcher.matches() ) { 
			System.out.println(matcher.group(1));
			System.out.println("end: " + matcher.regionEnd());
			html = html .substring(matcher.regionEnd());
			matcher = REGION.matcher(html);
		}
		
		
		return result;
	}
	
}

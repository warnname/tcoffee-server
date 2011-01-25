package util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.libs.IO;
import exception.QuickException;

public class TcoffeeHelper {
	
	public static class ResultHtml {
		public String style;
		public String body;
	}
	

	public static ResultHtml parseHtml( File file )  {
		try {
			return parseHtml(IO.readContentAsString(file));
		} catch (IOException e) {
			throw new QuickException(e, "Unable to parse html file: '%s'", file);
		}
	}
	
	public static ResultHtml parseHtml(String html) {
		Check.notNull(html, "Argument 'html' cannot be null");
		Pattern RE_TCOFFEE_HTML = Pattern.compile("^[\\s\\S]*<style>([\\s\\S]*)</style>[\\s\\S]*<body>([\\s\\S]*)</body>[\\s\\S]*$");
		ResultHtml result = null; 
		Matcher match = RE_TCOFFEE_HTML.matcher(html);
		if(match.matches()) {
			String style = match.group(1);
			String body = match.group(2);;

			/* 
			 * replace all '-' chars with &ndash;
			 */
			body = Utils.match(body, "(<[^>]+>)(\\-+)(</[^>]+>)", new Utils.MatchAction() {

				public String replace(List<String> groups) { 
					StringBuilder result = new StringBuilder();
					result.append(groups.get(1)); 					
					for( int i=0, c=groups.get(2).length(); i<c; i++ ) { 
						result.append("&ndash;");
					}
					result.append(groups.get(3));
					return result.toString();
				}
			});			
			
			/* remove the cpu time */
			body = body.replaceFirst("<span[^>]*>CPU&nbsp;TIME:[^<]*</span><br>","");
			
			/* final assignment */
			result = new ResultHtml();
			result.style = style;
			result.body = body;
			
		}
		
		return result;
	}


	
}

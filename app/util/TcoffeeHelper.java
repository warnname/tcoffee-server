package util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.libs.IO;
import exception.QuickException;

public class TcoffeeHelper {
	
	public static class ResultHtml {
		String style;
		String body;
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
			result = new ResultHtml();
			result.style = match.group(1);
			result.body = match.group(2);
		}
		
		return result;
	}

	
}

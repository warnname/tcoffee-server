package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.blackcoffee.commons.utils.FileIterator;
import org.blackcoffee.commons.utils.ReaderIterator;

import play.Logger;
import play.libs.IO;

/**
 * Utility class for T-Coffee related operations 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class TCoffeeHelper {
	
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
	
	/**
	 * Parse a T-coffee 'Template list' file. This file map 
	 * sequences to structures. An example is the following
	 * <pre>
	 * &gt;CO8A1  _P_ 1o91C
	 * &gt;TNFSF2  _P_ 3l9jT
	 * &gt;TNFSF4  _P_ 2hevF
	 * &gt;TNFSF13  _P_ 1xu2D
	 * :
	 * </pre>
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static List<String[]> parseTemplateList( File file )  
	{
		List<String[]> result = new ArrayList<String[]>();
		
		Reader reader;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		Iterator<String> it = new ReaderIterator(reader).iterator();
		while( it.hasNext() ) {
			String line = it.next();
			if( StringUtils.isEmpty(line) ) continue;
			line = line.replaceAll("  ", " ");
			
			String[] row = line.split("\\s");
			if( row == null || row.length != 3 ) {
				Logger.warn("Invalid Template_list row: '%s'", line);
				continue;
			} 
			
			for( int i=0,c=row.length; i<c; i++ ) {
				row[i] = row[i] != null ? row[i].trim() : "";
			}
			
			if( row[0].startsWith(">") ) {
				row[0] = row[0].substring(1).trim();
			}
			
			result.add(row);
		}
		
		return result;
		
	}
	
	public static String jsonConsensus( File file ) { 
		List<Integer> list = parseConsensus(IO.readContentAsString(file));
		
		boolean appendComma=false;
		StringBuilder result = new StringBuilder();
		result.append("[");
		for( int i=0; i<list.size(); i++ ) { 
			int value = list.get(i);
			if( value>=0 ) { 
				if( appendComma ) { result.append(","); }
				
 				result
 				.append("[") 
				.append(i+1) .append(",") 
				.append(value)
				.append("]");
 				
 				appendComma = true; // <-- append a comma on next iteration
			}
		}
		result.append("]");
		
		return result.toString();
	} 
	
	/**  
	 * Parse the result file produced by the "Strike" evaluation mode e.g. "t_coffee -other_pg strike <input_file>" 
	 * <p>
	 * The output is like the example below:
	 * <pre>
	 * Sequence	PDB	Score
	 * 1g41a   	1g41a	1.85
	 * 1e94e   	1e94e	1.59
	 * 1e32a   	1e32a	1.56
	 * 1d2na   	1d2na	1.46
	 * AVG     	-	1.62
	 * 
	 * 
	 * STRIKE out:
	 * </pre>
	 * <p>
	 * It contains an list of the "protein ID" altenate by the value evaluated by the strike method.
	 * The list is closed by the "AVG" average value	
	 * 
	 * @param file
	 * @return
	 */
	
	public static List<String[]> parseStrikeOutput( File file ) {
		
		List<String[]> result = new LinkedList<String[]>();
		

		boolean header = false;
		for( String line : new FileIterator(file) ) {
			if( StringUtils.isEmpty(line) ) {
				if( !header ) {
					// consume the blanks at the beginning of the file
					continue;
				}
				else{
					// a blank after an entry suppose to be reached the end of the <id, value> pairs
					break;
				}
			}
			
			/* 
			 *  check for the header
			 */
			String[] row = line.split("\\s+");
			
			if( row==null || row.length != 3 ) {
				Logger.warn("Invalid line '%s' (%d) in Strike result output: '%s'", line, row!=null?row.length:-1, file);
				continue;
			}
			
			/* 
			 * Look for the header
			 */
			if( !header && "Sequence".equals(row[0]) && "PDB".equals(row[1]) && "Score".equals(row[2])) {
				header = true;
				continue;
			} 

			// add th row to the result
			result.add(row);
		}
		
		
		return result;
		
	}
	
}

package bot;

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.blackcoffee.commons.utils.ReaderIterator;

import util.Utils;

public class InputContent { 
	
	static Pattern MODE_PATTERN = Pattern.compile("^\\[mode(?: |=)(\\S*)\\]");


	String mode;
	String sequences;
	String raw;
	
	
	public InputContent( String raw ) { 

		this.raw = raw;
		this.sequences = raw;
		
		for( String line : new ReaderIterator(new StringReader(raw))) { 
			this.mode = parseMode(line);
			if( this.mode != null ) { 
				this.sequences = Utils.ltrim( raw.substring(line.length()) );
			}

			break;
		}
		
	}
	
	static String parseMode( String value ) { 
		if( Utils.isEmpty(value)) { 
			return null;
		}
		
		String result = null;
		Matcher matcher = MODE_PATTERN.matcher(value);
		if( matcher.matches() ) { 
			result = matcher.group(1);
		}

		if( Utils.isNotEmpty(result) ) { 
			result = result.replace("-", "");
			result = result.replace("_", "");
			result = result.toLowerCase();
		}
		
		return result;
	}
	
	
}
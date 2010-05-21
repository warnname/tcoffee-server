package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import util.Check;
import util.Utils;
import exception.QuickException;

/**
 * Model the content of a FASTA file 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Fasta {
	
	static public class Sequence implements Serializable {
		
		/** The sequence header */
		String header;
		
		/** String value */
		String value;
		
		
		void parse( PushbackReader reader ) {
			StringBuilder block = new StringBuilder();   

			boolean stop;
			try {
				/* first line is defined as the header */
				header = readLine(reader, null);
				/* read the sequence block */
				int ch;
				String more;
				do {
					more = readLine(reader,VALID_CHARS);
					block.append(more);
					
					/* what's next ? */
					ch = reader.read();
					stop = !isValidChar((char)ch); 
					if( ch != -1 ) {
						/* pushback and continue reading */
						reader.unread(ch);
					}
					
				} 
				while( !stop );				
			}
			catch( IOException e ) {
				throw new QuickException(e, "Failure reading FASTA stream");
			}
			
			if( block.length()==0 ) {
				throw new QuickException("Empty sequence in FASTA block");
			}
			
			value = block.toString();
		}


		@Override
		public String toString() {
			return String.format(">%s|%s", header, value);
		}
		 
		/**
	     * Reads a line of text.  A line is considered to be terminated by any one
	     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
	     * followed immediately by a linefeed.
	     *
	     *
	     * @return     A String containing the contents of the line, not including
	     *             any line-termination characters, or null if the end of the
	     *             stream has been reached
		 * @throws IOException 
	     */
		String readLine(PushbackReader reader, char[] validChars) throws IOException {
			StringBuilder result = new StringBuilder();
			int ch;
			
			while( (ch=reader.read()) != -1) {
				if( ch != LINE_FEED && ch != CARRIAGE_RETURN ) {
					/* check if the read character is valid */
					if( validChars != null && !Utils.contains(validChars, (char)ch)) {
						throw new QuickException("Invalid character '%c' parsing line", ch); 
					}
					
					result.append((char)ch); // <-- LOOK at the cast!
				} 
				else {
					/* exit the loop 
					 * but before if we meet a CR - LF sequence */
					if( ch == CARRIAGE_RETURN && (ch=reader.read()) != LINE_FEED ) {
						reader.unread(ch);
					}
					break;
				}
			}

			
			return result.toString();
		}
	}
	

	/** The list of sequences */
	List<Sequence> sequences;
	
	/** The error message if parsing fail */
	String error; 
	
	/** The default constructor */
	public Fasta() {
		sequences = new ArrayList<Sequence>();
	}
	
	/**
	 * Constuct the object parsing the specified string as a FASTA format content
	 * 
	 * @param sequences the sequences in FASTA format 
	 */
	public Fasta( String sequences ) {
		this();
		parse( new PushbackReader(new StringReader(sequences)) );
	}

	public Fasta( File file ) throws FileNotFoundException {
		this();
		parse( new PushbackReader(new FileReader(file)) );
	}
	
	
	public boolean isEmpty() {
		return sequences == null || sequences.size() == 0;
	}
	
	public int count() {
		return sequences != null ? sequences.size() : 0;
	} 

	public int minLength() {
		if( sequences==null ) {
			return 0;
		}
		
		int min = Integer.MAX_VALUE;
		for( Sequence seq : sequences ) {
			if( seq.value!=null && min > seq.value.length() ) {
				min = seq.value.length();
			} 
		}
		
		return min!=Integer.MAX_VALUE ? min : 0;
	}
	
	public int maxLength() {
		if( sequences==null ) {
			return 0;
		}
		
		int max = 0;
		for( Sequence seq : sequences ) {
			if( seq.value!=null && max<seq.value.length() ) {
				max = seq.value.length();
			} 
		}
		
		return max;
	}
	
	void parse( PushbackReader reader ) {
		Check.notNull(reader, "Argument reader cannot be null");

		List<Sequence> result = new ArrayList<Sequence>();
		
		try {
			int ch;
			while( (ch=reader.read()) != -1 ) {
				if( ch == '>' ) {
					Sequence seq = new Sequence();
					seq.parse(reader);
					result.add(seq);
				}
			}
		}
		catch( Exception e ) {
			error = e.getMessage();
		}
		
		sequences = result;
	}
	
	public boolean isValid() {
		return !isEmpty() && minLength()>0;
	} 

	static boolean isValidChar( char ch ) {
		return Utils.contains(VALID_CHARS, ch);
	}
	
	/**
	 * @param file the file to be checked 
	 * @return <code>true</code> if the specified file a valid content in FASTA format 
	 */
	public static boolean isValid(File file) {
		try {
			return new Fasta(file).isValid();
		} 
		catch (FileNotFoundException e) {
			Logger.warn("Specified FASTA file does not exists: %s", file);
			return false;
		}
	}

	public static boolean isValid(String sequences) {
		return new Fasta(sequences).isValid();
	}
	

	private static final int LINE_FEED = '\n';
	
	private static final int CARRIAGE_RETURN = '\r';
	
	
	/** as defined http://www.ncbi.nlm.nih.gov/blast/fasta.shtml */
	static final char[] VALID_CHARS = { 
		'A',	// alanine 
		'B',	// aspartate or asparagine
		'C',	// cystine
		'D',	// aspartate
		'E',	// glutamate
		'F',	// phenylalanine
		'G',	// glycine
		'H',	// histidine
		'I',	// isoleucine
		'K',	// lysine
		'L',	// leucine
		'M',	// methionine
		'N',	// asparagine
		'P',	// proline
		'Q',	// glutamine
		'R',	// arginine
		'S',	// serine
		'T',	// threonine
		'U',	// selenocysteine
		'V',	// valine	
		'W',	// tryptophan
		'Y',	// tyrosine
		'Z',	// glutamate or glutamine
		'X',	// any   
		'*',	// translation stop
		'-'		// gap of indeterminate length
	};

}

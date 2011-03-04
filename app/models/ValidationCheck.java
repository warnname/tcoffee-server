package models;

import static models.ValidationFormat.*;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

import play.Logger;
import play.data.validation.EmailCheck;
import play.data.validation.Validation;
import plugins.AutoBean;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@AutoBean
@XStreamAlias("validation")
public class ValidationCheck implements Serializable {

	@XStreamAsAttribute
	public boolean required;

	/** Error message displayed when entered data does not match the specified {@link #required} constraint */
	@XStreamAlias("required-error")
	public String requiredError;
	
	
	/** 
	 * The field accepted format
	 * See {@link ValidationFormat}
	 */
	@XStreamAsAttribute
	public ValidationFormat format; 
	
	/** Error message displayed when entered data does not match the specified {@link format} */
	@XStreamAlias("format-error")
	public String formatError;
	
	/** 
	 * Sub-type for format 'FASTA'. Valida types are: 
	 * <li>amino-acid</li> (default)
	 * <li>nucleic-acid</li>
	 * <li>dna</li>
	 * <li>rna</li>
	 * 
	 */
	@XStreamAlias("type")
	public String type;
	
	/** minimum value accepted for numbers and date and string values */
	@XStreamAsAttribute
	public String min; 

	/** Error message displayed when entered data does not match the specified {@link #min} constraint */
	@XStreamAlias("min-error")
	public String minError;	
	
	/** maximum value accepted for numbers and date and string values */
	@XStreamAsAttribute
	public String max;
	
	/** Error message displayed when entered data does not match the specified {@link #max} constraint */
	@XStreamAlias("max-error")
	public String maxError;	
	
	/** A regular expression to match */
	@XStreamAsAttribute
	public String pattern; 

	/** Error message displayed when entered data does not match the specified {@link #pattern} constraint */
	@XStreamAlias("pattern-error")
	public String patternError;	
	
	
	/** Max number of sequences (only for FASTA format) */
	@XStreamAsAttribute
	@XStreamAlias("maxnum")
	public Integer maxNum;
	
	/** Error message displayed when entered data does not match the specified {@link #maxNum} constraint */
	@XStreamAlias("maxnum-error")
	public String maxNumError;	

	/** Max number of sequences (only for FASTA format) */
	@XStreamAsAttribute
	@XStreamAlias("minnum")
	public Integer minNum;
	
	/** Error message displayed when entered data does not match the specified {@link #maxNum} constraint */
	@XStreamAlias("minnum-error")
	public String minNumError;		
	
	/** Max length of sequence (only for FASTA format) */
	@XStreamAsAttribute
	@XStreamAlias("maxlen")
	public Integer maxLength;
	
	/** Error message displayed when entered data does not match the specified {@link #maxLength} constraint */
	@XStreamAlias("maxlen-error")
	public String maxLengthError;	

	/** Min length of sequence (only for FASTA format) */
	@XStreamAsAttribute
	@XStreamAlias("minlen")
	public Integer minLength;
	
	/** Error message displayed when entered data does not match the specified {@link #maxLength} constraint */
	@XStreamAlias("minlen-error")
	public String minLengthError;		
	
	/** 
	 * Default empty constructor 
	 */
	public ValidationCheck() { }
	

	
	Integer getMinAsInteger() {
		return Utils.parseInteger(min,null);
	}
	
	Integer getMaxAsInteger() {
		return Utils.parseInteger(max,null);
	}
	
	Date getMinDate() {
		return Utils.parseDate(min,null);
	}
	
	Date getMaxDate() {
		return Utils.parseDate(max,null);
	}
	
	Double getMinDecimal() {
		return Utils.parseDouble(min,null);
	}

	Double getMaxDecimal() {
		return Utils.parseDouble(max,null);
	}

	public void apply(String name, String value) {

		if( required && Utils.isEmpty(value)) {
			String message = Utils.isNotEmpty(requiredError) ? requiredError : "validation.required";
			Validation.addError(name, message, new String[] {name});		
		}

		
		if( TEXT.equals(format) ) {
			Integer min = getMinAsInteger();
			Integer max = getMaxAsInteger();
			if( min != null && ( value==null || value.trim().length()<min ) ) {
				String message = Utils.isNotEmpty(minError) ? minError : "validation.minSize";
				Validation.addError(name, message, new String[] {value});
			}
			
			if( max != null && ( value==null || value.trim().length()>max ) ) {
				String message = Utils.isNotEmpty(maxError) ? maxError : "validation.maxSize";
				Validation.addError(name, message, new String[] {value});
			}
			
			if( Utils.isNotEmpty(pattern) && !Pattern.matches(pattern, value) ) {
				String message = Utils.isNotEmpty(patternError) ? patternError : "validation.match";
				Validation.addError(name, message, new String[] {value});
			}
			

		}

		
		/*
		 * EMAIL FIELDS 
		 */
		else if( EMAIL.equals(format)  ) { 
		
			/* the value string can contains multiple addresses separated by a comma or a semicolon 
			 * split the string to check email address syntax one-by-one 
			 */
			String sEmail = value != null ? value : "";
			sEmail = sEmail.replace(",", ";"); // <-- normalize the comma 
			String[] addresses = sEmail.split(";");
			for( String addr : addresses ) { 
				addr = addr.trim();
				boolean isValid = new EmailCheck().isSatisfied(null, addr, null, null);
				if( !isValid ) {
					String message = Utils.isNotEmpty(formatError) ? formatError : "validation.email.format";
					Validation.addError(name, message, new String[] {addr});
					break;
				}
			}
			
			/* optional 'min' and 'max' attribute can be entered to specify the numeber of accepted email addresses  */
			Integer min = getMinAsInteger();
			Integer max = getMaxAsInteger();
			
			if( min != null && ( addresses.length < min ) ) {
				String message = Utils.isNotEmpty(minError) ? minError : "validation.minSize";
				Validation.addError(name, message, new String[] {value});
			}
			
			if( max != null && ( addresses.length > max ) ) {
				String message = Utils.isNotEmpty(maxError) ? maxError : "validation.maxSize";
				Validation.addError(name, message, new String[] {value});
			}
			
			
		}
		
		/*
		 * DATE  validation
		 */
		else if( DATE.equals(format) && Utils.isNotEmpty(value) ) {
			
			Date date = Utils.parseDate(value);
			if( date == null ) {
				String message = Utils.isNotEmpty(formatError) ? formatError : "validation.date.format";
				Validation.addError(name, message, new String[] {value});			
			}
		
			Date min = getMinDate();
			if( date!=null && min!=null && date.getTime() < min.getTime()) {
				String message = Utils.isNotEmpty(minError) ? minError : "validation.date.min";
				Validation.addError(name, message, new String[] {value});			
			}

			Date max = getMaxDate();
			if( date!=null && max!=null && date.getTime() > max.getTime()) {
				String message = Utils.isNotEmpty(maxError) ? maxError : "validation.date.max";
				Validation.addError(name, message, new String[] {value});			
			}

		}
		/* 
		 * INTEGER number validation
		 */
		else if( INTEGER.equals(format) && Utils.isNotEmpty(value)) {
			Integer num = Utils.parseInteger(value,null);
			if( num == null ) {
				String message = Utils.isNotEmpty(formatError) ? formatError : "validation.integer.format";
				Validation.addError(name, message, new String[] {value});			
			}
			
			Integer min = getMinAsInteger();
			if( min != null && num != null && num < min ) {
				String message = Utils.isNotEmpty(minError) ? minError : "validation.integer.min";
				Validation.addError(name, message, new String[] {value});			
			}
			
			Integer max = getMaxAsInteger();
			if( max != null && num != null && num > max ) {
				String message = Utils.isNotEmpty(maxError) ? maxError : "validation.integer.max";
				Validation.addError(name, message, new String[] {value});			
			}
			
		}
		/*
		 * DECIMAL number validation
		 */
		else if( DECIMAL.equals(format) && Utils.isNotEmpty(value) ) {
			Double num = Utils.parseDouble(value,null);
			if( num == null ) {
				String message = Utils.isNotEmpty(formatError) ? formatError : "validation.decimal.format";
				Validation.addError(name, message, new String[] {value});			
			}
			
			Double min = getMinDecimal();
			if( min != null && num != null && num < min ) {
				String message = Utils.isNotEmpty(minError) ? minError : "validation.decimal.min";
				Validation.addError(name, message, new String[] {value});			
			}
			
			Double max = getMaxDecimal();
			if( max != null && num != null && num > max ) {
				String message = Utils.isNotEmpty(maxError) ? maxError : "validation.decimal.max";
				Validation.addError(name, message, new String[] {value});			
			}
			
		}
		/*
		 * FASTA format validation
		 */
		else if( FASTA.equals(format) && Utils.isNotEmpty(value) ) {

			Fasta fasta;
			if( Utils.isEmpty(type) || "amino-acid".equalsIgnoreCase(type) )  { 
				 fasta = new Fasta(Fasta.AminoAcid.INSTANCE);		
			}
			else if( "nucleic-acid".equalsIgnoreCase(type) ) { 
				 fasta = new Fasta(Fasta.NucleicAcid.INSTANCE);		
			}
			else if( "dna".equalsIgnoreCase(type) ) { 
				 fasta = new Fasta(Fasta.Dna.INSTANCE);		
			}
			else if( "rna".equalsIgnoreCase(type) ) { 
				 fasta = new Fasta(Fasta.Rna.INSTANCE);		
			}
			else { 
				Logger.warn("Unknown fasta type '%s'. Using amino-acid format by default", type);
				 fasta = new Fasta(Fasta.AminoAcid.INSTANCE);		
			}
			
			/* parse the sequences */
			fasta.parse(value);
			
			/* check for validity */
            if ( !fasta.isValid() ) { 
				String message = Utils.isNotEmpty(formatError) ? formatError : "validation.fasta.format";
            	Validation.addError(name, message, new String[0]);
            } 
            else if( minNum != null && fasta.count()<minNum ) { 
				String message = Utils.isNotEmpty(minNumError) ? minNumError : "validation.fasta.minum";
            	Validation.addError(name, message, new String[0]);
            }
            else if( maxNum != null && fasta.count()>maxNum ) { 
				String message = Utils.isNotEmpty(maxNumError) ? maxNumError : "validation.fasta.maxnum";
            	Validation.addError(name, message, new String[0]);
            }
            else if( minLength != null && fasta.minLength()<minLength ) { 
				String message = Utils.isNotEmpty(minLengthError) ? minLengthError : "validation.fasta.minlen";
            	Validation.addError(name, message, new String[0]);
            }
            else if( maxLength != null && fasta.maxLength()>maxLength ) { 
				String message = Utils.isNotEmpty(maxLengthError) ? maxLengthError : "validation.fasta.maxlen";
            	Validation.addError(name, message, new String[0]);
            }
		} 
		
		
	}

}

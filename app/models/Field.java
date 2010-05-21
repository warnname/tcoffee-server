package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.libs.IO;
import play.mvc.Router;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import play.vfs.VirtualFile;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import converters.SimpleCollectionConverter;
import exception.QuickException;


@XStreamAlias("field")
public class Field {

	public static final List<String> TYPES = Arrays.asList(
			"text", 
			"hidden", 
			"password",
			"dropdown",
			"checkbox", 
			"radio", 
			"list", 
			"memo",
			"file"
			);
	
	@XStreamAsAttribute
	public String type;
	
	@XStreamAsAttribute
	public String name;
	
	public String value;
	
	@XStreamAsAttribute
	public String label;
	
	public String hint;
	
	public ValidationCheck validation;
	
	
	/** It could an URL (relative or absolute) to a file that can be used to preload the field */ 
	public String sample;
	
	/** the list of available choices, used only for types: <code>checkbox</code>, <code>radio</code>, <code>dropdown</code>, <code>list</code> */ 
	@XStreamConverter( SimpleCollectionConverter.class )
	public String[] choices;
	
	private @XStreamOmitField Map fHintsMap;
	
	private @XStreamOmitField byte[] fFileContent;
	private @XStreamOmitField File fFile;
	

	/**
	 * Field default constructor. Instantiate will all field to <code>null</code>
	 */
	public Field() {
		
	}

	/**!!
	 * Copy constructor. Create an instance equals to the one specified 
	 * 
	 * @param that the object from which copy
	 */
	public Field( Field that ) {
		this.type = Utils.copy(that.type);
		this.name = Utils.copy(that.name);
		this.value = Utils.copy(that.value);
		this.label = Utils.copy(that.label);
		this.hint = Utils.copy(that.hint);
		this.choices = Utils.copy(that.choices);
		this.validation = Utils.copy(that.validation);
		this.sample = that.sample;
	}
	
	public Field(final String type) {
		this.type = type;
	}
	
	public Field(final String type, final String name) {
		this.type = type;
		this.name = name;
	}

	public Field(final String type, final String name, final String value ) {
		this.type = type;
		this.name = name;
		this.value = value;
	}

	public void setType(String value) {
		if( value != null ) {
			value = value.toLowerCase();

			if( !TYPES.contains(value)) {
				throw new QuickException("Invalid field type: '%s'", value);
			}
		}
		
		this.type = value;
	}
	
	public String getLabel() {
		/* fallback on to name if label is not defined */
		return !Utils.isEmpty(label) ? label : name;
	}

	/**
	 * Binds this input object to the specified {@link Request} and {@link Params} instances. 
	 * In other words fetch the data from the http requests and populate the input model accordingly
	 *    
	 * @param request the current Play request 
	 * @param params the current Play request parameters 
	 */
	
	
	public void bind(Params params) {

		if( "checkbox".equalsIgnoreCase(type) ) {
			/* 
			 * all checkbox values with the same name are 'serialized' 
			 */
			value = Utils.asString(params.getAll(name), " ", "") ;
		}
		else if( "file".equals(type) ) {
			/* 
			 * manage 'file' type fields
			 */
			String filename = params.get(name);
			try {
				if( filename!=null && filename.startsWith("$ajaxupload:")) {
					filename = filename.substring("$ajaxupload:".length()).trim();
				}
				if( Utils.isNotEmpty(filename)) {
					this.value = filename;
					this.fFile = new File(filename);
					this.fFileContent = FileUtils.readFileToByteArray(fFile);
				}
			} catch (IOException e) {
				throw new QuickException(e, "Unable to read file content for field: '%s' for file: '%s'", name, filename);
			}
			
			
		}
		else {
			this.value = params.get(name);
			
			/* 
			 * when uploadeable is true the field content can be uploaded via ajax,
			 * when this happens this fields contains the reference to the uploaded 
			 * file. if so the the filename is prefixed with the string '$ajaxupload:'
			 */
			if( value!=null && value.startsWith("$ajaxupload:")) {
				String filename = value.substring("$ajaxupload:".length()).trim();
				try {
					value = FileUtils.readFileToString(new File(filename));
				} catch (IOException e) {
					throw new QuickException(e, "Unable to read upload content for field: '%s' from file: '%s'", name, filename );
				}
			}
		

		}
	}
	
	/**
	 * Validate current field value against specified validator (if exists)
	 */
	public void validate( ) {
		if( validation == null ) { 
			return;
		}
		
		validation.apply( name, value );
	}


	
	public String getSample() {
		if( sample == null ) return null;
		
		if( sample.startsWith("@") ) {
			VirtualFile vf = Play.getVirtualFile(sample.substring(1));
			return Router.reverse(vf);
		}
		
		return sample;
	}
	
	/**
	 * Parse the hint string has a map of hint. 
	 * The hint have to follow the following syntax: <code>choice-id: hint text</code>
	 * e.g.
	 * 
	 * <pre>
	 * &lt;hint&gt;
	 * score_html: blah blah
	 * fasta_aln: blah blah ..
	 * &lt;/hint&gt;
	 * </pre>
	 * 
	 * It is valid only for <code>checkbox</code> and <code>radio</code> fieldtypes
	 * 
	 * @return
	 */
	public Map getHintsMap() {
		if( fHintsMap!=null ) {
			return fHintsMap;
		}
		
		Map result = new HashMap<String, String>();
		if( Utils.isEmpty(hint) || !("checkbox".equals(type) || "radio".equals(type)) ) {
			return fHintsMap = result;
		}
		
		BufferedReader reader = new BufferedReader(new StringReader(hint.trim()));
		String line;
		try {
			while( (line=reader.readLine())!=null ) {
				int p=line.indexOf(':');
				if( p!=-1 ) {
					result.put(line.substring(0,p).trim(), line.substring(p+1).trim());
				}
			}
		} catch (IOException e) {
			throw new QuickException(e);
		}
		
		return fHintsMap = result;
		
	}
	
	/**
	 * The uploaded file binary content. Only for 'file' field type
	 */
	public byte[] getFileContent() {
		return fFileContent;
	} 

	/**
	 * The uploaded file reference. Only for 'file' field type
	 */
	public File getFile() {
		return fFile;
	}
	
	/**
	 * The uploaded file path as string. Only for 'file' field type
	 */
	public String getFilePath() {
		return fFile != null ? Utils.getCanonicalPath(fFile) : null;
	} 
	
	/**
	 * The name of the file uploaded. Only for 'file' field type
	 */
	public String getFileName() {
		return fFile != null ? fFile.getName() : null;
	}
	
	public boolean hasFile() {
		return fFile != null;
	} 
	
}

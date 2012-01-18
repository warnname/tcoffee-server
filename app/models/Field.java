package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.vfs.VirtualFile;
import util.Utils;
import bundle.BundleRegistry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import controllers.Data;
import converters.SimpleCollectionConverter;
import exception.QuickException;


@XStreamAlias("field")
public class Field implements Serializable {

	public static final List<String> TYPES = Arrays.asList(
			"text", 
			"textarea",
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
	
	private File fFile;
	
	
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
		this.fFile = that.fFile;
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
			 * field of type 'file' contains ALWAYS a reference (name) of a file 
			 * uploaded to the server using the FileChooser dialog 
			 */
			String filename = params.get(name);
			if( filename!=null && filename.startsWith("file://")) {
				filename = filename.substring("file://".length()).trim();
			}
			if( Utils.isNotEmpty(filename)) {
				this.value = filename;
				this.fFile = Data.getUserFile(filename);
			}
		}		
		
		else {
			this.value = params.get(name);
			
			if( StringUtils.isEmpty(this.value) ) { 
				return;
			}
			
			if( "memo".equals(type) ) {
				/*
				 * If the value starts with the prefix 'file://' it will read he content of the specified file
				 */
				if( value.toLowerCase().startsWith("file://") ) { 
					String filename = value.substring("file://".length()).trim();
					try {
						this.fFile = Data.getUserFile(filename);
						this.value = FileUtils.readFileToString(fFile);
					} 
					catch (IOException e) {
						throw new QuickException(e, "Unable to read upload content for field: '%s' from file: '%s'", name, filename );
					}
				}
			}  

			
			// normalize value removing trailing and leading blanks and special chars
			value = value.trim();

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
		
		/* replace the current value with the normalized one if any */
		if( validation.isValid() && validation.getNormalizedValue() != null ) { 
			value = validation.getNormalizedValue();
		}
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
	
	
	public boolean getUseTipTip() { 
		/* TODO find a better way instead of using the singleton */
		Bundle bundle = BundleRegistry.instance().get(Scope.Params.current().get("bundle"));
		String result = bundle != null ? bundle.properties.getProperty("ui.use.tiptip") : null;
		
		return "true".equalsIgnoreCase(result);
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
		return fFile != null ? fFile.getAbsolutePath() : null;
	} 
	
	/**
	 * The name of the file uploaded. Only for 'file' field type
	 */
	public String getFileName() {
		return fFile != null ? fFile.getName() : null;
	}
	
	public boolean hasFile() {
		return fFile != null && fFile.exists();
	}

	/**
	 * If the field has an upload file associated, this method will copy that 
	 * file the specified path, updating the file reference
	 * 
	 * @param path
	 */
	void consolidate(final File path) {
		if( !hasFile() ) return;

		File target = new File( path, fFile.getName() );

		if( target .equals( fFile ) ) { 
			Logger.debug("Source and target paths are the same, nothing to do: '%s'", target);
			return;
		}
		
		FileInputStream sIn = null;
		FileOutputStream sOut = null;
		try { 
			sIn = new FileInputStream(fFile);
			sOut = new FileOutputStream(target);
			IOUtils.copyLarge( sIn, sOut );
			
			// update the file attribute to the new location 
			fFile = target;
			// !! Important
			// When field 'type is 'file' the value attribute contains the 
			// absolute path to that path
			if( "file".equals(type) ) { 
				value = fFile.getAbsolutePath();
			}
		}
		catch( IOException e ) { 
			throw new QuickException(e, "Unable to copy file '%s' to '%s'", fFile, target);
		}
		finally { 
			try { sIn.close(); } catch( IOException e ) {};
			try { sOut.close(); } catch( IOException e ) {}
		}
		
	} 
	
}

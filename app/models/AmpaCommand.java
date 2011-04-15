package models;

import io.seq.Alphabet;
import io.seq.Fasta;
import io.seq.Sequence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import play.libs.IO;
import util.FileIterator;
import util.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import exception.QuickException;

/**
 * Implements a multi-fasta version for AMPA script 
 * 
 * @author Paolo Di Tommaso
 *
 */
@XStreamAlias("ampa")
public class AmpaCommand extends AbstractShellCommand {
	
	/** 
	 * The windows size to be used 
	 */
	@XStreamAsAttribute
	public Eval window;
	
	/**
	 * The Threshold value 
	 */
	@XStreamAsAttribute
	public Eval threshold;
	
	/**
	 * The multit
	 */
	@XStreamAsAttribute
	public Eval input;

	int numOfSequences;

	private Fasta fasta;

	private File fInput;

	public AmpaCommand( AmpaCommand that ) { 
		super(that);
		this.window = Utils.copy(that.window);
		this.threshold = Utils.copy(that.threshold);
		this.input = Utils.copy(that.input);
	}
	
	/**
	 * Produce the command line to run the AMPA script 
	 * 
	 * 
	 */
	@Override
	protected String onInitCommandLine(String cmdLine) {
		StringBuilder result = new StringBuilder();
		
		fInput = new File(ctxfolder, input.eval());
		fasta = new Fasta(Alphabet.AminoAcid.INSTANCE);
		try {
			fasta.parse(fInput);
		} 
		catch (FileNotFoundException e) {
			throw new QuickException(e, "Unable to read file: '%s'", fInput);
		}
		
		numOfSequences = fasta.count();
		for( int i=0; i<numOfSequences; i++) { 
			result.append( getCommandFor( fasta.sequences.get(i), i) ) .append("\n");
		}
		
		return result.toString();

	}

	File getInputFileFor( int index ) { 
		return new File(ctxfolder, String.format("input-%s.fasta", index+1));
	}

	File getDataFileFor( int index ) { 
		return new File(ctxfolder, String.format("data-%s.txt", index+1));
	}
	
	File getGraphFileFor( int index ) { 
		return new File(ctxfolder, String.format("graph-%s.png", index+1));
	}
	
	File getResultFileFor( int index ) { 
		return new File(ctxfolder, String.format("result-%s.text", index+1));
	}

	
	/*
	 * save the sequence in a separate file and create a command line for that 
	 */
	private String getCommandFor(Sequence seq, int index) {
	
		File input = getInputFileFor(index);
		IO.writeContent( seq.toString(), input);
		
		
		StringBuilder result = new StringBuilder()
			.append("AMPA.pl")
			.append(" -in=") .append(input.getName())
			.append(" -w=") .append(window.eval())
			.append(" -t=") .append(threshold.eval())
			.append(" -gf=") .append(getGraphFileFor(index).getName())
			.append(" -rf=") .append(getResultFileFor(index).getName())
			.append(" -df=") .append(getDataFileFor(index).getName())
			;
		
		
		return result.toString();
	}
	
	@Override
	protected boolean done(boolean success) {
		if( !success ) return false;
		
		File fResult = new File(ctxfolder, "result.txt");
		PrintWriter wResult;
		try {
			wResult = new PrintWriter( new FileWriter(fResult) );
		} catch (IOException e) {
			throw new QuickException(e, "Failing opening write stream to: '%s'", fResult);
		}
		
		File fData = new File(ctxfolder, "data.txt");
		PrintWriter wData;
		try {
			wData = new PrintWriter( new FileWriter(fData) );
		} catch (IOException e) {
			throw new QuickException(e, "Failing opening write stream to: '%s'", fData);
		}
		
		/* 
		 * parse all 'data' file to create flot json file 
		 */
		StringBuilder chartData = new StringBuilder();
		chartData.append("{");
		chartData.append("\"series\": [");
		for( int index=0; index<numOfSequences; index++ ) { 
			if( index>0 ) { chartData.append(","); }
			appendData(chartData, index);
			
			/* 
			 * merge all 'data'
			 */
			wData.append( IO.readContentAsString(getDataFileFor(index)) );
			wData.append("\n");
			
			/* 
			 * merge all 'result'
			 */
			wResult.append("# Protein: ") .append( fasta.sequences.get(index).header ) .append("\n");
			wResult.append( IO.readContentAsString(getResultFileFor(index)) );
			wResult.append("\n");
			
		}
		chartData.append("]}");
		
		/* 
		 * close writers
		 */
		wData.close();
		wResult.close();
		
		File chartFile = new File(ctxfolder,"graph.json");
		IO.writeContent(chartData, chartFile);
		
		/*
		 * append the user input object
		 */
		result.add(
			new OutItem(fInput,"input_file")
			.label("Input data file")
			.format("fasta"));		
		
		/*
		 * append item to return
		 */
		result.add(
				new OutItem(fResult,"result_file")
				.label("Result file")
				.format("txt"));		
		
		result.add(
				new OutItem(fData,"data_file")
				.label("Data file (txt format)")
				.format("txt"));		

		result.add( 
				new OutItem(chartFile,"data_file")
				.label("Chart Data (json format)")
				.format("json") );		

		/*
		 * add to the result object all the produced graph file 
		 */
		for( int index=0; index<numOfSequences; index++ ) { 
			result.add(new OutItem(getGraphFileFor(index),"graph_file").label("Graph file ("+ (index+1) + ")"));		
			
		}
		
		return true;
	}

	void appendData(StringBuilder json, int index) {
		
		String label = fasta.sequences.get(index).header;
		label = StringEscapeUtils.escapeJavaScript(label);
		json.append("{")
			.append("\"label\": ") .append("\"") .append(label) .append("\"")
			.append(", ")
			.append("\"data\": [");
		int i=0;
		for( String line : new FileIterator( getDataFileFor(index) )) { 
			if( line.startsWith("#") ) { continue; };
			if( i++>0 ) { json.append(","); }
			json.append("[") .append( parseLine(line) ) .append("]");
		}
		json.append("]");
		json.append("}");
		
	}

	static String parseLine(String line) {
		return line.replace('\t', ',');
	}
 
}

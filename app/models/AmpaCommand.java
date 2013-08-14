package models;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.blackcoffee.commons.format.Alphabet;
import org.blackcoffee.commons.format.Fasta;
import org.blackcoffee.commons.format.Sequence;
import org.blackcoffee.commons.utils.FileIterator;

import play.Logger;
import play.libs.IO;
import util.StringIterator;
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
	
	private int fMin;
	
	private int fMax; 
	
	private int fStretchesCount;

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
			.append(" -rf=") .append(getResultFileFor(index).getName())
			.append(" -df=") .append(getDataFileFor(index).getName())
			.append(" -noplot")
			;
		
		
		return result.toString();
	}
	
	@Override
	protected boolean done(boolean success) {
		if( !success ) return false;
		
		fMin = Integer.MAX_VALUE;
		fMax = 0;
		
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
		
		File fTabular = new File(ctxfolder, "data.csv");
		PrintWriter wTabular; 
		try {
			wTabular = new PrintWriter( new FileWriter(fTabular) );
		} catch (IOException e) {
			throw new QuickException(e, "Failing opening write stream to: '%s'", fTabular);
		}
		
		/* 
		 * parse all 'data' file to create flot json file 
		 */
		StringBuilder mainPlotData = new StringBuilder();
		mainPlotData.append("{");
		mainPlotData.append("\"series\": [");
		for( int index=0; index<numOfSequences; index++ ) { 
			/* 
			 * merge all 'data'
			 */
			wData.append( IO.readContentAsString(getDataFileFor(index)) );
			wData.append("\n");
			
			/* 
			 * merge all 'result'
			 */
			String resultFile = IO.readContentAsString(getResultFileFor(index));
			wResult.append(">") .append( fasta.sequences.get(index).header ) .append("\n");
			wResult.append(resultFile);
			wResult.append("\n");
			
			/* 
			 * parse to find out all stretches found 
			 */
			ResultData data = parseResult(resultFile, fasta.sequences.get(index));

			/*
			 * add the json object for this item 
			 */
			if( index>0 ) { mainPlotData.append(","); }
			appendData(mainPlotData, index, data);
			
			/*
			 * write csv file  
			 */
			for( String line : data.tabular ) {
				wTabular.println(line);
			}
			
			
		}
		mainPlotData.append("], ");
		
		
		/* add other meta data */
		mainPlotData 
			.append("\"meta\": {") 
						.append("\"min\": ") .append(fMin) .append(", ")
						.append("\"max\": ") .append(fMax) .append(", ")
						.append("\"threshold\": ") .append(threshold) .append(", ")
						.append("\"window\": ") .append(window) .append(", ")
						.append("\"nStretch\": ") .append(fStretchesCount) 
						.append(" }");
		
		mainPlotData.append("}");
		
		/* 
		 * close writers
		 */
		wData.close();
		wResult.close();
		wTabular.close();
		
		File chartFile = new File(ctxfolder,"graph.json");
		IO.writeContent(mainPlotData, chartFile);
		
		/*
		 * append the user input object
		 */
		ctx.result.add( new OutItem(fInput,"input_file") 
						.label("Input data file") 
						.format("fasta"));		
		
		/*
		 * append item to return
		 */
		ctx.result.add( new OutItem(fResult,"result_file")
						.label("Result file")
						.format("txt"));		
		
		ctx.result.add( new OutItem(fData,"data_file")
						.label("Data file (txt format)")
						.format("txt"));		

		ctx.result.add( new OutItem(chartFile,"data_file")
						.label("Chart Data (json format)")
						.format("json") );		

		ctx.result.add( new OutItem(fTabular,"data_file")
						.label("Tabular Data (csv format)")
						.format("csv") );
		
		return true;
	}

	
	static Pattern STRETCH_PATTERN = Pattern.compile("Antimicrobial stretch found in (\\d+) to (\\d+). Propensity value ([0-9\\.]+) \\((\\d+) %\\)");
	static Pattern MEAN_PATTERN = Pattern.compile("\\# This protein has a mean antimicrobial value of ([0-9\\.]+)");
	
	/**
	 * Parse an AMPA result file extracting al stretches position 
	 * 
	 */
	static class ResultData {
		String stretches; 
		String mean;
		List<String> tabular;
		
	} 
	
	static ResultData parseResult(String str, Sequence seq) {
		ResultData result = new ResultData();
		StringBuilder stretches = new StringBuilder();
		List<String> tabular = new ArrayList<String>();
		
		int index = 0;
		StringBuilder tabLine = new StringBuilder();
		for( String line : new StringIterator(str)) { 
			index++;
			Matcher matcher;
			/* try to match a streth */
			if( (matcher=STRETCH_PATTERN.matcher(line)).find() ) { 
				int a = NumberUtils.toInt(matcher.group(1), -1);
				int b = NumberUtils.toInt(matcher.group(2), -1);
				double c = NumberUtils.toDouble(matcher.group(3), -1);
				int d = NumberUtils.toInt(matcher.group(4), -1);
				if( a<0 || b<0 || c<0) { 
					Logger.warn("Invalid streatch value(s): %s to %s; propensity: %s; probability", a, b, c, d);
				}
				else { 
					if( stretches.length()>0 ) { stretches.append(","); }
					stretches.append("{") 
						.append("\"from\":") .append(a) .append(",") 
						.append("\"to\":") .append(b) .append(",")
						.append("\"propensity\":") .append(c) .append(",")
						.append("\"probability\":") .append(d) 
						.append("}");
					
					// add a line to the csv result
					String region = seq.value.substring(a-1,b);
					tabLine.setLength(0);
					tabLine.append(seq.header.replace(',', '_')).append(",")
							.append(index).append(",")
							.append(a).append(",")
							.append(b).append(",")
							.append(c).append(",")
							.append(d).append("%").append(",")
							.append(region);
					tabular.add( tabLine.toString() );
				}
			}
			/* try to match a 'mean' row */
			else if( (matcher=MEAN_PATTERN.matcher(line)).find() ) { 
				result.mean = matcher.group(1);
			}
		}
		
		// wrap in an array 
		stretches.insert(0, "[");
		stretches.append("]");
		result.stretches = stretches.toString();
		result.tabular = tabular;
		
		return result;
	}

	void appendData(StringBuilder json, int index, ResultData data) {
		
		String label = fasta.sequences.get(index).header;
		label = StringEscapeUtils.escapeJavaScript(label);
		json.append("{")
			.append("\"label\": ") .append("\"") .append(label) .append("\"") .append(", ")
			.append("\"stretch\": ") .append(data.stretches)  .append(",")
			.append("\"mean\": ") .append(data.mean)  .append(",")
			.append("\"seq\":") .append("\"").append( fasta.sequences.get(index).value ) .append("\",")
			.append("\"data\": [");
		int i=0;
		for( String line : new FileIterator( getDataFileFor(index) )) { 
			if( line.startsWith("#") ) { continue; };
			if( i++>0 ) { json.append(","); }
			json.append("[") .append( parseLine(line) ) .append("]");
		}
		json.append("]");
		json.append("}");
		
		// increment stretches count 
		fStretchesCount += data.stretches.length();
	}

	String parseLine(String line) {
		/* keep track of min / max values */
		String[] vals = line.split("\t");
		int x = vals!=null && vals.length>0 ? NumberUtils.toInt(vals[0], -1) : -1;
		if( x != -1 ) { 
			if( fMin>x ) { 
				fMin=x;
			}
			if( fMax<x ) { 
				fMax=x;
			}
		}
		
		/* return a comma separated pair value */
		return line.replace('\t', ',');
	}
 
}

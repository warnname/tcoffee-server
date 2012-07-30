import models.Field;
import models.OutItem;
import models.Service;
import play.Logger;
import play.libs.Time

/*
 * This script invoke the 'Piper' execution
 * The framework inject the following variables that are meant to 
 * be considered implicit object
 * - input: the data ented by the user 
 * - result: the object to which add the produced output
 * - context: the execution context object 
 */ 

def bundlePath = context['bundle.path']
def scratchFolder = new File(context['data.path'])
def formattedGenomesFolder = new File(context['settings.piper.formatted.genomes.path'])
def timeout = Time.parseDuration( context['settings.piper.max.duration'] ?: '1h' ) *1000
def cmdline = context['settings.piper.cmdline'] ?: ''

def availGenomesFile = context['settings.piper.all.genomes.file'] ?: 'dataset/all-genomes.txt'
availGenomesFile = availGenomesFile.startsWith('/') ? new File(availGenomesFile) : new File(bundlePath, availGenomesFile)

// The Piper pipeline scripts root can be specified by the variable 'settings.piper.pipeline.path'
// If the path specified is NOT absoulte, it is relative to the bundle path
// If not specified is will be the path 'pipeline' in the piper bundle
def piperFolder = context['settings.piper.pipeline.path'] ?: 'pipeline'
piperFolder = (piperFolder .startsWith('/')) ? new File(piperFolder) : new File(bundlePath, piperFolder)

// the hetmap script file 
def heatmapScriptFile = new File(piperFolder, "qualityCheck/utility/heatmap.R")

assert piperFolder.exists(), "The Piper scripts folder does not exist: '$piperFolder'"
assert scratchFolder.exists(), "The folder where the job should run does not exists: '$scratchFolder'"
assert heatmapScriptFile.exists(), "Cannot file 'heatmap' script: '$heatmapScriptFile'" 
assert availGenomesFile.exists(), "The file '${availGenomesFile}' does not exist"

Logger.debug "scratch-folder: $scratchFolder" 
Logger.debug "formatted-genomes-folder: ${formattedGenomesFolder}"
Logger.debug "piper-folder: ${piperFolder}"
Logger.debug "avail-genomes-file: ${availGenomesFile}"
Logger.debug "timeout: $timeout"
Logger.debug "cmdline: $cmdline"

/* 
 * THE USER INPUT ENTRIES 
 * 
 * - context.query is the query as provided by the user in the iput form 
 * - context.genomes the array of genomes selected by the user  
 */
File query = context.query
def genomes = context.genomes

assert query, 			"Missing query object"
assert query.exists(),  "Missing query file"
assert genomes, 		"Missing genomes input"


/*
 *  Create the 'genomes' file required by 'Piper' CLI containing the absolute path 
 *  to the selected genomes by the user.
 *  
 *  The 'genomes' field contains a blank (or comma) separated list of genomes. 
 *  For each of then is created an entry line in the 'genomes' file,
 *  each line is composed by two parts: the absolute genomes file name, plus 
 *  the file name itself used as entry handler 
 */
def gtext = new StringBuilder()
genomes.split(' |\\,') .each {
	def pattern = ~".+\\b${it}\$" 
	def line = availGenomesFile.readLines().grep(pattern)?.find({true})   // <-- strange bug w/o the '{true}' closure condition
	assert line, "Cannot find the genome fasta file for entry: '${it}'"
	// append to the buffer result
	gtext << line << '\n'
} 

def genomesFile = new File(scratchFolder, 'genomes')
genomesFile.text = gtext 


/*
 * prepare the 'allGenomesInfo' folder structure
 */
if( !formattedGenomesFolder.exists() ) formattedGenomesFolder.mkdirs()
def allGenomeInfoFolder = new File(scratchFolder,"allGenomeInfo")
assert allGenomeInfoFolder.mkdir(), "Cannot create 'allGenomeInfo folder at '$allGenomeInfoFolder'"

genomesFile.eachLine { String line ->
   def items = line.split('\\s+')
   assert items?.length == 2,  "Invalid entry '$line' in dataset file '$genomesFile'"
   
   // check that the referenced genome file exists
   def genomeFile = new File(items[0])
   assert genomeFile.exists(), "Missing genome file: '$genomeFile'"
   
   // create a link to the dbformat cache
   def alias = items[1]
   assert alias, "Missing alias for genome: '$genomeFile'"
   
   def cacheFolder = new File(formattedGenomesFolder,alias)
   assert cacheFolder.exists() || cacheFolder.mkdirs(), "Cannot create folder: '$cacheFolder'"

   "ln -s ${cacheFolder} ${alias}".execute(null, allGenomeInfoFolder)

}



/*
 * Launch the pipeline
 */

def exports = ""
Service.current().defaultEnvironment()?.each { key,value ->
	exports += "export $key=\"$value\"\n"	
}
// append the piper scripts folder 
exports += "export PATH=\"${piperFolder}\":\$PATH"

new File(scratchFolder,"run.sh").text = 
"""\
#!/bin/sh
set -e
${exports}
[ \$TMP_4_TCOFFEE ] && mkdir -p \$TMP_4_TCOFFEE
[ \$LOCKDIR_4_TCOFFEE ] && mkdir -p \$LOCKDIR_4_TCOFFEE
[ \$CACHE_4_TCOFFEE ] && mkdir -p \$CACHE_4_TCOFFEE
startPipeline.pl -genomes ${genomesFile} -query ${query} -experiment exp_1 -blast_strategy abblastn -blast blastn; 
executePipeline.pl -step similarity -experiment exp_1 -pipeline_dir . 
"""


"chmod +x ./run.sh".execute(null,scratchFolder)
		
def proc = new ProcessBuilder("sh", "-c", "$cmdline ./run.sh".toString())
		.directory(scratchFolder)
		.redirectErrorStream(true)
		.start()
		
def outFile = new File(scratchFolder,"pipeline.out")
Thread.start { outFile << proc.getInputStream() }
// wait for the pipeline termination and get the process exit-code (non zero --> error )
def exit = timeout ? proc.waitForOrKill(timeout) : proc.waitFor()

/*
 * folder that contains the pipeline result files 
 */
def experimentFolder = new File (scratchFolder, 'experiments/exp_1');


/*
 * Create the result object
 */
result.add( new OutItem(query,"input_file").label("Input sequences") );

if( outFile.exists() ) {
	result.add(new OutItem(outFile, "system_file"))	
}
else {
	Logger.warn "Missing script output file: '$outFile'"
}

if( exit ) return exit

/* 
 * Extra step to create the Heatmap 
 * 1) Check that exists the expected files generated by the pipeline
 * 2) Run the script. NOTE: the script MUST run in the folder where the 'simMatrix.csv' file is located  
 * 3) check the png exists and return the heatmap file
 */
def simMatrixFile = new File(experimentFolder,"simMatrix.csv")

// 1
assert experimentFolder.exists(), "The pipeline experiments path does not exist: '$experimentFolder'"
assert simMatrixFile.exists(), "The similarity matrix file does not exist: '$simMatrixFile'"

// 2 
// add the similary matrix to the result set 
result.add( new OutItem(simMatrixFile, "matrix_file") )

// 3
def cmd = ["R","CMD", "BATCH", heatmapScriptFile.toString()]
def r_proc = new ProcessBuilder(cmd).directory(experimentFolder).start()
exit = timeout ? r_proc.waitForOrKill(timeout) : r_proc.waitFor()

// 3
def heatmapFile = new File(experimentFolder,"heatMap.png")
if( heatmapFile.exists() ) {
	result.add(new OutItem(heatmapFile, "heatmap_file"))
}
else {
	Logger.warn "Piper: Missing heatmap file: $heatmapFile"
}

/*
 * terminated
 */
return exit




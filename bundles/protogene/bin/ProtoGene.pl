#!/usr/bin/env perl

########################################################
#                                                      #
# ProtoGene : Bona-Fide Back Translation of Protein    #
#             Multiple Sequence Alignments             #
#                                                      #
########################################################

use strict;
use warnings;
use diagnostics;
use Carp;

use Env qw(HOME);                          # Use only environmental (shell) HOME variable
use File::Basename;
use File::Copy qw(move);                   # Avoid external 'mv' command usage
use LWP::Simple;                           # To test gigablaster availability

#use lib '/Users/ptommaso/Downloads/protogene/';    # Local path for ProtoGene's own perl modules
#use lib '/mnt/local/lib/tcoffee_perl/';
use Getopt::Long;                          # For options specifications
use File::Which qw(which);                 # Locate external executable programs in the PATH
#use Time::Format qw(%time);               # Use local time for part of pseudo-uniq temp file name
#use Mail::Send;                            # Send warnings and errors files by e-mail ==> only if the $userEMail variable is defined

use loci_from_Exonerate;                   # Exonerate parser
use Views;                                 # Non-text outputs, e.g. HTML/CSS
#use output_checker;                       # Check output for cds consistancy with query



############## Specific to our server
#$ENV{'PATH'} .= ':/mnt/local/bin/'; # additional path for executable on the server
##############



my $Version  = '3.4.4';
my $uct      = 15;                                                      # UpdateCacheThreshold: number of days before update
my $cachedir = '/scratch/cluster/monthly/t_coffee/ProtoGene_Cache';     # Cache directory
if( exists $ENV{PROTOGENE_CACHE}) { 
	$cachedir = $ENV{PROTOGENE_CACHE};
}

##### User settings ####################################
#my $userEMail    = 'paolo.ditommaso@gmail.com';      # To receive an e-mail with encountered problems; leave blank to inactive this option
my $webblast_exe = which('webblast.pl')   || '';       #
my $blast_prog   = 'blastall';                         # Or wu-blastall for Wu-BLAST; for local blast usage
my $exonerate    = which('exonerate-1.0') || '';       #
########################################################
my $blast_exe = which($blast_prog) || '';
my $doc       = which('perldoc')   || '';
#my $date = $time{'yy-mm-dd_hh\hmm-ss.mmm'};   #Used to get a unique file
my $date  = `/usr/bin/env date +%y-%m-%d_%T`; #Used to get a unique file
chomp($date);
$date =~ s/\:/\-/g;

checkProgramsPresence($webblast_exe, $exonerate); # Check external programs presence in the PATH
$blast_exe =~ s/${blast_prog}$//;

checkCacheAccessibility($cachedir); # Check cache directory accessibility



##Options management
my ($msa, $species, $db, $athome, $revtrans, $pep, $hideBOJ, $debug)   = ('', 'All_organisms', 'refseq_protein', 0, 0, 0, 0, 0);
my ($version, $help, $run_name, $Cache, $template, $lim, $tmp) = (0, 0, '', 'update', '', 0, 0);
my $giga = 0;
GetOptions("msa|in=s"       => \$msa,        #Input sequences
           "orgm|species=s" => \$species,    #Organism(s) to blast against
           "db|database=s"  => \$db,         #Database to blast against
           "local"          => \$athome,     #Use to specify a local db query, definied in $local_db
           "giga"           => \$giga,       #Use GigaBlaster server
           "revtrans"       => \$revtrans,   #Use to reverse-translate sequences with no match
           "pep"            => \$pep,        #Add the original peptide query beneath the linked nt seq
           "version|v"      => \$version,    #Print version information
           "help|h"         => \$help,       #Print full help message
           "run_name=s"     => \$run_name,   #Use another name, instead of input seq name, for result files
           "cache=s"        => \$Cache,      #Specify what cache parameter
           "hideBOJ"        => \$hideBOJ,
           "debug"          => \$debug,      #Verbose output
           "template=s"     => \$template,   #Use a template file
           "lim=i"          => \$lim,        #Limite number of input query sequences
           "tmp"            => \$tmp,        #To keep traces of fake intermediate files like fake xml from NCBI, fake aln, ...
           );
$athome = 2 if ( $giga==1 );
if ( $version==1 ){
    print "\n\tPROTOGENE : Bona-Fide Back Translation of Protein Multiple Sequence Alignments\n\tversion   : $Version\n\n";
    exit 0;
}
if ( $help==1 ){
    if ( $doc ne '' ){
        system("$doc $0");
    }
    else{
        print {*STDERR} "\n\tperldoc command doesn't seem to be available\n\tto see the documentation in pod format\n\n";
    }
    exit(1);
}
if ( $blast_exe eq '' && $version==0 && $help==0 ){
    print {*STDERR} "\n\t$blast_prog program is not reachable\n\tIt could not be in your PATH or not installed\n\tIt is required only for local blast searches\n\n";
}

#Cache management
my $cache = cacheManagement($Cache) || '.';


#Open and check template file
my ($templG, $templP, $templSeq);
($templG, $templP, $templSeq) = checkTemplate($template) if ($template);


#Short help message
if ( $msa eq '' || $species eq '' || $db eq '' || $cache eq '' ){
    my $appli = basename($0);
    print {*STDERR} "\n\tCannot open the MSA file in FASTA format
\tTry:  $appli --msa=path_of_the_fasta_msa_file [Options]

\tOptions: --orgm=All_organisms, Bacteria, Viruses, Vertebrata,
\t                Eukaryota, Mammalia, Primates, Homo_sapiens,
\t                Gallus_gallus, Bos_taurus, Escherichia_coli,
\t                Arabidopsis_thaliana, Mus_musculus,
\t                Drosophila_Melanogaster, ...
\t                default is 'All_organisms'
\t         --db=nr, pdb, swissprot, refseq_protein
\t                default is 'refseq_protein'
\t         --local to execute a local BLAST query with
\t                --db=path_for_a_local_db_blast_formated
\t         --template to provide your own nucleotidic sequences
\t                following the cds file format
\t         --revtrans to reverse-translate sequences with no
\t                blast hit, in IUB (IUPAC) depiction code
\t                They are removed from the alignement by default
\t         --pep  to add the original peptide query beneath the
\t                back-translated sequence
\t         --cache=none, update, use, own_PATH_directory, old, empty
\t                to select the cache mode
\t                default is 'update'
\t         --version to print version information
\t         --help to print a full help message\n\n";
    exit(1);
}



##Open and Check the fasta file
my $originalMSA   = $msa;
my $change        = 0;
($msa, $change)   = checkFastaFile($msa, $change);
my $fasta_checker = -1;
my (@input_order, @original_names, @original_seq); #Use 3 lists in parallel : original_names, seq, order(1->n)
open( my $MSA, '<', "$msa" ) if ( -e "$msa" );
while(<$MSA>){
    if ( $_ =~ /^>/ ){
        $fasta_checker++;
        my $name = $_;
        $name    =~ s/\r\n//g; #Remove return lines from windows OS '^M'
        $name    =~ s/^>[ \t]+/>/;
        chomp($name);
        @original_names = (@original_names, $name);
        @input_order    = (@input_order, $fasta_checker);
    }
    elsif ( $_ !~ /^>/ ){
        my $seqq = $_;
        $seqq    =~ s/\r\n//g;
        $seqq    =~ s/\./-/g; #for msa with '.' as gap
        $seqq    =~ s{[BJOUZ]}{X}ig; #U here for selenocystein
        $seqq    =~ s/[^A-Za-z\-\*\n\r]//g; #Remove all the non-gap or non-alphabetic characters from the seq
        chomp($seqq);
        #fasta sequence on 1 line
        if ( !exists($original_seq[$fasta_checker]) ){
            @original_seq = (@original_seq, $seqq);
        }
        else {
            $original_seq[$fasta_checker] = $original_seq[$fasta_checker].$seqq;
        }
    }
}
close $MSA;
if ( $fasta_checker == -1 ){
    failure();
    print {*STDERR} "\tThe MSA file does NOT seem to be a protein FASTA format\n\tSee <a href='http://en.wikipedia.org/wiki/FASTA_format' target='_blank'>FASTA format</a>\n\n";
    exit(1);
}
elsif ( $lim >0 && $fasta_checker > $lim ){
    failure();
    print {*STDERR} "\tThe FASTA file is too large, try with less than $lim sequences\n\tor split your file\n\n";
    exit(1);
}
elsif ( exists( $original_seq[0] ) && $original_seq[0] =~ /[acgtu]/i ){
    my $first_seq = $original_seq[0];
    #Check if sequences are amino acids and not nucleotides
    my $a   = ($first_seq =~ s/[aA]//g)   || 0;
    my $c   = ($first_seq =~ s/[cC]//g)   || 0;
    my $g   = ($first_seq =~ s/[gG]//g)   || 0;
    my $t   = ($first_seq =~ s/[tTuU]//g) || 0;
    my $non = ($first_seq =~ s/[^aAcCgGtTuUXxNn-]//g) || 0;
    if ( ($a+$c+$g+$t) >= (($a+$c+$g+$t+$non)*80/100) ){
        failure();
        print {*STDERR} "\tYour sequences seem already to be nucleotides\n\tthis program purpose is to turn AMINO ACID alignments into CDS nucleotide alignments\n\n";
        exit(1);
    }
}
undef $fasta_checker;


#Check GigaBlaster status if used
if ( $giga==1 ){
    if ( head('http://www.igs.cnrs-mrs.fr/Giga2/~database/remoteblast.cgi') ){
    }
    else{
        $athome = 0;
    }
}


#Start main program with version # of programs and list original queries
print "\n\t   Protogene\t$Version\t$date\n\n";
open(my $EXONERATEISHERE, "$exonerate --version |");
my $IsExonerateHere = <$EXONERATEISHERE>;
close $EXONERATEISHERE;
print "\t   $IsExonerateHere\n";
undef $IsExonerateHere;
undef $Version;
for(my $m=0; $m<=$#original_names; $m++){
    print "\t$original_names[$m]\n";
}
$date .= "_$$"; #Add PID



# run_name implementation for web server
$originalMSA = $run_name if ( $run_name ne '' );

unlink("${originalMSA}.cds");
unlink("${originalMSA}.cdsP");
unlink("${originalMSA}.cdsP.html");
unlink("${originalMSA}.out");
unlink("${originalMSA}.boj");



# Remove old files from the cache directory
cacheManagement('old', '1'); #everytime ProtoGene is running




##Build the sequences, from the alignment, to perform webblast
my $quiet = '';
EACH_SEQ:
for(my $r=0; $r<=$#input_order; $r++){
    my $right_name = $original_names[$r];
    my $templ_name = $original_names[$r];
    $right_name    =~ s/^>//;
    $templ_name    =~ s/^([^\s\t]+).*$/$1/;

    open(my $READY2BLAST, '>', "$cache/${date}_seq2blast.fas${r}");
    my $noGap = $original_seq[$r];
    $noGap    =~ s/[\.-]//g; #remove gaps
    print {$READY2BLAST} ">$input_order[$r]\n$noGap\n";
    close $READY2BLAST;
    undef $noGap;


    my @multipleequalblasthits;
    if ( exists($templG->{$templ_name}) && $templG->{$templ_name} ne '' ){
        @multipleequalblasthits = (@multipleequalblasthits, 'My_own_seq'); #To change for ?
        print "\n\n$r done\n\nNucleotidic template for $r:\t$templG->{$templ_name}\n\n**********************************************************************\n\n";
    }
    elsif ( exists($templP->{$templ_name}) && $templP->{$templ_name} ne '' ){
        @multipleequalblasthits = (@multipleequalblasthits, $templP->{$templ_name});
        print "\n\n$r done\n\nBlastP template for $r:\t$templP->{$templ_name}\n\n**********************************************************************\n\n";
    }
    else{
    ##   --> send them to webblast
        $quiet = '-quiet=on ' if ( $r>0 ); #Show webblast parameters only for the first webblast run
        launchWebblast("$cache/${date}_seq2blast.fas${r}", $quiet, $athome, $db, $species, $blast_exe, "$cache/${date}.blastp${r}");

    ##Get the BLASTp hit(s)  acc number
        open(my $BLASTP, '<', "${cache}/${date}.blastp${r}");
        while(<$BLASTP>){
            @multipleequalblasthits = (@multipleequalblasthits, $1) if ( $_ =~ /^>\s*\d+@?\w?\w?\w?__([^\s\|\.]+).*$/ ); #Warning: double '_'
        }
        close $BLASTP;
        unlink("$cache/${date}.blastp${r}", "${cache}/${date}_seq2blast.fas${r}") if ( $tmp == 0 || exists($multipleequalblasthits[0]) );
        if ( !exists($multipleequalblasthits[0]) ){
            print "\n$input_order[$r] ...\tNo blast result found above filter thresholds for $right_name\n\n";
            buildFailureOutputFiles($r, 'No_BLASTp_Result', 'Unavailable', '');
            undef $original_seq[$r];
            undef $original_names[$r];
            undef $input_order[$r];
            next EACH_SEQ;
        }
    }



    #When multiple equal blast hits  ==> use, and add, every hits
    my ($resultPOS, $resultBOJ) = ('', '');
    my @failureStatus = ('');
    my @intronStatus  = ('');
    for(my $qq=0; $qq<=$#multipleequalblasthits; $qq++){
        print "\n${right_name}  -->  [${multipleequalblasthits[$qq]}]\n";


        my @nt_GIs;
        my $intronStep = 0;
        if ( exists($templG->{$templ_name}) && $templG->{$templ_name} ne '' && $templG->{$templ_name} !~ /^My_Seq$/i ){
            downloadSeq($cache, $date, '', '', $templG->{$templ_name});
            @nt_GIs = $templG->{$templ_name};
        }
        elsif ( exists($templG->{$templ_name}) && $templG->{$templ_name} ne '' && $templG->{$templ_name} =~ /^My_Seq$/i ){
            open(my $MYSEQ, '>', "$cache/My_Seq-$$.fas");
            print {$MYSEQ} ">My_Seq-$$ for $templ_name\n".$templSeq->{$templ_name}."\n";
            close $MYSEQ;
            @nt_GIs = "My_Seq-$$";
        }
        else{
        #Prot ACC -> PUID
            my $protGI = blastPAcc2PGI($multipleequalblasthits[$qq]);
            if ( $protGI eq '' ){
                print "\tNo protein (prot GI) link found for ${multipleequalblasthits[$qq]} in $right_name\n\n";
                @failureStatus = ('PUI_unavailable', ${multipleequalblasthits[$qq]});
                next;
            }
#           print "\t$protGI\n";

        #PUID -> nt GIs & GeneID
            my ($ntGIs, $geneID) = protGI2NTGIs($protGI, $multipleequalblasthits[$qq]);
            if ( $ntGIs eq '' && $geneID eq '' ){
                print "\tNo nucleotide (nt GIs) link found for ${multipleequalblasthits[$qq]} in $right_name\n\n";
                @failureStatus = ('No_nt_link', ${multipleequalblasthits[$qq]});
                next;
            }
            print " linked with [$ntGIs $geneID]\n";


        #Get transcript and contig seq
            @nt_GIs = split(/,/, $ntGIs);
            downloadSeqFromGIs($cache, $date, '', '', @nt_GIs);
        #GeneID -> Chr
            my @geneIDs = split(/,/, $geneID);
            while(<@geneIDs>){
                my $geneID = $_;
                my ($chr, $amont, $aval) = geneID2Chr($geneID, $multipleequalblasthits[$qq]);
                print "\n\tNo gene locus found for ${multipleequalblasthits[$qq]} with $geneID in $right_name\n\n" if ($chr eq '' and $geneID ne '');

                if ( $chr ne '' ){
            #Get Chr seq
                    downloadSeq($cache, $date, $amont, $aval, $chr);
                    $intronStep = 1;

                    @nt_GIs = ("$chr:$amont-$aval", @nt_GIs);
                }
            }
        }


    #Align Nt seq with our protein query seq
        my ($POS, $BOJ) = runExonerate($exonerate, $cache, $date, ${input_order[$r]}, ${original_seq[$r]}, $right_name, $multipleequalblasthits[$qq], $r, @nt_GIs);

        my ($POSresult, $bestNucleotide) = prepareResults4CDS($POS, $multipleequalblasthits[$qq], ${original_seq[$r]}, $right_name);
        $resultPOS .= $POSresult if ( $bestNucleotide ne '' && $POSresult ne '' && $resultPOS !~ /_G_$bestNucleotide/ );

        @intronStatus = ($POS->{0}, ${multipleequalblasthits[$qq]}, '')              if ( $intronStep==1 && exists($POS->{0}) && $POS->{0} ne '' && $POS->{0} =~ /\:/ );
        @intronStatus = ($POS->{0}, ${multipleequalblasthits[$qq]}, $bestNucleotide) if ( $intronStep==0 && ($bestNucleotide =~ /^N[CTWZG]_/ || $bestNucleotide =~ /^AC_/) );

        my ($intronLess, $nameLess) = ('', '');
        $intronLess = $POS->{0}       if ( %$BOJ eq 0 && exists($POS->{0}) && $POS->{0} ne '' );
        $nameLess   = $bestNucleotide if ( %$BOJ eq 0 && $intronStep==0 && ($bestNucleotide =~ /^N[CTWZG]_/ || $bestNucleotide =~ /^AC_/) );
        my ($BOJresult, $bestGenomic) = prepareResults4BOJ($BOJ, $multipleequalblasthits[$qq], ${original_seq[$r]}, $right_name, $intronLess, $nameLess);
        $resultBOJ .= $BOJresult      if ( $bestGenomic ne '' && $BOJresult ne '' && $resultBOJ !~ /_G_$bestGenomic[ \-]/ );
#    print " $intronStep $POS->{0} [@intronStatus] {$BOJresult} $bestNucleotide - $bestGenomic \t $BOJ->{0}\n";
    }


    if ( $resultPOS eq '' ){
        buildFailureOutputFiles($r, $failureStatus[1], $failureStatus[0], '')     if ( $failureStatus[0] ne '' );
        buildFailureOutputFiles($r, $multipleequalblasthits[0], 'Failed_Aln', '') if ( $failureStatus[0] eq '' );
        next;
    }
    elsif ( $resultBOJ eq '' ){
        if ( $failureStatus[0] eq 'PUI_unavailable' || $failureStatus[0] eq 'No_nt_link' ){
            buildFailureBOJOutputFile($failureStatus[1], $failureStatus[0], $right_name, ${original_seq[$r]});
        }
        elsif ( $failureStatus[0] eq '' && $intronStatus[0] ne '' ){
            buildIntronlessBOJOutputFile($intronStatus[1], $intronStatus[0], $intronStatus[2], $right_name, ${original_seq[$r]});
        }
        else{
            buildFailureBOJOutputFile($multipleequalblasthits[0], 'Unavailable', $right_name, ${original_seq[$r]});
        }
        createCDSOutputFile($resultPOS, ${original_seq[$r]}, $right_name);
    }
    else {
        createCDSOutputFile($resultPOS, ${original_seq[$r]}, $right_name);
        createBOJOutputFile($resultBOJ, ${original_seq[$r]});
    }


    undef $original_seq[$r];
    undef $original_names[$r];
    undef $input_order[$r];
}


checkAndCleanStderrFiles("$cache/${date}_ExonerateError") if ( -e "$cache/${date}_ExonerateError" );
checkOutputFiles();
unlink("$msa") if ( $change==1 );
my @tmpFiles = glob($cache."/${date}*");
if ( exists($tmpFiles[0]) ){
    mkdir("$cache/${date}_TMP");
    move("$_", "$cache/${date}_TMP") while(<@tmpFiles>);
}


#Ouput Checker


#STDOUT Log output for our server
if ( -s "${originalMSA}.cds" || -s "${originalMSA}.out"){
    print "\n\nTERMINATION STATUS: SUCCESS\n";
    print "OUTPUT RESULTS\n";
    print "    #### File Type=        MSA Format= fasta_CDS Name= ${originalMSA}.cds\n" 
        if ( -s "${originalMSA}.cds");
    print "    #### File Type=        MSA Format= fasta_CDS+Query Name= ${originalMSA}.cdsP\n" 
        if ( -s "${originalMSA}.cdsP");
    print "    #### File Type=        MSA Format= colored_fasta_CDS Name= ${originalMSA}.cdsP.html\n" 
        if ( Views::Html("${originalMSA}.cdsP") && -s "${originalMSA}.cdsP.html");
    print "    #### File Type=        MSA Format= Rejected_seq Name= ${originalMSA}.out\n" 
        if ( -s "${originalMSA}.out");
    print "    #### File Type=        MSA Format= fasta_Exon-Boundaries Name= ${originalMSA}.boj\n" 
        if ( -s "${originalMSA}.boj" && -s "${originalMSA}.cds" && $hideBOJ==0);
}
else{
    failure();
    exit(1);
}

exit(0);


=head1 NAME

ProtoGene.pl - Converts a peptidic alignment to a nucleotidic alignment through database search

=head1 SYNOPSIS

B<ProtoGene.pl> --msa=path_of_the_fasta_msa_file [options]

with these options available:

=over 4

=item I<--orgm>=targeted species in the database search

=item All_organisms [default], Bacteria, Viruses, Vertebrata, Eukaryota, Mammalia, Primates, Homo_sapiens, Gallus_gallus, Bos_taurus, Escherichia_coli, Arabidopsis_thaliana, Mus_musculus, Drosophila_Melanogaster, ...

=item I<--db>=targeted database in the database search

=item nr, pdb, swissprot, refseq_protein [default]

=item I<--local> to execute a local BLAST query with  --db=path_for_a_local_db_blast_formated

=item I<--template> to provide your own nucleotidic sequences following the cds file format

=item I<--revtrans> to reverse-translate sequences with no blast hit, in IUB (IUPAC) depiction code

=item They are removed from the alignement by default

=item I<--pep> to add the original peptide query beneath the back-translated sequence

=item I<--cache>=none, update, use, own_path_directory, old, empty

=item none: no cache usage, none temporary files are stored

=item update: use cache but update old files [default]

=item use: force use cache, whatever the age of files

=item own_path_directory: use my own directory, and its files

=item old: remove the old files in the cache directory

=item empty: empty the whole cache directory $HOME/.ProtoGene/

=item I<--version> to print version information

=item I<--help> to print this help message

=back

=head1 DESCRIPTION

PROTOGENE : Bona-Fide Back Translation of Protein Multiple Sequence Alignments
It converts a peptidic alignment to a nucleotidic alignment through database search.
Blast queries allow to get nucleotidic sequences from where peptidic sequences come from.
Exonerate aligns nucleotidic and peptidic sequences together.
PROTOGENE re-builds the original alignment with nucleotidic information it has gotten.

=head1 REQUIREMENT

=over 4

=item B<Perl 5.6 or better> is required !

=item Standard Perl modules B<lib>, B<strict>,  B<warnings>,  B<diagnostics>, B<Env> are required

=item and some other current ones : B<Getopt::Long>,  B<File::Which>,  B<File::Copy>, B<Mail::Send>

=item -

=item I<exonerate> from http://www.ebi.ac.uk/~guy/exonerate/

=item I<blast> from http://www.ncbi.nlm.nih.gov/BLAST/download.shtml or http://blast.wustl.edu/

=back

=head1 VERSION

=over 8

=item version 3.4.4

=item on Feb 25th, 2010

=back

=head1 AUTHORS

=over 8

=item Sebastien MORETTI

=item moretti.sebastien [AT] gmail.com

=item Frederic REINIER

=item reinier [AT] crs4.it

=item Lab. Information Genomique et Structurale - IGS

=item CNRS - Life Sciences

=item Marseille, France

=item http://www.igs.cnrs-mrs.fr/

=back

=cut


######################  Management  ######################

#Failure declaration
sub failure {
    print {*STDERR} "\n\nFATAL\n\nTERMINATION STATUS: FAILURE\n\n";
    return;
}

#Check external programs presence in the PATH
sub checkProgramsPresence{
    my ($webblast_exe, $exonerate) = @_;

    if ( $webblast_exe eq '' ){
        failure();
        print {*STDERR} "\twebblast.pl program is not reachable\n\tIt could not be in your PATH or not installed\n\n";
        exit(1);
    }
    if ( $exonerate eq '' ){
        failure();
        print {*STDERR} "\texonerate program is not reachable\n\tIt could not be in your PATH or not installed\n\n";
        exit(1);
    }
}

#Check cache directory accessibility: Test and change rights, if needs, of the $HOME/.ProtoGene/ cache directory
sub checkCacheAccessibility{
    my ($cachedir) = @_;

    if ( -d "$cachedir/" && -w "$cachedir/" && -x "$cachedir/" && -r "$cachedir/" ){
    }
    elsif ( -d "$cachedir/" ){
        if ( -o "$cachedir/" ){
            chmod 0754, "$cachedir/";
        }
        else{
            print {*STDERR} "\n\t** Warning ** :\n\tPermissions do NOT seem to be valid for the current cache directory\n\n";
        }
    }
    else{
        mkdir "$cachedir/";
        if ( -o "$cachedir/" ){
            chmod 0754, "$cachedir/"; #0754 est en octal, donc 754 ou '0754' ne sont pas bons !
        }
        else{
            print {*STDERR} "\n\t** Warning ** :\n\tPermissions do NOT seem to be valid for the current cache directory\n\n";
        }
    }
    return;
}

#Cache Management
sub cacheManagement{
    my ($Cache, $inScript) = @_;
    $inScript = 0 if ( !defined $inScript );

    if ( $Cache eq 'empty' ){
        my @temp_files = glob($cachedir.'/*.fas');
        if ( exists ($temp_files[0]) ){
            unlink $_ while(<@temp_files>);
        }
        @temp_files = glob($cachedir.'/*_ExonerateError');
        if ( exists ($temp_files[0]) ){
            unlink $_ while(<@temp_files>);
        }
        unlink("$cachedir/webblast.log", "$cachedir/web_tempo.result", "$cachedir/debug.tempo");
        print "\n\tcache directory [$cachedir] is empty\n\n";
        exit 0;
    }
    elsif ( $Cache eq 'old' ){
        my @temp_files = glob($cachedir.'/*.fas');
        my @tempFiles  = grep { -M $_ > $uct } @temp_files; #Sort list only with $uct days old files
        if ( exists ($tempFiles[0]) ){
            unlink $_ while(<@tempFiles>);
        }
        print "\n\tcache directory [$cachedir] is empty of its old files\n\n" if ($inScript==0);
        exit 0 if ( $inScript==0 );
    }
    elsif ( $Cache eq 'update' || $Cache eq 'use' ){
        $cache = $cachedir; #Limit cache files to less than $uct days old ones for 'update'
    }
    elsif ( $Cache eq 'none' ){
    }
    elsif ( -d $Cache ){
        die "\n\tProtoGene cannot access to your directory\n\n"  if ( ! -x $Cache );
        die "\n\tProtoGene cannot write into your directory\n\n" if ( ! -w $Cache );
        die "\n\tProtoGene cannot read into your directory\n\n"  if ( ! -r $Cache );
        $cache = $Cache;
    }
    else {
        die "\n\tWrong cache argument, or your cache folder doesn't seem to exist\n\n";
    }

    return($cache);
}

##########################################################


######################    Fasta    ######################

#Check input aln
sub checkFastaFile{
    my ($msa, $change) = @_;

    if ( -e "$msa" ){
    #Convert no fasta format to fasta with seq_reformat if available
        my $seqreformat = which('t_coffeeX') || ''; #desactive tant que t_coffee seq_reformat pose prob
        if ( $seqreformat ne '' ){
            my @formatType = `$seqreformat -other_pg seq_reformat -in $msa -print_format`;
            my $format     = '';
            while(<@formatType>){
                if ( $_ =~ /FORMAT:/ ){
                    $format = $_;
                    chomp($format);
                    last;
                }
            }
            if ( $format ne '' && $format !~ /FORMAT:fasta_/ ){
                system("$seqreformat -other_pg seq_reformat -in $msa -output fasta_aln -out=${msa}.007") if ($format =~ /_aln *$/);
                system("$seqreformat -other_pg seq_reformat -in $msa -output fasta_seq -out=${msa}.007") if ($format =~ /_seq *$/);
                $msa    .= '.007';
                $change  = 1;
            }
        }
    }
    else {
        failure();
        print {*STDERR} "\tThe file \'$msa\' doesn't not seem to be reachable\n\n";
        exit(1);
    }

    return($msa,$change);
}

#Prepare Fasta Header of the query name to incorporate our annotations
sub fastaHeaders4ProtoGene{
    my ($QueryName) = @_;

    $QueryName =~ s/  +/ /g;
    my ($cqacc, $cqdesc) = ($QueryName, $QueryName);
    $cqacc  =~ s/^ *([^ ]*) .*$/$1/;
    $cqacc  =~ s/[\s\t]*$//g;
    $cqacc  =  $cqacc.'_G_@@';
    $cqdesc =~ s/^ *[^ ]* *(.*)$/$1/;
    $cqdesc =~ s/^[\| \.]*//;
    $cqdesc =~ s/[\|\. ]*$//;

    $QueryName = $cqacc.$cqdesc;
    return($QueryName);
}

#Prepare Fasta Header to put it on the fasta header of the result outputs
sub getFastaHeaderAnnot{
    my ($bestTarget) = @_;

    #Get annotation from nucleotide file header
    open(my $BEST, '<', "$cache/${bestTarget}.fas");
    my $annot=<$BEST> || '';
    close $BEST;
    chomp($annot);
    $annot =~ s/^ *[^ ]* //;
    $annot =~ s/[\. ]*$//;
    $annot =~ s/^ *//;
    $annot =~ s/  +/ /g;

    return($annot);
}

#########################################################


###################### Translation ######################

#Direct Reverse translation
sub reverse_trad{
    my ($aa) = @_;

    my %reverse_code=('A' => 'GCN', 'F' => 'TTy', 'K' => 'AAr', 'P' => 'CCN', 'T' => 'ACN',
                      'C' => 'TGy', 'G' => 'GGN', 'L' => 'yTN', 'Q' => 'CAr', 'V' => 'GTN',
                      'D' => 'GAy', 'H' => 'CAy', 'M' => 'ATG', 'R' => 'mGN', 'W' => 'TGG',
                      'E' => 'GAr', 'I' => 'ATh', 'N' => 'AAy', 'S' => 'wsN', 'Y' => 'TAy',
                      'X' => 'NNN', '-' => '---', '*' => 'Trr'); # '*' is for stop codon

    my $triplet = 'nnn';
    while( my ($x,$y) =each(%reverse_code) ){
        if ( uc($aa) eq $x ){
            $triplet = $y;
            last;
        }
    }

    return($triplet);
}

#########################################################


####################### webblast #######################
sub launchWebblast{
    my ($infile, $quiet, $whatBlast, $db, $species, $blast_exe, $outfile) = @_;
    my $blast_at = '';

    $blast_at    = "-database=$db"                         if ( $whatBlast==0 );
    $blast_at    = "-database=$db -blast_dir=${blast_exe}" if ( $whatBlast==1 );
    $blast_at    = "-database=$db -gigablast=yes"          if ( $whatBlast==2 );

#    print "             Evalue treshold : 0.05
#             Matrix : BLOSUM62
#             Filter : F
#             **********************
#             For RefSeq Protein db:
#             Blast_identity_threshold : 100
#             Cover threshold : 100
#             **********************
#             For NR Protein db:
#             Blast_identity_threshold : 95
#             Cover threshold : 95";

    system("$webblast_exe -program=blastp ${blast_at} -infile=$infile -matrix=BLOSUM62 -evalue=0.05 -method=geneid -filter=Off -organism=$species -identity=100 -cover=100 -hits=10 $quiet -outfile=$outfile");


    if ( -z "$outfile" ){ #Another blastP query if no hit with refseq and db was not nr !
        $quiet = '-quiet=on';
        print {*STDERR} "run BLAST.. against NR because there was no acceptable hit against $db\n"
            if ( $whatBlast != 1 && $db ne 'nr' );
        print {*STDERR} "run BLAST.. again, with decreased thresholds, because there was no acceptable hit against $db\n"
            if ( $whatBlast == 1 || $db eq 'nr' );
        $blast_at =~ s/-database=[^\s]*/-database=nr/ if ( $whatBlast != 1 );
        system("$webblast_exe -program=blastp ${blast_at} -infile=$infile -matrix=BLOSUM62 -evalue=0.05 -method=geneid -filter=Off -organism=$species -identity=95 -cover=95 -hits=10 $quiet -outfile=$outfile");
    }

    move('webblast.log',     "$cache"); #Move temporary webblast.pl files
    move('web_tempo.result', "$cache");
    move('debug.tempo',      "$cache");

    unlink("$cache/webblast.log")     if ( -z "$cache/webblast.log" );
    unlink("$cache/web_tempo.result") if ( -z "$cache/web_tempo.result" );
    unlink("$cache/debug.tempo")      if ( -z "$cache/debug.tempo" );

    return;
}


########################################################


##################### NCBI requests #####################

#Prot ACC -> PUID
sub blastPAcc2PGI{
    my ($blastHit) = @_;

    my $protGI = '';
    my $count  = 0;
    GET_PUI:
    for(my $rep=0;$rep <= 4; $rep++){
        $count++;
#        system("wget -q -O $cache/${date}_${blastHit}protGi.tmp 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=protein&term=${blastHit}[pacc]&retmode=xml&tool=ProtoGene&email=smoretti\@unil.ch'");
        system("wget -q -O $cache/${date}_${blastHit}protGi.tmp 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=protein&term=${blastHit}&retmode=xml&tool=ProtoGene&email=smoretti\@unil.ch'");
        open(my $GIP, '<', "$cache/${date}_${blastHit}protGi.tmp");
        PROT_GI:
        while(<$GIP>){
            if ( $_ =~ /^.*<Id>(\d+)<\/Id>.*$/ ){
                $protGI = $1;
                last PROT_GI; #OK if only one PUID
            }
        }
        close $GIP;
        $rep = $rep-15 if ( -z "$cache/${date}_${blastHit}protGi.tmp" && $count==1 );
        last GET_PUI if ( $protGI =~ /^\d+$/ );
    }
    unlink("$cache/${date}_${blastHit}protGi.tmp") if ( $tmp == 0 || $protGI ne '' );

    return($protGI);
}

#PUID -> nt GIs
sub protGI2NTGIs{
    my ($protGI, $blastHit) = @_;

    my $ntGIs  = '';
    my $geneID = '';
    my $count  = 0;
    GET_NTUI:
    for(my $rep=0;$rep <= 4; $rep++){
        $count++;
        #nt GIs
        system("wget -q -O $cache/${date}_${blastHit}nucleo.tmp 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=protein&db=nuccore,nucleotide,gene&id=$protGI&retmode=xml&tool=ProtoGene&email=smoretti\@unil.ch'");
        open(my $GIN, '<', "$cache/${date}_${blastHit}nucleo.tmp");
        my $flag = 0;
        NT_GI:
        while(<$GIN>){
            if ( $_   =~ /<LinkName>protein_nuc[a-z]+<\/LinkName>/ ){ #for nuccleotide and nuccore
                $flag = 1;
            }
            elsif ( $_ =~ /\<LinkName\>protein_gene\<\/LinkName\>/ ){
                $flag  = 2;
            }
            elsif ( $flag==1 && $_ =~ /^.*\<Id\>(\d+)\<\/Id\>.*$/ ){
                my $match = $1;
                $ntGIs  .= ",$match" if ( $ntGIs  ne '' && $ntGIs  !~ /,$match,/ && $ntGIs  !~ /^$match,/ );
                $ntGIs   = $match    if ( $ntGIs  eq '' );
            }
            elsif ( $flag==2 && $_ =~ /^.*\<Id\>(\d+)\<\/Id\>.*$/ ){
                my $match = $1;
                $geneID .= ",$match" if ( $geneID ne '' && $geneID !~ /,$match,/ && $geneID !~ /^$match,/ );
                $geneID  = $match    if ( $geneID eq '' );
            }
        }
        close $GIN;
        $rep = $rep-15 if ( -z "$cache/${date}_${blastHit}nucleo.tmp" && $count==1 );
        last GET_NTUI if ( $ntGIs ne '' || $geneID ne '' );
    }
    unlink("$cache/${date}_${blastHit}nucleo.tmp") if ( $tmp==0 || ($ntGIs ne '' || $geneID ne '') );

    return($ntGIs, $geneID);
}

sub geneID2Chr{
    my ($geneID, $blastHit) = @_;

    my $chr   = '';
    my ($amont, $aval) = ('', '');
    my $count = 0;
    GET_CHR:
    for(my $rep=0;$rep <= 8; $rep++){
        $count++;
        #Gene Acc
#        system("wget -q -O $cache/${date}_${blastHit}gene.tmp 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=gene&id=$geneID&retmode=xml&tool=ProtoGene&email=smoretti\@unil.ch'");
        system("wget -q -O $cache/${date}_${blastHit}gene.tmp 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&id=$geneID&retmode=xml&tool=ProtoGene&email=smoretti\@unil.ch'");
        open(my $GACC, '<', "$cache/${date}_${blastHit}gene.tmp");
        my $flag = 0;
        GENE_CHR:
        while(<$GACC>){
            if ( $_  =~ /\<ERROR\>Empty id list \- nothing todo\<\/ERROR\>/ && $flag==0 ){
                $rep = $rep-15 if ( $count==1 );
            }
#            if ( $_   =~ /\<Entrezgene_locus\>/ && $flag==0 ){
            if ( $_   =~ /<Item Name="GenomicInfoType" Type="Structure">/ && $flag==0 ){
               $flag = 1;
            }
#            elsif ( $_ =~ /\<Gene-commentary_type value=\"genomic\"\>/ && $flag==1 ){
#                $flag  = 2;
#            }
#            elsif ( $_ =~ /\<Gene-commentary_type value=/ && $flag==2 ){
#                last GENE_CHR;
#            }
#            elsif ( $_ =~ /\<Gene-commentary_accession\>([\w\_\-\.]+)\<\/Gene-commentary_accession\>/ && $flag==2 ){
            elsif ( $_ =~ /<Item Name="ChrAccVer" Type="String">([^<]+?)\.?\d*<\/Item>/ && $flag==1 ){
                $chr   = $1;
                $flag  = 3;
            }
#            elsif ( $_ =~ /\<Seq-interval_from\>(\d+)\<\/Seq-interval_from\>/ && $flag==3 ){
            elsif ( $_ =~ /<Item Name="ChrStart" Type="Integer">(\d+)<\/Item>/ && $flag==3 ){
                $amont = $1;
                $flag  = 4;
            }
#            elsif ( $_ =~ /\<Seq-interval_to\>(\d+)\<\/Seq-interval_to\>/ && $flag==4 ){
            elsif ( $_ =~ /<Item Name="ChrStop" Type="Integer">(\d+)<\/Item>/ && $flag==4 ){
                $aval  = $1;
                last GENE_CHR;
            }
        }
        close $GACC;
        $rep = $rep-15 if ( -z "$cache/${date}_${blastHit}gene.tmp" && $count==1 );
        last GET_CHR   if ( $chr ne '' && $amont =~ /^\d+$/ && $aval =~ /^\d+$/ );
    }
    unlink("$cache/${date}_${blastHit}gene.tmp") if ( $tmp == 0 || $chr ne '' );
    if ( $amont eq '' || $aval eq '' ){
        $amont = '';
        $aval = '';
    }
    elsif ( $amont ne '' && $aval ne '' ){
        if ( $amont > $aval ){
            $amont = $amont+5000;
            $aval  = $aval-5000;
        }
        else{
            $amont = $amont-5000;
            $aval  = $aval+5000;
        }
    }
    $amont = 1 if ( $amont ne '' && $amont <= 0 );
    $aval  = 1 if ( $aval ne ''  && $aval <= 0 );

    return($chr, $amont, $aval);
}

sub downloadSeqFromGIs{
    my ($cache, $date, $amont, $aval, @acc) = @_;

    GET_SEQ:
    for(my $a=0; $a<=$#acc; $a++){
        my $whatNumber = 0;
        if ( $amont =~ /^\d+$/ && $aval =~ /^\d+$/ ){
            GET_CHR_SEQ:
            for(my $rep=0;$rep <= 4; $rep++){
                system("wget -q -O $cache/${acc[$a]}-${amont}.fas 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nucleotide&id=${acc[$a]}&rettype=fasta&retmode=text&from=$amont&to=$aval&tool=ProtoGene&email=smoretti\@unil.ch'") if ( !-e "$cache/${acc[$a]}-${amont}.fas" || -z "$cache/${acc[$a]}-${amont}.fas" || $Cache eq 'none' || ($Cache eq 'update' && -M "$cache/${acc[$a]}-${amont}.fas" > $uct ) );
                open(my $CIBLE, '<', "$cache/${acc[$a]}-${amont}.fas");
                my $counter = 0;
                my $lines   = 0;
                CHECK_CHR_SEQ:
                while(<$CIBLE>){
                    $lines++              if ( $_ !~ /^>/ );
                    $counter = $counter+2 if ( $counter==1 && $_ !~ /^\w/ && $lines==1 && $_ !~ /^>/ );
                    $counter++            if ( $_ =~ /^>/ );
                    $counter = $counter+2 if ( $_ !~ /^>/ && ($_ =~ /Error:/ || $_ =~ /[<>]/) );
#                   last CHECK_CHR_SEQ    if ( $_ !~ /^>/ && $counter==1 && $_ =~ /^\w/ );
                }
                close $CIBLE;
                $whatNumber++;
                last GET_CHR_SEQ if ( $whatNumber==20 );
                if ( $counter != 1 ){
                    $rep = $rep-1;
                    unlink("$cache/${acc[$a]}-${amont}.fas");
                }
            }
        }
        else{
            GET_OTHER_SEQ:
            for(my $rep=0;$rep <= 4; $rep++){
                system("wget -q -O $cache/${acc[$a]}.fas 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nucleotide&id=${acc[$a]}&rettype=fasta&retmode=text&tool=ProtoGene&email=smoretti\@unil.ch'") if ( !-e "$cache/${acc[$a]}.fas" || -z "$cache/${acc[$a]}.fas" || $Cache eq 'none' || ($Cache eq 'update' && -M "$cache/${acc[$a]}.fas" > $uct ) );
                open(my $CIBLE, '<', "$cache/${acc[$a]}.fas");
                my $counter = 0;
                my $lines   = 0;
                CHECK_OTHER_SEQ:
                while(<$CIBLE>){
                    $lines++              if ( $_ !~ /^>/ );
                    $counter = $counter+2 if ( $counter==1 && $_ !~ /^\w/ && $lines==1 && $_ !~ /^>/ );
                    $counter++            if ( $_ =~ /^>/ );
                    $counter = $counter+2 if ( $_ !~ /^>/ && ($_ =~ /Error:/ || $_ =~ /[<>]/) );
                }
                close $CIBLE;
                $whatNumber++;
                last GET_OTHER_SEQ if ( $whatNumber==20 );
                if ( $counter != 1 ){
                    $rep=$rep-1;
                    unlink("$cache/${acc[$a]}.fas");
                }
            }
        }
    }

    return;
}

sub downloadSeq{
    my ($cache, $date, $amont, $aval, @acc) = @_;

    my $cp   = 0;
    my $from = $amont;
    my $to   = $aval;
    DOWNL_SEQ:
    for(my $a=0; $a<=$#acc; $a++){
        my $pacc2puid = $acc[$a];
        if ( $pacc2puid !~ /^[NAX][CGTWZM]_/ ){
        #pacc = primary acc NOT prot acc !   #265666 -> S55551
            system("wget -q -O $cache/${date}_${acc[$a]}.gui 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?&db=nucleotide&term=${pacc2puid}[pacc]&tool=ProtoGene&email=smoretti\@unil.ch'");
            open(my $GUI, '<', "$cache/${date}_${acc[$a]}.gui");
            DOWNL:
            while(<$GUI>){
                if ( $_ =~ /\<Id\>(\d+)\<\/Id\>/ ){
                    $pacc2puid = $1;
                    last DOWNL;
                }
            }
            close $GUI;
            unlink("$cache/${date}_${acc[$a]}.gui") if ($tmp == 0 or $pacc2puid =~ /^\d+$/);
        }
        my $whatNumber = 0;
        if ( $amont =~ /^\d+$/ && $aval =~ /^\d+$/ ){
            CHECK_CHR_DOWN:
            for(my $rep=0;$rep <= 4; $rep++){
                system("wget -q -O $cache/${acc[$a]}:${from}-${to}.fas 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nucleotide&id=${pacc2puid}&rettype=fasta&retmode=text&from=$amont&to=$aval&tool=ProtoGene&email=smoretti\@unil.ch'") if ( !-e "$cache/${acc[$a]}:${from}-${to}.fas" || -z "$cache/${acc[$a]}:${from}-${to}.fas" || $Cache eq 'none' || ($Cache eq 'update' && -M "$cache/${acc[$a]}:${from}-${to}.fas" > $uct ));
                open(my $CIBLE, '<', "$cache/${acc[$a]}:${amont}-${aval}.fas");
                my $counter = 0;
                my $lines   = 0;
                while(<$CIBLE>){
                    $lines++              if ( $_ !~ /^>/ );
                    $counter = $counter+2 if ( $counter==1 && $_ !~ /^\w/ && $lines==1 && $_ !~ /^>/ );
                    $counter++            if ( $_ =~ /^>/ );
                    $counter = $counter+2 if ( $_ !~ /^>/ && ($_ =~ /Error:/ || $_ =~ /[<>]/) );
                }
                close $CIBLE;
                $whatNumber++;
                last CHECK_CHR_DOWN if ( $whatNumber==20 );
                if ( $counter != 1 ){
                    $rep = $rep-1;
                    unlink("$cache/${acc[$a]}:${amont}-${aval}.fas");
                }
            }
        }
        else{
            CHECK_OTHER_DOWN:
            for(my $rep=0;$rep <= 4; $rep++){
                system("wget -q -O $cache/${acc[$a]}.fas 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nucleotide&id=${pacc2puid}&rettype=fasta&retmode=text&tool=ProtoGene&email=smoretti\@unil.ch'") if ( !-e "$cache/${acc[$a]}.fas" || -z "$cache/${acc[$a]}.fas" || $Cache eq 'none' || ($Cache eq 'update' && -M "$cache/${acc[$a]}.fas" > $uct ));
                open(my $CIBLE, '<', "$cache/${acc[$a]}.fas");
                my $counter = 0;
                my $lines   = 0;
                while(<$CIBLE>){
                    $lines++              if ( $_ !~ /^>/ );
                    $counter = $counter+2 if ( $counter==1 && $_ !~ /^\w/ && $lines==1 && $_ !~ /^>/ );
                    $counter++            if ( $_ =~ /^>/ );
                    $counter = $counter+2 if ( $_ !~ /^>/ && ($_ =~ /Error:/ || $_ =~ /[<>]/) );
                }
                close $CIBLE;
                $whatNumber++;
                last CHECK_OTHER_DOWN if ( $whatNumber==20 );
                if ( $counter != 1 ){
                    $rep = $rep-1;
                    unlink("$cache/${acc[$a]}.fas");
                }
            }
        }
        my $checkSeq = '';
        $checkSeq    = `grep -v '>' "$cache/${acc[$a]}:${from}-${to}.fas"` if ( $amont =~ /^\d+$/ && $aval =~ /^\d+$/ );
        $checkSeq    = `grep -v '>' "$cache/${acc[$a]}.fas"`               if ( $amont !~ /^\d+$/ || $aval !~ /^\d+$/ );
        if ( $checkSeq !~ /^[A-Za-z]/ && $cp==0 ){
#       if ( -s "$cache/${acc[$a]}:${from}-${to}.fas" <120 && $cp==0){ #Instead of multiple downloads, only seq name and header
            unlink("$cache/${acc[$a]}:${from}-${to}.fas") if ( $amont =~ /^\d+$/ && $aval =~ /^\d+$/ );
            unlink("$cache/${acc[$a]}.fas")               if ( $amont !~ /^\d+$/ || $aval !~ /^\d+$/ );
#           $amont = '';
            $aval  = $aval-5000 if ( $aval =~ /^\d+$/ );
            $a     = $a-1;
            $cp++;
        }
    }

    return;
}
#########################################################



###################### Exonerate #######################
sub runExonerate{
    my ($exonerate, $cache, $date, $order, $original, $right_name, $blastHit, $r, @gis) = @_;

    #Prot to align to nucleotidic sequence(s)
    open(my $FASTA, '>', "$cache/${date}_${order}.fas");
    my $seq2aln = $original;
    $seq2aln    =~ s/-//g;
    $seq2aln   .= '*'; #To add stop codon triplet, if any, at the end of the protein seq
    #* is aligned only if there is one stop codon similar to the last position of protein
    #If not, * is excluded from the alignment because it is an external border misalignment
    print {$FASTA} ">$order\n$seq2aln\n";
    close $FASTA;

    #Warning: Abusive threshold between genomic and transcript sequence lengths. Arbitrarily fixed at 5000 bp here !
    # To manage short DNA seq, everything pass through --model protein2genome but with --exhaustive for short ones
    my %best_pos;
    my %bestBOJ;
    my @bestAln;
    RUN_EXONERATE:
    for(my $b=0; $b<=$#gis; $b++){
        if ( -s "$cache/${gis[$b]}.fas" > 5000 ){
            system("$exonerate --showquerygff --showtargetgff --model protein2genome --bestn 1 --percent 75 -q $cache/${date}_${order}.fas -t $cache/${gis[$b]}.fas >$cache/${date}_${order}.exon 2>>$cache/${date}_ExonerateError"); #Exonerate prot - genomic or long RNA
        }
        else{
            system("$exonerate --showquerygff --showtargetgff --model protein2genome --bestn 1 --percent 75 --exhaustive -q $cache/${date}_${order}.fas -t $cache/${gis[$b]}.fas >$cache/${date}_${order}.exon 2>>$cache/${date}_ExonerateError"); #Exonerate prot - short genomic or RNA
        }
        my $targetNT = $gis[$b];
        $targetNT = "gi\|".$targetNT."\|" if ( $gis[$b] =~ /^\d+$/ );
        print {*STDERR} "\n@@ -> $b ... $targetNT\n";


        unlink("$cache/Error:") if ( $tmp == 0 || -z "$cache/Error:" ); #Remove exonerate Error file is it fails to align protein and nucleotide sequences


        if ( !-e "$cache/${date}_${order}.exon" || (-s "$cache/${date}_${order}.exon") < 520 ){
            print "\tProtein-Nucleotide alignment has   FAILED   for $targetNT with $right_name\n";
            next RUN_EXONERATE;
        }
        print "\tProtein-Nucleotide alignment was successful for $targetNT with $right_name\n";


        #Parse Exonerate output
        my ($posiTions, $posBOJ) = loci_from_Exonerate::parser("$cache/${date}_${order}.exon", $debug);


        %best_pos   = testPositions($posiTions, $gis[$b], %best_pos);
        %bestBOJ    = testBOJ($posBOJ, $gis[$b], %bestBOJ);
        my $whichGI = $gis[$b];
        $whichGI    = 'gi|'.$whichGI.'|' if ( $whichGI =~ /^\d+$/ );
        print {*STDERR} "\tNo exon-intron structure found for $whichGI with $right_name\n" if ( %$posBOJ eq 0 );
    }

    #Remove temp files from exonerate and query sequence for exonerate
    unlink("$cache/${date}_${order}.fas", "$cache/${date}_${order}.exon");# if ($tmp == 0);


    my $refBestPOS = \%best_pos; #Use perl references to send two variables (mem address for the 2 hashes) instead of 2 hashes,
    my $refBestBOJ = \%bestBOJ; #and easily, and properly, get them in the main script !
    return($refBestPOS, $refBestBOJ);
}


sub testPositions{
    my ($posiTions, $currentTarget, %best_pos) = @_;

    my %positions = %$posiTions;
    if ( %positions ne 0 ){
        %positions = (%positions, '0' => $currentTarget);
        my @clefs  = sort({$a <=> $b} keys(%positions));
        foreach (@clefs) {
            print {*STDERR} "$_\t$positions{$_}\n" if ( $debug && $_>0 );
        }

        my @cles = sort({$a <=> $b} keys(%best_pos));
        if ( !exists($cles[0]) ){
            return(%positions);
        }
        else{
            return(%positions) if ( exists($cles[0]) && $#cles < $#clefs );
            return(%best_pos)  if ( exists($cles[0]) && $#cles >= $#clefs );
        }
    }
    elsif ( %best_pos ne 0 ){
        return(%best_pos);
    }
    else{
        return(%positions);
    }
}

sub testBOJ{
    my ($posBOJ, $currentTarget, %bestBOJ) = @_;

    my %positBOJ = %$posBOJ;
    if (%positBOJ ne 0){
        %positBOJ = (%positBOJ, '0' => $currentTarget);
        my @clefs = sort({$a <=> $b} keys(%positBOJ));
        while(my ($x,$y) =each(%positBOJ) ){
            print {*STDERR} "pos:$x -> $y\n" if ( $x>0 );
        }

        my @cles = sort({$a <=> $b} keys(%bestBOJ));
        if ( !exists($cles[0]) ){
            return(%positBOJ);
        }
        else{
            return(%positBOJ) if ( exists($cles[0]) && $#cles < $#clefs );
            return(%bestBOJ)  if ( exists($cles[0]) && $#cles >= $#clefs );
        }
    }
    elsif ( %bestBOJ ne 0 ){
        return(%bestBOJ);
    }
    else {
        return(%positBOJ);
    }
}
########################################################



################## Build output files ##################
sub prepareResults4CDS{
    my ($POS, $blastHit, $input_seq, $input_name) = @_;

    my %POSITIONS = %$POS;

    my $resultPOS = '';
    my ($bestOne, $annot) = ('', '');
    if ( %POSITIONS ne 0 ){
        ($bestOne, $annot) = prepareAnnotation($POSITIONS{0});
        my $seqName = buildAnnotation($bestOne, $annot, $input_name, $blastHit);
        my $seqCDS  = buildCDSseq($input_seq, %POSITIONS);
        $resultPOS  = ">".$seqName."\n".$seqCDS."\n";
    }

    return($resultPOS, $bestOne);
}

sub prepareResults4BOJ{
    my ($BOJ, $blastHit, $input_seq, $input_name, $intronLess, $nameLess) = @_;

    my %EXONBORDERS = %$BOJ;

    my $resultBOJ = '';
    my ($bestOne, $annot) = ('', '');
    if ( %EXONBORDERS ne 0 ){
        ($bestOne, $annot) = prepareAnnotation($EXONBORDERS{0});
        my $seqName = buildAnnotation($bestOne, $annot, $input_name, $blastHit);
        my $seqBOJ  = buildBOJseq($input_seq, %EXONBORDERS);
        $resultBOJ  = ">".$seqName."\n".$seqBOJ."\n";
    }
    elsif ( $intronLess ne '' ){
        ($bestOne, $annot) = prepareAnnotation($intronLess);
        my $description    = $annot;#getFastaHeaderAnnot($intronLess);
        my $readyname      = '';
        $readyname         = fastaHeaders4ProtoGene($input_name);
        if ( $intronLess =~ /\:.+/ || $bestOne =~ /^N[CTWZG]_/ || $bestOne =~ /^AC_/ || $nameLess =~ /^N[CTWZG]_/ || $nameLess =~ /^AC_/ ){
            $readyname   =~ s/_G_@@/_G_${bestOne}-IntronLess _S_ $blastHit _DESC_ /  if ( $nameLess eq '' );
            $readyname   =~ s/_G_@@/_G_${nameLess}-IntronLess _S_ $blastHit _DESC_ / if ( $nameLess ne '' );
            $readyname   =~ s/(.)$/$1 MATCHES_ON $description/                       if ( $description ne '' );
            $bestOne     = $nameLess                                                 if ( $nameLess ne '' );
        }
        else{
            $readyname =~ s/_G_@@/_G_Unavailable _S_ $blastHit _DESC_ /;
            $bestOne   = '';
        }
        $readyname =~ s/  +/ /g;
        $resultBOJ = ">".$readyname."\n".$input_seq."\n";
    }

    return($resultBOJ, $bestOne);
}

sub prepareAnnotation{
    my ($best_pos) = @_;

    my $bestOne = $best_pos;
    $bestOne    =~ s/:.+$// if ( $best_pos !~ /^\d+$/ );
    if ( $bestOne =~ /^\d+$/ ){
        open(my $BEST, '<', "$cache/${bestOne}.fas");
        FIND_BEST:
        while(<$BEST>){
            if ($_ =~ /^>/){
                my $goodName = $_;
                chomp($goodName);
                $goodName    =~ s/^>//;
                $goodName    =~ s/^gi\|\d+\|//;
                $goodName    =~ s/^ *([^ ]+) *.*$/$1/;
                $goodName    =~ s/^...?\|([\w\_\-]+).*/$1/;
                $bestOne     = $goodName;
                last FIND_BEST;
            }
        }
        close $BEST;
    }
    my $annot = getFastaHeaderAnnot($best_pos);

    return($bestOne, $annot);
}

sub buildAnnotation{
    my ($bestOne, $annot, $input_name, $blastHit) = @_;

    $annot =~ s{>}{}g; #Remove > sign in annotation if any e.g.: NM_105729
    my $readyName = '';
    $readyName    = fastaHeaders4ProtoGene($input_name);
    $readyName    =~ s/_G_@@/_G_$bestOne _S_ $blastHit _DESC_ /;
    $readyName    =~ s/(.)$/$1 MATCHES_ON $annot/ if ( $annot ne '' );
    $readyName    =~ s/  +/ /g;

    return($readyName);
}

sub buildCDSseq{
    my ($input_seq, %POSITIONS) = @_;

    my $cdsSeq = '';
    my $locus  = 0;
    for(my $z=0; $z<=length($input_seq); $z++){
        if ( $z==length($input_seq) ){
            $locus++;
            if ( exists($POSITIONS{$locus}) && ($POSITIONS{$locus} eq 'TAA' || $POSITIONS{$locus} eq 'TAG' || $POSITIONS{$locus} eq 'TGA') ){
                $cdsSeq .= $POSITIONS{$locus};
            #To add properly the stop codon, if any, at the end of the CDS
            }
            else{
                $cdsSeq .= '---'; #Else add 3x'-', if no stop codon were found, to keep the original MSA side length
            }
        }
        elsif ( substr($input_seq, $z, 1) =~ /[A-Za-z]/ ){
            $locus++;
            if ( exists($POSITIONS{$locus}) ){
                $cdsSeq .= $POSITIONS{$locus};
            }
#            elsif ( substr($input_seq, $z, 1) =~ /^[Mm]$/ ){ #for mismatches where we are sure of the triplet, i.e. monocodon Met et Trp
#                $cdsSeq .= 'ATG';
#            }
#            elsif ( substr($input_seq, $z, 1) =~ /^[Ww]$/ ){
#                $cdsSeq .= 'TGG';
#            }
            else{
                $cdsSeq .= 'NNN';
            }
        }
        else{
            $cdsSeq .= '---';
        }
    }

    return($cdsSeq);
}

sub buildBOJseq{
    my ($input_seq, %EXONBORDERS) = @_;

    my $bojSeq        = '';
    my $countNoGapPos = 0;
    for(my $as=0;$as < length($input_seq); $as++){
        my $whatIs = substr($input_seq, $as, 1);
        if ( $whatIs eq '-' ){
            $bojSeq .= $whatIs;
        }
        else{
            $countNoGapPos++;
            $bojSeq .= lc($EXONBORDERS{$countNoGapPos}) if ( exists($EXONBORDERS{$countNoGapPos}) );
            $bojSeq .= uc($whatIs)                      if ( !exists($EXONBORDERS{$countNoGapPos}) );
        }
    }

    return($bojSeq);
}

sub createCDSOutputFile{
    my ($CDSresultat, $original_seq, $original_name) = @_;

    open(my $CDS,  '>>', "${originalMSA}.cds");
    open(my $CDSP, '>>', "${originalMSA}.cdsP") if ( $pep==1 );
    $CDSresultat =~ s/  +/ /g;
    $CDSresultat =~ s/MATCHES_ON for >/MATCHES_ON for /;

    if ( $pep==1 ){
        my $peptide = $original_seq;
        $peptide    =~ s/(.)/${1}--/g;
        print {$CDSP} $CDSresultat, ">$original_name\n$peptide\n";
    }
    my $CDSreformated = '';
    my @allCDS = split(/>/, $CDSresultat);
    for(my $er=0; $er<=$#allCDS; $er++){
        my ($CDSreformatedName, $CDSreformatedSeq) = ($allCDS[$er], $allCDS[$er]);
        $CDSreformatedName =~ s/^([^\n]*\n).+\n$/>$1/;
        $CDSreformatedSeq  =~ s/^[^\n]*\n(.+\n)$/$1/;
        $CDSreformatedSeq  =~ s/([^\n]{60})/$1\n/g;
        $CDSreformated    .= $CDSreformatedName.$CDSreformatedSeq;
    }
#    print {$CDS} $CDSresultat;
    print {$CDS} $CDSreformated;

    close $CDS;
    close $CDSP if ( $pep==1 );
    return;
}

sub createBOJOutputFile{
    my ($BOJresultat, $original_seq) = @_;
    $BOJresultat =~ s/  +/ /g;

    open(my $BOJ, '>>', "${originalMSA}.boj");
    print {$BOJ} $BOJresultat;
    close $BOJ;
    return;
}

sub buildIntronlessBOJOutputFile{
    my ($BLASTstatus, $NTstatus, $NTname, $original_name, $original_seq) = @_;

    my $annot     = getFastaHeaderAnnot($NTstatus);
    my $readyname = '';
    $readyname    = fastaHeaders4ProtoGene($original_name);
    $NTstatus     =~ s/\:.+$//;
    $readyname    =~ s/_G_@@/_G_${NTstatus}-IntronLess _S_ $BLASTstatus _DESC_ / if ( $NTname eq '' );
    $readyname    =~ s/_G_@@/_G_${NTname}-IntronLess _S_ $BLASTstatus _DESC_ /   if ( $NTname ne '' );
    $readyname    =~ s/(.)$/$1 MATCHES_ON $annot/                                if ( $annot ne '' );
    $readyname    =~ s/  +/ /g;
    open(my $BOJ, '>>', "${originalMSA}.boj");
    print {$BOJ} ">$readyname\n$original_seq\n";
    close $BOJ;
    return;
}

sub buildFailureOutputFiles{
    my ($order, $BLASTstatus, $NTstatus, $annot) = @_;

    my $oriname   = $original_names[$order];
    my $readyname = '';
    open(CDS,  '>>', "${originalMSA}.cds");
    open(CDSP, '>>', "${originalMSA}.cdsP") if ( $pep==1 );
    open(OUT,  '>>', "${originalMSA}.out");
    open(BOJ,  '>>', "${originalMSA}.boj");
    if ( $BLASTstatus eq 'No_BLASTp_Result' ){
        revtransBuilding($oriname, $order, $BLASTstatus, $NTstatus, $readyname, '');
    }
    elsif ( $NTstatus eq 'PUI_unavailable' ){
        revtransBuilding($oriname, $order, $BLASTstatus, $NTstatus, $readyname, '');
    }
    elsif ( $NTstatus eq 'No_nt_link' ){
        revtransBuilding($oriname, $order, $BLASTstatus, $NTstatus, $readyname, '');
    }
    elsif ( $NTstatus eq 'Failed_Aln' ){
        revtransBuilding($oriname, $order, $BLASTstatus, $NTstatus, $readyname, '');
    }

    close CDS;
    close CDSP if ( $pep==1 );
    close OUT;
    close BOJ;
    return;
}

sub buildFailureBOJOutputFile{
    my ($BLASTstatus, $NTstatus, $original_name, $original_seq) = @_;

    my $readyname = '';
    $readyname    = fastaHeaders4ProtoGene($original_name);
    $readyname    =~ s/_G_@@/_G_$NTstatus _S_ $BLASTstatus _DESC_ /;
    $readyname    =~ s/  +/ /g;
    open( my $BOJ, '>>', "${originalMSA}.boj");
    print {$BOJ} ">$readyname\n$original_seq\n";
    close $BOJ;
    return;
}

sub revtransBuilding{
    my ($oriname, $order, $BLASTstatus, $NTstatus, $readyname, $annot) = @_;

    $readyname = fastaHeaders4ProtoGene($oriname);
    $readyname =~ s/_G_@@/_G_$NTstatus _S_ $BLASTstatus _DESC_ /;
    $readyname =~ s/(.)$/$1 MATCHES_ON $annot/ if ($annot ne '');
    $readyname =~ s/  +/ /g;
    print OUT "$readyname\n";
    my $final_seq = '';
    if ( $revtrans==1 ){
        $final_seq = '';
        for(my $w=0; $w < length($original_seq[$order]); $w++){
            my $aa     = substr($original_seq[$order], $w, 1);
            $final_seq = $final_seq.reverse_trad($aa);
        }
        $readyname           =~ s/_G_$NTstatus _S_ $BLASTstatus /_G_revtrans /;
        my $CDSreformatedSeq = ${final_seq}."---\n";
        $CDSreformatedSeq    =~ s/([^\n]{60})/$1\n/g;
#       print CDS "$readyname\n${final_seq}---\n";
        print CDS "$readyname\n$CDSreformatedSeq";
    }
    if ( $pep==1 && $revtrans==1 ){
        my $peptide = $original_seq[$order];
        $peptide    =~ s/(.)/${1}--/g;
        print CDSP "$readyname\n${final_seq}---\n$original_names[$order]\n$peptide\n";
    }
    return;
}

########################################################



################## Check output files ##################
sub checkAndCleanStderrFiles{
    my ($ExonerateStderrFiles) = @_;

    my $body = '';
    if ( -e "$ExonerateStderrFiles" && -s "$ExonerateStderrFiles"){
        my %uniq;
        open(my $ERRSTD, '<', "$ExonerateStderrFiles");
        while(<$ERRSTD>){
#           $body .= $_ if ( $_ !~ /^$/ && $_ !~ /Exhaustively generating suboptimal alignments will be very slow/ && $_ !~ /Message: Exhaustive alignment of/ );
            %uniq = (%uniq,"$_" => '') if ( $_ !~ /^$/ && $_ !~ /Exhaustively generating suboptimal alignments will be very slow/ && $_ !~ /Message: Exhaustive alignment of/ && $_ !~ /Missing calc_macro for Calc/ && $_ !~ /Warning zero length sequence/ );
        }
        close $ERRSTD;
        while( my ($a,$b)=each(%uniq) ){
            $body .= $a;
        }

#        if ( $body ne '' && $userEMail =~ /^[\w\_\-\.]+@[\w\_\-\.]+\.[A-Za-z][A-Za-z][A-Za-z]*$/ ){
#            my $msg = new Mail::Send Subject=>"[Protogene: ${date}_Error]", To=>"$userEMail";
#            my $fh  = $msg->open;
#            print $fh $body;
#           $fh->close; # complete the message and send it
#        }

    }
    unlink("$ExonerateStderrFiles") if ( $tmp == 0 || $body eq '' );
    return;
}

sub checkOutputFiles{

    unlink("${originalMSA}.cds")       if ( -z "${originalMSA}.cds" );
    unlink("${originalMSA}.cdsP")      if ( -z "${originalMSA}.cdsP" );
    unlink("${originalMSA}.cdsP.html") if ( -z "${originalMSA}.cdsP.html" );
    unlink("${originalMSA}.out")       if ( -z "${originalMSA}.out" );
    unlink("${originalMSA}.boj")       if ( -z "${originalMSA}.boj" || !-e "${originalMSA}.cds" );
}
########################################################



####################### Template #######################
sub template_failure {
    print {*STDERR} "\tYour template file has NOT the right format. Every lines must look like:\n";
    print {*STDERR} "\t>name1_G_NAcc ... or\n\t>name1 _S_ PAcc ...\n";
    print {*STDERR} "\tWith NAcc = accession number or GI (from NCBI) of the nucleotidic target of your query name1\n";
    print {*STDERR} "\tWith PAcc = accession number (from NCBI) of the peptidic target of your query name1\n";
    print {*STDERR} "\t\tname1 must NOT contain space, '\\s', character\n";
    print {*STDERR} "\t\taccession number must NOT be version number of NCBI\n";
    print {*STDERR} "\tYou can provide your own nucleotidic sequence, below corresponding template line, with NAcc='My_Seq'\n\n";
    print {*STDERR} "\tE.g.: >CDK2_hs Human Cyclin-Dependent Kinase 2\n";
    print {*STDERR} "\t      MENFQKVEKIGEGTYGVVYKARNKLTGEVVALK....\n\n";
    print {*STDERR} "\t\ttemplate file should be\n";
    print {*STDERR} "\t      >CDK2_hs_G_NC_000012\n";
    print {*STDERR} "\t\tif you provide a nucleotidic target identifier\n";
    print {*STDERR} "\t      >CDK2_hs _S_ NP_001789\n";
    print {*STDERR} "\t\tif you provide a peptidic target identifier\n";
    print {*STDERR} "\t      >CDK2_hs_G_My_Seq\n";
    print {*STDERR} "\t      TTCCTTCTCAGGGATAACACTCTATTCATGTCACTCCATTCA...\n";
    print {*STDERR} "\t\tif you provide your own nucleotidic target sequence\n\n";
}

sub checkTemplate{

    my ($templateFile) = @_;

    if ( -e "$templateFile" && -s "$templateFile" ){
        my $fasta_checker = -1;
        my (%blastpTarget, %nucTarget, %nucSeq);
        open(my $TEMPLATE, '<', "$templateFile");
        my $realSeqName = '';
        READ_TEMPLATE:
        while(<$TEMPLATE>){
            if ( $_ =~ /^>/ && $_ !~ /_[GS]_ *[^ ]*/ ){
                failure();
                template_failure();
                close $TEMPLATE;
                exit 1;
                return;
            }
            if ( $_ =~ /^>/ ){
                $fasta_checker++;
                my $name = $_;
                $name    =~ s/\r\n//g; #Remove return lines from windows OS '^M'
                chomp($name);
                $realSeqName = $name;
                $realSeqName =~ s/^(>.*)_[GS]_.*$/$1/;
                $realSeqName =~ s/^(>.*)_[GS]_.*$/$1/;
                $realSeqName =~ s/ //g;
                $realSeqName = 'wHaT' if ($realSeqName !~ /[\w\>]/);
#                print "\nname = $realSeqName\n";

#SI pas la bonne struct _G_ . _S_ . ...
                my $protTarget = $name;
                $protTarget    =~ s/^.*_S_ *([^ ]+).*$/$1/;
                $protTarget    = '' if ( $protTarget !~ /^[\w\_]+$/ || $protTarget eq 'No_BLASTp_Result' || $protTarget =~ /^My_own_seq$/ );
                %blastpTarget  = (%blastpTarget, $realSeqName => $protTarget);
#                print "PAcc = $blastpTarget{$realSeqName}\n";

                my $nuclSeq = $name;
                $nuclSeq    =~ s/^.*_G_ *([^ ]+).*$/$1/;
                $nuclSeq    = '' if ( $nuclSeq !~ /^[\w\_]+$/ || $nuclSeq eq 'No_nt_link' || $nuclSeq =~ /navailable$/ || $nuclSeq eq 'Failed_Aln' );
                %nucTarget  = (%nucTarget, $realSeqName => $nuclSeq);
                %nucSeq     = (%nucSeq, $realSeqName => '');
#                print "NAcc = $nucTarget{$realSeqName}\n";
            }
            elsif ( $_ !~ /^>/ && exists($nucTarget{$realSeqName}) && $nucTarget{$realSeqName} =~ /^My_Seq$/i ){
                my $seqq = $_;
                $seqq    =~ s/\r\n//g;
                $seqq    =~ s/[\.\-]//g; #for msa with '.' as gap
                $seqq    =~ s/[^A-Za-z\*\n\r]//g; #Remove all the non-gap or non-alphabetic characters from the seq
                chomp($seqq);
                #fasta sequence on 1 line
                if ( $nucSeq{$realSeqName} eq '' ){
                    $nucSeq{$realSeqName} = $seqq;
#                    print "Seq = $seqq\t$fasta_checker\n" if ($seqq ne '');
                }
                else {
                    $nucSeq{$realSeqName} .= $seqq;
#                    print "      $seqq\t$fasta_checker\n" if ($seqq ne '');
                }
            }
            else{
                next READ_TEMPLATE; #For empty lines !?
            }
        }
        close $TEMPLATE;
        if ( $fasta_checker == -1 ){
            failure();
            template_failure();
            exit 1;
            return;
        }

        my ($listProtT, $listNucT, $listNtSeq) = (\%blastpTarget, \%nucTarget, \%nucSeq);
        return($listNucT, $listProtT, $listNtSeq) if ( $fasta_checker >= 0 );
    }
    else{
        return('', '', '');
    }
}


########################################################


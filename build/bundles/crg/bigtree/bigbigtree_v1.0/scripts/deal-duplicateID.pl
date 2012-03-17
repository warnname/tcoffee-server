#!/usr/bin/perl -w 
my $TCOFFEE_CMD = "t_coffee";

if (@ARGV==0)
{
        print "Usage: deal_duplicateID.pl fasta_dic/\n";
        exit (1); 
}

my $dirname = $ARGV[0];
opendir ( DIR, $dirname ) || die "Error in opening dir $dirname\n";
@files = grep(/aa.fasta_aln$/,readdir(DIR));
closedir(DIR);
@files = grep(!/^cluster/,@files);

foreach $file (@files) {
  my %hash_2_name=();
  my %hash_2_value=();
  $nn_aln = $file;
  $nn_aln =~ s/aa.fasta_aln/nn.fasta_aln/;
  
  if (! -e $nn_aln ){
	  print "PROCESS aa_fasta_aln=$dirname/$file\n";
	  print "        nn_fasta_aln=$dirname/$nn_aln\n";
	  print "OUTPUT  fasta_aln=$nn_aln\n\n";
		  
	#calculate pairwise sequence similarity   
	@lines=`$TCOFFEE_CMD -other_pg seq_reformat -in $dirname/$file -output sim|grep AVG`;
	
	#pick up the most similar pairwise sequence similarity
	   foreach $line (@lines){
		   @tmp=split(' ', $line);
		   if($tmp[2] =~ m/(cluster\d+)/)
		   {
			   if ((! $hash_2_name{$1}) || ($tmp[4] > $hash_2_value{$1}))
			   {
				   $hash_2_name{$1}=$tmp[2];
				   $hash_2_value{$1}=$tmp[4];
			   }
		   }
	   }
	
	#extract sequence by similarity   
	   my $list=();
	   foreach $key (keys %hash_2_name)
	   {
		   $list .= "'$hash_2_name{$key}' "; 
	   }
	   system("$TCOFFEE_CMD -other_pg seq_reformat -in $dirname/$nn_aln -action +keep_name +extract_seq_list $list +rm_gap > tmp.fasta_aln");
	   system("sed -i 's/\\(>cluster[0-9]*\\)_[0-9]*/\\1/g' tmp.fasta_aln");
	   system("mv tmp.fasta_aln $nn_aln");  
  }
}

#File loci_from_Exonerate.pm

package loci_from_Exonerate;



sub parser{
    my ($infile, $debug) = @_;
    my %pos_aln;
    my %pos_boj;

    open(my $MAP, '<', "$infile") if ( -e "$infile" ) or die "$!\n\n";
    my $flag = 0;
    my (@query, @match, @target, @genomic);
    my ($Match, $Genom, $Query) = ('', '', '');
    my ($up, $down);
    my $margeL = 0;
    my (@cdsUp);
    my $seuil  = 0; #To limit processing if matches are too bad
    EXONERATE_RES:
    while(<$MAP>){
        if ( $flag==1 && $margeL==0 && $_ =~ /^( +\d+ : )[\w\-\{\} \>\<\*]+ : +\d+$/ ){ #\* for stop codon management
            $margeL = length($1);
        }
        if ( $flag==0 && $_ =~ /^ +Target range: \d+ -> \d+/ ){
            $flag = 1;
        }
        elsif ( $_ =~ /^vulgar:/ ){
            $flag = 8;
        }
        elsif ( $_ =~ /^\# --- END OF GFF DUMP ---/ ){
            last EXONERATE_RES;
        }
        elsif ( $flag==1 && $_ =~ /^ +\d+ : ([\w\-\{\} \>\<\*]+) : +\d+/ ){
            @query = (@query, $_);
            $Query = $Query.$1;
            $flag++;
        }
        elsif ( $flag==2 && $_ =~ /^ +[\|\+\-\{\}\dbp\.\!\:\#]+/ ){
            @match = (@match, $_);
            $_     =~ s/^.{$margeL}//;
            $Match = $Match.$_;
            chomp($Match);
            $flag++;
        }
        elsif ( $flag==3 && $_ =~ /^ +[\w\-\{\}\*\#]+/ ){
            @target = (@target, $_);
            $flag++;
        }
        elsif ( $flag==4 && $_ =~ /^ +(\d+) : ([\w\-\{\}\.]+) : +(\d+)/ ){
            @genomic = (@genomic, $_);
            $up      = $1;
            $Genom   = $Genom.$2;
            $down    = $3;
            $flag    = 1;
        }
        elsif ( $flag==8 && $_ =~ /exonerate:protein2[a-z]+\tsimilarity\t\d+\t/ ){
            my $similarity_query = $_;
            chomp($similarity_query);
            $similarity_query    =~ s/^[^\;]+\; //;
            while( $similarity_query =~ m/^ ?Align (\d+) / ){
                @cdsUp = (@cdsUp, $1);
                $similarity_query =~ s/^ ?Align \d+ \d+ \d+ \;//;
            }
            $flag=9;
        }
    }
    close $MAP;
    print {*STDERR} "[@cdsUp]\n";


    my $drap     = 0;
    my $exonNbr  = 0;
    my ($locus, $lieu) = (0, 0);
    my $frameIntron = 0;
    my $boj      = '';
    my $pep_pos  = '';
    my $accolade = 0;
    MATCHES:
    for(my $i=0; $i<length($Match); $i++){
        my $matched_symbol = substr($Match, $i, 1);
        if ( $drap==3 && $matched_symbol =~ /^[\+\-]$/ ){
            $drap  = 0;
            $locus = 0;
            $exonNbr++;
            print {*STDERR} $matched_symbol, substr($Genom,$i,1), "\t", substr($Query,$i,1), "\n" if ($debug);
        }
        elsif ( $matched_symbol =~ /^[\+\-]$/ ){
            $drap++;
            print {*STDERR} $matched_symbol, substr($Genom,$i,1), "\t", substr($Query,$i,1), "\n" if ($debug);
        }
#       elsif ( $matched_symbol =~ /[ \|\.\!\:]/ && substr($Genom,$i,1) !~ /[\{\}\.a-z\-]/ ){
        elsif ( $matched_symbol =~ /[ \|\.\!\:\#]/ && substr($Genom,$i,1) !~ /[\{\}\.a-z]/ ){
            my $peptide_pos = '';
            $peptide_pos    = $cdsUp[0]+$lieu if (substr($Query,$i,1) =~ /[A-Z\*]/);
            $pep_pos        = $cdsUp[0]+$lieu if (substr($Query,$i,1) =~ /[A-Z\*]/);
            $frameIntron    = 1 if (substr($Query,$i,1) =~ /[A-Z\*]/ && $frameIntron==-1);
            $frameIntron++      if (substr($Query,$i,1) =~ /[a-z]/   && $frameIntron>=1);
            $lieu++             if (substr($Query,$i,1) =~ /[A-Z\*]/);
            my $line = $matched_symbol.substr($Genom,$i,1)."\t".substr($Query,$i,1)."\t".$peptide_pos;
    #Et les mi-mismatches ?
            if ( $matched_symbol eq '|' && $peptide_pos =~ /^\d+$/ ){
                my $triplet = substr($Genom,$i,1);
                my $j = 0;
                if ( substr($Query,$i,1) ne '*' ){
                    while( length($triplet) <3 ){
                        $j++;
                        $triplet = $triplet.substr($Genom,($i+$j),1) if (substr($Genom,($i+$j),1) =~ /[A-Z]/);
                    }
                }
                elsif ( substr($Query,$i,1) eq '*' ){
                    while( length($triplet) <3 ){
                        $j++;
                        $triplet = $triplet.substr($Genom,($i+$j),1) if (substr($Genom,($i+$j),1) =~ /[A-Z]/);
                    }
                    $i = $i+2;
                }
                %pos_aln = (%pos_aln, $peptide_pos => $triplet);
            }
            my $shift = '';
            $shift    = "\t$frameIntron" if ( $frameIntron>0 );
            print {*STDERR} $line, "$shift\n" if ($debug);
            $locus++;
        }
        elsif ( $matched_symbol =~ /\{/ ){
            print {*STDERR} $matched_symbol, substr($Genom,$i,1), "\t", substr($Query,$i,1), "\n" if ($debug);
            $frameIntron = -1;
            $accolade++;
        }
        else{
            print {*STDERR} $matched_symbol, substr($Genom,$i,1), "\t", substr($Query,$i,1), "\n" if ($debug);
            $lieu++ if ( substr($Query,$i,1) =~ /U/ );
            if ( substr($Query,$i,1) =~ /\}/ ){
                if ( $frameIntron==1 ){
                    $boj     = 'o';
                    %pos_boj = (%pos_boj, $pep_pos => $boj);
                }
                elsif ( $frameIntron==2 ){
                    $boj     = 'j';
                    %pos_boj = (%pos_boj, $pep_pos => $boj);
                }
                $frameIntron = -2 if ( $accolade==1 );
                if ( $accolade==2 ){
                    $frameIntron = 0;
                    $accolade    = 0;
                }
                $boj = '';
            }
            elsif ( $matched_symbol =~ /b/ && $frameIntron==0 ){
                $boj     = 'b';
                %pos_boj = (%pos_boj, ($pep_pos+1) => $boj);
            }
            if ( $matched_symbol =~ /p/ ){
                $frameIntron = 0;
                $boj         = '';
            }
        }
    }


    my $refpos_aln = \%pos_aln; #Use perl references to send two variables (mem address for the 2 hashes) instead of 2 hashes,
    my $refpos_boj = \%pos_boj; #and easily, and properly, get them in the main script !
#    return(%pos_aln);
    return($refpos_aln, $refpos_boj);

}


1;

=head2 sub parser

=over

=item Parse Exonerate output to get only exact matching positions

=item my ($exonerate_output_file) = @_;

=back

=cut


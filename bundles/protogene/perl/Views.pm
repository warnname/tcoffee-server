#File Views.pm
package Views;

my %css = ('species'  => qq{font-style: italic;\n},
           'odd'      => qq{background-color: #FFA500;\n}, #oranges
           'oddodd'   => qq{background-color: #FFDAB9;\n}, #peachpuff
           'oddeven'  => qq{background-color: #e8bcad;\n},
           'even'     => qq{background-color: #5CDC4F;\n}, #greens
           'evenodd'  => qq{background-color: #CBFFC6;\n},
           'eveneven' => qq{background-color: #ABCFA7;\n},
           'match_on' => qq{background-color: #BD43EF;\n            font-weight: bolder;\n}, #violet
          );

my $ncbi_nt = 'http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=nuccore&amp;id=';
my $ncbi_aa = 'http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&amp;id=';
my $expasy  = 'http://www.expasy.ch/uniprot/';
my $pdb     = 'http://www.pdb.org/pdb/explore/explore.do?structureId=';

my %species = ( 'barley'    => '',
                'bovine'    => '',
                'cat'       => '',
                'dog'       => '',
                'hiv-1'     => '',
                'hiv1'      => '',
                'hiv'       => '',
                'human'     => '', #All in lowercase
                'maize'     => '',
                'mouse'     => '',
                'potato'    => '',
                'rat'       => '',
                'tobacco'   => '',
                'wheat'     => '',
                'yeast'     => '',
                'zebrafish' => '',
              );
my %nonSpecies = ( 'mRNA'     => '',
                   'cDNA'     => '',
                   'gene'     => '',
                   'strain'   => '',
                   'cultivar' => '',
                   'plasmid'  => '',
                   'sequence' => '',
                 );


sub Html {
    my ($file) = @_;
    return 0 if ( $file !~ /\.cdsP$/ || -z $file || !-r $file );

    open (my $CDSP,    '<', "$file")      or die "\tCannot open '$file'\n";
    open (my $CDSHTML, '>', "$file.html") or die "\tCannot create in '$file.html'\n";
    print {$CDSHTML} &htmlHeader($file);
    {
        my $isOdd    = 'odd';
        my $isSubOdd = 'odd';
        CDS_FILE:
        while(<$CDSP>){
            chomp();
            next CDS_FILE if ( /^$/ );
            s{>}{&gt;}g;
            s{<}{&lt;}g;

            if ( m/ MATCHES_ON (.+)$/) {
                my $match = $1;
                if ( $match =~ /^Genomic sequence/i || $match =~ /^Synthetic construct/i ){
                    $match = '';
                }
                my ($firstM, $secondM) = ('', '');
                if ( $match =~ /^(.+?(v|V)ir(us|al)).*/ ){
                    my $viralMatch = $1;
                    s{MATCHES_ON \Q$viralMatch\E}{<span class='match_on'>MATCHES_ON <span class='species'>$viralMatch</span></span>};
                }
                elsif ( $match =~ /^(PREDICTED:?) ([a-zA-Z_\.\-]+1?) ([\w\.\-]+),? ([^\s]+) */ ){
                    $predicted = $1;
                    $firstM    = $2;
                    $secondM   = $3;
                    $secondM  .= ' '.$4 if ( $secondM eq 'x' );
                    if ( $firstM =~ /\..+$/ || exists($species{lc($firstM)}) || exists($nonSpecies{$secondM}) ){
                        s{MATCHES_ON $predicted $firstM}{<span class='match_on'>MATCHES_ON $predicted <span class='species'>$firstM</span></span>};
                    }
                    else {
                        s{MATCHES_ON $predicted $firstM $secondM}{<span class='match_on'>MATCHES_ON $predicted <span class='species'>$firstM $secondM</span></span>};
                    } #need to generalize s{MATCHES_ON \Q$Match\E}{...}
                } #problem with "MATCHES_ON H.giganteus (Hpga1)" because second match has parentheses => need to manage match per match and not line type then matches inside in _G_X76876 _S_ CAA54203
                elsif ( $match =~ /^\[?([a-zA-Z_\.\-]+1?) ([\w\.\-]+),? ([^\s]+) */ ){ #1? for HIV-1
                    my $firstM  = $1;
                    my $secondM = $2;
                    $secondM   .= ' '.$3 if ( $secondM eq 'x' );
                    if ( $firstM =~ /\..+$/ || exists($species{lc($firstM)}) || exists($nonSpecies{$secondM}) ){
                        s{MATCHES_ON $firstM}{<span class='match_on'>MATCHES_ON <span class='species'>$firstM</span></span>};
                        s{MATCHES_ON \[$firstM}{<span class='match_on'>MATCHES_ON [<span class='species'>$firstM</span></span>};
                    }
                    else {
                        s{MATCHES_ON $firstM $secondM}{<span class='match_on'>MATCHES_ON <span class='species'>$firstM $secondM</span></span>};
                        s{MATCHES_ON \[$firstM $secondM}{<span class='match_on'>MATCHES_ON [<span class='species'>$firstM $secondM</span></span>};
                    }
                }
                else {
                    s{MATCHES_ON }{<span class='match_on'>MATCHES_ON </span>};
                }
            }

            if ( m/^&gt;.*MATCHES_ON / ){
                s{^&gt;([^ ]*)_G_(\w+) }{&gt;$1_G_<a href='$ncbi_nt$2' target='_blank'>$2</a> } if ( !m/_G_My_Seq / );
                if ( m/ _S_ (\w+) / && ! m/ _S_ My_own_seq / ){
                    my $blastHit = $1;
                    if ( $blastHit =~ /^[A-Z][A-Z0-9]{5}$/ ){
                        s{ _S_ (\w+) }{ _S_ <a href='$expasy$1' target='_blank'>$1</a> };
                    }
                    elsif ( $blastHit =~ /^\d[a-z0-9]{3}$/i ){
                        s{ _S_ (\w+) }{ _S_ <a href='$pdb$1' target='_blank'>$1</a> };
                    }
                    else {
                        s{ _S_ (\w+) }{ _S_ <a href='$ncbi_aa$1' target='_blank'>$1</a> };
                    } 
                }
                
                print {$CDSHTML} "<span class='$isOdd$isSubOdd'>", $_, "\n";
                $isSubOdd = $isSubOdd eq 'odd' ? 'even' : 'odd';
            }
            elsif ( m/^&gt;.*_G_revtrans / ){
                print {$CDSHTML} "<span class='$isOdd$isSubOdd'>", $_, "\n";
                $isSubOdd = $isSubOdd eq 'odd' ? 'even' : 'odd';
            }
            elsif ( m/^&gt;.*/ && !m/MATCHES_ON / ){
                
                print {$CDSHTML} "<span class='$isOdd'>", $_, "\n";
                $isOdd    = $isOdd eq 'odd' ? 'even' : 'odd';
                $isSubOdd = 'odd';
            }
            else {
                print {$CDSHTML} $_, "</span>\n";
            }
        }
    }
    print {$CDSHTML} &htmlFooter();
    close $CDSHTML;
    close $CDSP;
    return 1;
}


sub htmlHeader {
    my ($jobName) = @_;
    $jobName =~ s{^.*/([^\/]+?)\.cdsP$}{$1};    

    my $css = '';
    for my $style ( keys(%css) ){
        $css .= "        .$style {
            $css{$style}        }\n";
    }

    return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">
<head>
    <title>ProtoGene xHTML/CSS output for $jobName</title>
    <meta http-equiv='content-type' content='text/html;charset=UTF-8' />
    <meta http-equiv='Content-Style-Type' content='text/css' />
    <style type='text/css'>
$css    </style>
</head>

<body>
    <div align='center'>New '<font color='red'>Beta</font>' CDS output</div>
    <pre>
";
}

sub htmlFooter {
    return "    </pre>
</body>
</html>
";
}

1;


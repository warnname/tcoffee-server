# please read and install the following suite 
# ViennaRNA-1.8.4 [see the INSTALL file]
# other software: awk grep sort sed gnuplot

#!/bin/bash

if [ -z $CATR_HOME ]; then 
  BIN_PATH=`which start.modified.sh`
  BIN_PATH=`dirname $BIN_PATH`
  export CATR_HOME=$BIN_PATH/..
fi


mkdir ./data
mkdir ./database
mkdir ./results
mkdir ./eps
mkdir ./tmp

# takes the sequences (label - sequence)
echo $1  $3
echo $1  $2 > ./database/protein.txt
echo $3  $4 > ./database/rna.txt


# creates the interaction list
echo $1 $3 > ./database/interactions.rna.txt


# filters out small fragments (proteins - rna)
sh select.length.sh 50 750 50 3000

# computes 10 features (concatenated) secondary + polarity + hydro for rna and proteins
sh rna-feature.sh        ./database/rna.50.3000.txt      > ./database/rna.dat
sh protein-feature.sh    ./database/protein.50.750.txt   > ./database/protein.dat

# normalizes the lengths
# change it into adaptator.sh, this is just temporary
sh adaptator.sh        ./database/rna.dat 3001         > ./database/rna.3000.dat
sh adaptator.sh        ./database/protein.dat 751      > ./database/protein.750.dat

# computes fouriers' coefficients
sh fourier.line.sh     ./database/rna.3000.dat         > ./database/fourier.rna.3000.dat
sh fourier.line.sh     ./database/protein.750.dat      > ./database/fourier.protein.750.dat

# creates the inputs
sh picker.slow.sh ./database/interactions.rna.50.750.txt ./database/fourier.rna.3000.dat ./database/fourier.protein.750.dat 200 posi

# calculates the score
sh matrix.sh $1 $3

# creates the plot
sh correct.sh $1 $3

#!/bin/bash
bin_path=`which start.modified.sh`
bin_path=`dirname $bin_path`
params_path=$bin_path/../params


# builds the matrix
sh score.2.sh

# computes the product
awk '(NR==1){for(i=2;i<=NF;i++){a[i]=$i}} (NR>1){for(i=2;i<=NF;i++){s=s+$1*a[i]*$i}} END{print s}' ./tmp/rlc.tmp > ./tmp/score.txt

# visualize the regions 
awk '(NR==1){for(i=2;i<=NF;i++){a[i]=$i}} (NR>1){for(i=2;i<=NF;i++){printf "%f\t",$1*a[i]*$i} printf "\n"}' ./tmp/rlc.tmp  > ./tmp/mat.tmp
cp ./tmp/mat.tmp ./data/ff.$1-$2.mat.txt
rl=`awk '{print length($2)}' ./database/rna.txt`
pl=`awk '{print length($2)}' ./database/protein.txt`
sh fourier.2d.2.sh ./tmp/mat.tmp 3000 750 $rl $pl > ./tmp/out.total.mat
cp ./tmp/out.total.mat ./results/mat.$1.$2.txt

# creates the matrix
awk '{for(i=1;i<=NF;i++){a[NR,i]=$i}}  END{for(i=1;i<=NF;i++){  for(j=1;j<=NR;j++){ printf "%f\t", (a[j,i])} printf "\n"} }'  ./tmp/out.total.mat  > ./results/mat.$1.$2.tr.txt

# calculates the score
cat          ./tmp/score.txt      > ./tmp/analysis.txt
#V=`cat tmp/analysis.txt`
cat $params_path/negatives.txt >> ./tmp/analysis.txt

P=`awk '(NR==1){s=$1} (NR>1){if($1<s){k++}} END{print k/(NR-1)}' tmp/analysis.txt`

# background
awk '{s=0; d=0;  for(i=2;i<=NF;i++){s=s+$i; a[i]=$i} for(i=2;i<=NF;i++){d=d+(a[i]-s/(NF-1))^2;}  print sqrt(d/(NF-1))}' ./database/protein.dat > ./tmp/varp.tmp
awk '{s=s+$1} END{print s/NR}' ./tmp/varp.tmp > ./tmp/varp.2.tmp
awk '{s=0; d=0;  for(i=2;i<=NF;i++){s=s+$i; a[i]=$i} for(i=2;i<=NF;i++){d=d+(a[i]-s/(NF-1))^2;}  print sqrt(d/(NF-1))}' ./database/rna.dat     > ./tmp/varr.tmp
awk '{s=s+$1} END{print s/NR}' ./tmp/varr.tmp > ./tmp/varr.2.tmp
se2=`paste ./tmp/varp.2.tmp ./tmp/varr.2.tmp | awk 'BEGIN{s=0} ($1<0.005)||($2<0.05){s=1} END{print s}' | head -1`

if [ $se2 == 0 ] ; then  echo $P > ./tmp/disc.tmp ; V=`cat tmp/analysis.txt` ; fi
if [ $se2 == 1 ] ; then  echo 0  > ./tmp/disc.tmp ; V=0;  fi

# echo $P > ./tmp/disc.tmp

# plots the distribution
sh  plot.dist.gp $V

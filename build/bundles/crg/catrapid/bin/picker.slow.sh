#!/bin/bash

# creates the datasets
echo "remove" >   ./data/bait.$5
cp ./data/bait.$5 ./data/prey.$5
rm ./data/bait.$5 ./data/prey.$5
cat $2 $3 > ./tmp/data.dat
cat $1 | awk '{print $1"+"$2}' > ./tmp/plus.dat
#
for j in `more ./tmp/plus.dat `; do
#finds
A=`echo $j | sed 's/+/ /g' | awk '{print $1}'`;  
B=`echo $j | sed 's/+/ /g' | awk '{print $2}'`; 
grep -w $A ./tmp/data.dat | awk '{for(i=2;i<='$4'+1;i++){printf "\t%4.2f", $i} printf "\n"}' > ./tmp/tmp.1
grep -w $B ./tmp/data.dat | awk '{for(i=2;i<='$4'+1;i++){printf "\t%4.2f", $i} printf "\n"}' > ./tmp/tmp.2 
#double-checks
AA=`wc ./tmp/tmp.1 | awk '{print $1}'`
BB=`wc ./tmp/tmp.2 | awk '{print $1}'`
awk '('$AA'==10)&&('$BB'==10)' ./tmp/tmp.1  >> ./data/bait.$5
awk '('$AA'==10)&&('$BB'==10)' ./tmp/tmp.2  >> ./data/prey.$5
done

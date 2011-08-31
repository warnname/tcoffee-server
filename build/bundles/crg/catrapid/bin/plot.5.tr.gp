#!/bin/sh
# export GDFONTPATH=$HOME/Library/Fonts:/Library/Fonts:/System/Library/Fonts
cp $2/norm.$1.tr.txt  $2/n.$1.tr.txt
awk '{max=-1000; s=0; for(i=1;i<=NF;i++){if($i>max){max=$i} s=s+$i} print NR,s/NF}' $2/n.$1.tr.txt > ./data/max.prot.txt
awk '{max=-1000; s=0; for(i=1;i<=NF;i++){if($i>max){max=$i} s=s+$i} print NR, s/NF}'$2/n.$1.tr.txt > ./data/max.prot.$1.txt

awk '{for(i=1;i<=NF;i++){a[NR,i]=$i}}  END{ for(i=1;i<=NF;i++){  for(j=1;j<=NR;j++){ printf "%f\t", (a[j,i])} printf "\n"} }' $2/n.$1.tr.txt | awk '{max=-1000; s=0; 
for(i=1;i<=NF;i++){if($i>max){max=$i} s=s+$i} print NR,s/NF}' > ./data/max.rna.txt
awk '{for(i=1;i<=NF;i++){a[NR,i]=$i}}  END{ for(i=1;i<=NF;i++){  for(j=1;j<=NR;j++){ printf "%f\t", (a[j,i])} printf "\n"} }' $2/n.$1.tr.txt | awk '{max=-1000; s=0;
for(i=1;i<=NF;i++){if($i>max){max=$i} s=s+$i} print NR,s/NF}' > ./data.save/max.rna.$1.txt
awk '{for(i=1;i<=NF;i++){print NR,i, $i}}' $2/n.$1.tr.txt > ./tmp/parametrized.txt
#
selected=`sort -n -k 3 ./tmp/parametrized.txt | tail -1 | awk '{print $1}'`
awk '($1=='$selected'){print $2, $3}' ./tmp/parametrized.txt  > ./$2/maxcut.$1.txt
#
pl=`wc      ./$2/n.$1.tr.txt | awk '{print $1}'`
rl=`head -1 ./$2/n.$1.tr.txt | awk '{print NF}'`


gnuplot << EOF

#set cbrange [-3:3]
#set cbrange [0:1]
#set cbrange [-1:1]
#set cbrange [-3.5:3.5]
set terminal jpeg large enhanced  size 600 600
#set terminal svg
set output "$3/n.$1.tr.jpg"
set multiplot
set size 1,1
set origin 0.1,0.15
set size 0.8,0.8
set xrange [1:$rl]
set yrange [1:$pl]
set border 0
set pm3d map 
set palette model RGB defined (0 "blue", 100 "white", 110 "white", 200 "red") 
set ylabel "Protein Residue Index"  -13., 0
set xlabel "RNA Nucleotide Index" 0,-3 
set xtics nomirror 
set ytics nomirror 
#set xtics out
#set ytics out
#set xtics 25
#set ytics 25
#set notics
unset ytics
unset xtics
#set colorbox origin -1, 0  size 1,0
#set colorbox vert user origin .05,.15 size 0.05,0.5
#set cbtics 1
splot "$2/n.$1.tr.txt" matrix notitle
#
unset pm3d
set style fill   solid 1.00 noborder
set xtics out nomirror
unset xlabel
unset ylabel
set border 0
set origin 0.2,0.125
set size 0.60,0.22
set xrange [1:$rl]  
set yrange [*:2] reverse
plot './data/max.rna.txt' u 1:2 w l lc 0  notitle , './data/max.rna.txt' u 1:2 lc 0 with filledcurve y1=0 notitle
#

unset xtics
unset yrange 
set ytics offset -0.1 nomirror 
set origin -0.025,0.28
set size 0.27,0.56
set xrange [*:4.5] reverse
set yrange [1:$pl]
#unset xrange
#unset yrange
plot './data/max.prot.txt' u 2:1 w l lc 0  notitle ,  './data/max.prot.txt' u 2:1 lc 0 with filledcurve y1=0 notitle


EOF

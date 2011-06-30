#!/bin/sh
awk '{for(i=1;i<=NF;i++){printf "%4.2f\t", ($i-0.019)/0.075} printf "\n"}' ./$2/$1.tr.txt > ./$2/n.$1.tr.txt
pl=`wc      ./$2/$1.tr.txt | awk '{print $1}'`
rl=`head -1 ./$2/$1.tr.txt | awk '{print NF}'`


gnuplot << EOF

set cbrange [-1:1.6]
set terminal png giant enhanced size 800 600
set output "./$3/matrix.png"
set xrange [1:$rl]
set yrange [1:$pl]
set border 0
set pm3d map 
#set palette rgbformulae 7,5,15
#set palette model XYZ functions gray**0, gray**0.35, gray**0.1
#set palette model RGB
set palette model RGB defined (0 "blue", 100 "white", 120 "white", 200 "red")
#(0 "green", 1 "dark-green", 1 "yellow", 2 "dark-yellow", 2 "red", 3 "dark-red" )
set ylabel "Protein Residue Index"  
set xlabel "RNA Residue Index"  
set xtics nomirror 
set ytics nomirror 
set xtics out
set ytics out
set xtics 25
set ytics 25
set cbtics 1
splot './$2/n.$1.tr.txt' matrix notitle 
EOF

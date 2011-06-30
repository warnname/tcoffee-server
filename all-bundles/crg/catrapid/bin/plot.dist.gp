#!/bin/sh
bin_path=`which start.modified.sh`
bin_path=`dirname $bin_path`
params_path=$bin_path/../params

v=`echo $1 | awk '{print ($1-62)/46}'`
w=6;

p=`awk '('$1'>$1){k++} END{printf "%i\n",int(k/NR*100) }' $params_path/negatives.txt`
n=`awk '('$1'<$1){k++} END{printf "%i\n",int(k/NR*100) }' $params_path/positives.txt`
x=`echo $v | awk '($1*$1<=3*3){print $1} ($1*$1>=3*3){print $1/sqrt($1*$1)*3}'`
y1=`paste $params_path/posi.histo.txt $params_path/nega.histo.txt  | awk '{r=('$v'-$1)^2; print r,$2 }' | sort -n -k 1 -r | awk '(NF==2)' | tail -1 | awk '{print $2}'`

tx1=`echo $x  | awk '($1<O){print $1-2} ($1>0){print $1+0.125}'`
tx2=`echo $x  | awk '($1<O){print $1-3} ($1>0){print $1+0.125}'`
ty1=`echo $y1 | awk '{print $1+0.125}'`
ty2=`echo $y1 | awk '{print $1+0.235}'`

gnuplot << EOF
set style fill solid 0.50 noborder

set terminal jpeg large enhanced  size 600 600
set output "./eps/dp.jpg"

set xlabel "Interaction Score"
set ylabel "Probability"
set yrange [0:1.1]
set xrange [-$w:$w]
set style line 1 lt 2 lw 1
set arrow from $tx1,$ty1 to $x,$y1   head front nofilled lc -1  linewidth 1.000
set label " your pair\n  dp=$p %"  at first  $tx2, first  $ty2  front
set title "discriminative power (dp) \n with respect to negative interactions"
set arrow 2 to   -$w,0.83 from  -1,0.83 lc 3
set arrow 3 to    $w,0.83 from   0.5,0.83 lc 1
set arrow 4 to   -0.75,0.83 from   0.2,0.83 lc -1
set arrow 5 to    0.2,0.83 from   -0.75,0.83 lc -1

#set arrow 4 to    0.25,0.82 from -0.75,0.82  
set label "weak" at first -0.75, first  0.87 
set label "interacting"     at first    0.5, first  0.87  front textcolor lt 1
set label "non-interacting" at first -$w+1.8, first  0.87  front textcolor lt 3
plot '$params_path/nega.histo.txt' lc 3  with filledcurve y1=0 title "negative interactions", '$params_path/posi.histo.txt' lc 1  with filledcurve y1=0 title "positive interactions", '$params_path/nega.histo.txt' w l lc 3 notitle

EOF
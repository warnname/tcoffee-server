c=`wc ./coefficients.txt | awk '{print $1}'`
# prey is the line
awk '{for(i=1;i<='$c';i++){printf "%f\t", $i} printf "\n"}'  ./data/prey.combined.txt  > ./tmp/line.txt
# bait is the row
awk '{print 1; for(i=1;i<='$c';i++){printf "%f\n", $i}}'     ./data/bait.combined.txt  > ./tmp/row.txt
# builds the matrix
cat   ./tmp/line.txt coefficients.txt > ./tmp/lc.tmp
paste ./tmp/row.txt ./tmp/lc.tmp      > ./tmp/rlc.tmp
# computes the product
more ./tmp/rlc.tmp | awk '(NR==1){for(i=2;i<=NF;i++){a[i]=$i}} (NR>1){for(i=2;i<=NF;i++){s=s+$1*a[i]*$i}} END{print s}'


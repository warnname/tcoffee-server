bin_path=`which start.modified.sh`
bin_path=`dirname $bin_path`
params_path=$bin_path/../params

# combine the coefficients
# for some reasons pro and rna are swapped, this is the correct way of using them
paste $params_path/coefficients.pro.txt ./data/prey.posi | awk '{for(i=2;i<=NF;i++){printf "%f\t", $i*$1} printf "\n"}' | awk '{for(i=1;i<=NF;i++){a[i]=a[i]+$i}} END{for(i=1;i<=NF;i++){printf "%f\t", a[i]} printf "\n"}' > ./data/bait.combined.txt
paste $params_path/coefficients.rna.txt ./data/bait.posi | awk '{for(i=2;i<=NF;i++){printf "%f\t", $i*$1} printf "\n"}' | awk '{for(i=1;i<=NF;i++){a[i]=a[i]+$i}} END{for(i=1;i<=NF;i++){printf "%f\t", a[i]} printf "\n"}' > ./data/prey.combined.txt

c=`wc $params_path/coefficients.txt | awk '{print $1}'`
# prey is the line
awk '{for(i=1;i<='$c';i++){printf "%f\t", $i} printf "\n"}'  ./data/prey.combined.txt  > ./tmp/line.txt
# bait is the row
awk '{print 1; for(i=1;i<='$c';i++){printf "%f\n", $i}}'     ./data/bait.combined.txt  > ./tmp/row.txt
# builds the matrix
cat   ./tmp/line.txt $params_path/coefficients.txt > ./tmp/lc.tmp
paste ./tmp/row.txt ./tmp/lc.tmp      > ./tmp/rlc.tmp

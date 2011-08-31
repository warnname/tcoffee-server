# normalizes
as=`awk '{for(i=1;i<=NF;i++){s=s+sqrt(($i)^2)^2; k++}} END{print s}' ./data/ff.$1-$2.mat.txt`
#
an=`awk '{for(i=1;i<=NF;i++){s=s+$i; k++}} END{print k}' ./data/ff.$1-$2.mat.txt `
bn=`awk '{for(i=1;i<=NF;i++){s=s+$i; k++}} END{print k}' ./results/mat.$1.$2.tr.txt`
#
bs=`awk '{for(i=1;i<=NF;i++){s=s+sqrt(($i)^2)^('$an'/'$bn'); k++}} END{print s}' ./results/mat.$1.$2.tr.txt `

# parceval's theoreme
awk '{for(i=1;i<=NF;i++){s=$i/((('$bs')^2)/('$as')^2); s=(s-0.0000148)/0.000069; t=(s+2.5)/(4.29+2.83); u=(t-0.5)*2; printf "%f\t", s} printf "\n"}' ./results/mat.$1.$2.tr.txt  > ./results/norm.$1.$2.tr.tmp
 
# min-max scaling 
mi=`awk '{for(i=1;i<=NF;i++){print $i}}'   ./results/norm.$1.$2.tr.tmp  | sort -n -k 1 -r | tail -1`
ma=`awk '{for(i=1;i<=NF;i++){print $i}}'   ./results/norm.$1.$2.tr.tmp  | sort -n -k 1    | tail -1`

se=`echo $mi $ma | awk 'BEGIN{s=0} ($1<-3)||($2>3){s=1} END{print s}'`

# background
awk '{s=0; d=0;  for(i=2;i<=NF;i++){s=s+$i; a[i]=$i} for(i=2;i<=NF;i++){d=d+(a[i]-s/(NF-1))^2;}  print sqrt(d/(NF-1))}' ./database/protein.dat > ./tmp/varp.tmp
awk '{s=s+$1} END{print s/NR}' ./tmp/varp.tmp > ./tmp/varp.2.tmp
awk '{s=0; d=0;  for(i=2;i<=NF;i++){s=s+$i; a[i]=$i} for(i=2;i<=NF;i++){d=d+(a[i]-s/(NF-1))^2;}  print sqrt(d/(NF-1))}' ./database/rna.dat     > ./tmp/varr.tmp
awk '{s=s+$1} END{print s/NR}' ./tmp/varr.tmp > ./tmp/varr.2.tmp
se2=`paste ./tmp/varp.2.tmp ./tmp/varr.2.tmp | awk 'BEGIN{s=0} ($1<0.005)||($2<0.05){s=1} END{print s}' | head -1`

if [ $se  == 1 ] ; then
if [ $se2 == 0 ] ; then  fis=`cat ./tmp/disc.tmp` ; fi
if [ $se2 == 1 ] ; then  fis=0; fi
awk '{for(i=1;i<=NF;i++){s=3*(($i-"'$mi'")/("'$ma'"-"'$mi'")-0.5)*2;printf "%f\t", s}  printf "\n"}' ./results/norm.$1.$2.tr.tmp > ./results/norm.$1.$2.tr.txt
fi

if [ $se  == 0 ] ; then
if [ $se2 == 0 ] ; then  fis=1; fi
if [ $se2 == 1 ] ; then  fis=0; fi
fi=`cat ./tmp/disc.tmp`
awk '{for(i=1;i<=NF;i++){printf "%4.2f\t", $i*'$fis'} printf "\n"}' ./results/norm.$1.$2.tr.tmp > ./results/norm.$1.$2.tr.txt
fi




#
# plots
sh plot.4.tr.gp $1.$2 results eps

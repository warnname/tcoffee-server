for j in `cat interactions.txt | grep -v "#" `; do 
# rna is the first entry
i1=`echo $j | sed 's/+/ /g' | awk '{print $1}'`; 
i2=`echo $j | sed 's/+/ /g' | awk '{print $2}'`; 
si1=`awk '($1=="'$i1'"){print $2}' run.interactions.txt` ; 
si2=`awk '($1=="'$i2'"){print $2}' run.interactions.txt` ; 
#protein goes first
sh start.modified.sh $i2 $si2 $i1 $si1 ; 
done  

#l=`awk 'END{print NR}' $1 `; 
#for ((k=0;k<$l;k++)) 
#do 
awk '{s[NR]=$2;} 
END{s[NR]=0;
s[1]=s[NR];  

for(k=0;k<NR;k++){
c=0;
for(i=0;i<NR;i++){
c=c+sqrt(2/NR)*s[i+1]*cos( (3.14/NR)*(k+1/2)*(i+1/2))} print k,c
}
}' $1
#; done 

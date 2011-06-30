awk '{for(i=1;i<=NF;i++){s[NR,i]=$i;}}
 
END{

for(k1=0;k1<'$4';k1++){
for(k2=0;k2<'$5';k2++){

c=0;

for(i1=0;i1<NR;i1++){
for(i2=0;i2<NR;i2++){
f1=cos( (3.14/'$2')*(k1+1/2)*(i1+1/2))*sqrt(2/'$2');
f2=cos( (3.14/'$3')*(k2+1/2)*(i2+1/2))*sqrt(2/'$3');
c=c+s[i1,i2]*f1*f2; } 
 		    }
printf "%4.2f\t",c
		    }
printf "\n"
                    }
}' $1

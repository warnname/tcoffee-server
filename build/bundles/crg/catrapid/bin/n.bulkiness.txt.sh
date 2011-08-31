awk '(NR==1){T=1; N=1; P=1; for(i=1;i<=length($1);i++){
    aminoacid[i]=substr($1,i,1); 
if(aminoacid[i]=="A"){c=0.44335};
if(aminoacid[i]=="C"){c=0.550629};
if(aminoacid[i]=="D"){c=0.453202};
if(aminoacid[i]=="E"){c=0.55665};
if(aminoacid[i]=="F"){c=0.897646};
if(aminoacid[i]=="G"){c=0};
if(aminoacid[i]=="H"){c=0.563218};
if(aminoacid[i]=="I"){c=0.985222};
if(aminoacid[i]=="K"){c=0.673782};
if(aminoacid[i]=="L"){c=0.985222};
if(aminoacid[i]=="M"){c=0.703339};
if(aminoacid[i]=="N"){c=0.515599};
if(aminoacid[i]=="P"){c=0.767926};
if(aminoacid[i]=="Q"){c=0.604817};
if(aminoacid[i]=="R"){c=0.595512};
if(aminoacid[i]=="S"){c=0.332239};
if(aminoacid[i]=="T"){c=0.677066};
if(aminoacid[i]=="V"){c=0.994527};
if(aminoacid[i]=="W"){c=1};
if(aminoacid[i]=="Y"){c=0.800766};
if(aminoacid[i]=="X"){c='0.655036'}
print aminoacid[i],c}}' $1 | awk '{a[NR]=$1; b[NR]=$2} END{
print 1,  b[1]
print 2, (b[1]+b[2]+b[3])/3;
print 3, (b[1]+b[2]+b[3]+b[4]+b[5])/5;
for(i=4;i<=NR-3;i++){A=(b[i-3]+b[i-2]+b[i-1]+b[i]+b[i+1]+b[i+2]+b[i+3])/7; print  i, A}
print NR-2, (b[NR]+b[NR-1]+b[NR-1]+b[NR-3]+b[NR-4])/5;
print NR-1, (b[NR]+b[NR-1]+b[NR-2])/3;
print NR,    b[NR]
 
}'

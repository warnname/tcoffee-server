#!/bin/bash
awk '{for(i=2;i<'$2'+1;i++){a[i]=0} ; for(i=2; i<=NF;i++){a[i]=$i} ; printf "%s\t",$1;  for(i=2; i<='$2'+1;i++){printf "%s\t", a[i]} printf "\n"}' $1

#!/bin/bash
# for some reasons pro and rna are swapped, this is the correct way of using them
paste ./coefficients.pro.txt ./data/prey.posi | awk '{for(i=2;i<=NF;i++){printf "%f\t", $i*$1} printf "\n"}' | awk '{for(i=1;i<=NF;i++){a[i]=a[i]+$i}} END{for(i=1;i<=NF;i++){printf "%f\t", a[i]} printf "\n"}' > ./data/bait.combined.txt
paste ./coefficients.rna.txt ./data/bait.posi | awk '{for(i=2;i<=NF;i++){printf "%f\t", $i*$1} printf "\n"}' | awk '{for(i=1;i<=NF;i++){a[i]=a[i]+$i}} END{for(i=1;i<=NF;i++){printf "%f\t", a[i]} printf "\n"}' > ./data/prey.combined.txt


#! /bin/sh

if [ $# -ne 0 ]; then
         
        stDev=0;
        stError=0;
        awk '{if (NR!=1) {print}}' $@ > temp.txt
        awk -F"," '{print $2}' temp.txt | awk '{sum+=$1} END {print " \nMean = " sum/NR;}'
        awk -F"," '{print $2}' temp.txt | awk '{delta = $1 - avg; avg += delta / NR; mean2 += delta * ($1 - avg); } END { stDev=sqrt(mean2 / NR); stError=stDev/sqrt(NR); 
              print "SDeviation = " stDev " \nSError = " stError; }'

        rm temp.txt
fi;


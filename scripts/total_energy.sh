#! /bin/sh

if [ $# -ne 0 ]; then

        sum=0;
        awk '{if (NR!=1) {print}}' $@ > temp.txt
        awk -F"," '{print $2}' temp.txt | awk '{sum+=$1} END {printf("\nTotal Energy Consumption(mA) =%f \n",sum)}'

        rm temp.txt
fi;

#! /bin/bash
for i in `seq 0 2`; do 
	echo "clientPort=$((2181+$i))" >> conf/zoo$i.cfg
done

#! /bin/bash
for i in `seq 0 2`; do
	bin/zkServer.sh stop conf/zoo$i.cfg
done

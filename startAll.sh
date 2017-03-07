#! /bin/bash
for i in `seq 0 1`; do
	bin/zkServer.sh start conf/zoo$i.cfg
done

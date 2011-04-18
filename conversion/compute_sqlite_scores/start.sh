#!/bin/sh
if [ -f ActiveDaemonPID.pid ];then
echo "Daemon already in progress"
else
java -jar compute_to_sqlite.jar &
pid=$!
echo ${pid} > ActiveDaemonPID.pid
fi
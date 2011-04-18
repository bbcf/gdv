#!/bin/sh
if [ -f ActiveDaemonPID.pid ];then
echo "Daemon already in progress"
else
java -Xmx2048m -jar transform_to_sqlite.jar &
pid=$!
echo ${pid} > ActiveDaemonPID.pid
fi
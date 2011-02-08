#!/bin/sh
if [ -f /project/transform_to_sqlite/ActiveDaemonPID.pid ];then
echo "Daemon already in progress"
else
java -jar /project/transform_to_sqlite/transform_to_sqlite_daemon.jar &
pid=$!
echo ${pid} > /project/transform_to_sqlite/ActiveDaemonPID.pid
fi

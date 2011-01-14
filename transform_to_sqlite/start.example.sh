#!/bin/sh
if [ -f /the/path/ActiveDaemonPID.pid ];then
echo "Daemon already in progress"
else
java -jar /the/path/transform_to_sqlite_daemon.jar &
pid=$!
echo ${pid} > /the/path/ActiveDaemonPID.pid
fi

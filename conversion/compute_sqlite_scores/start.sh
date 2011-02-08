#!/bin/sh
if [ -f /Users/jarosz/git/conversion/compute_sqlite_scores/ActiveDaemonPID.pid ];then
echo "Daemon already in progress"
else
java -jar /Users/jarosz/git/conversion/compute_sqlite_scores/compute_to_sqlite.jar &
pid=$!
echo ${pid} > /Users/jarosz/git/conversion/compute_sqlite_scores/ActiveDaemonPID.pid
fi

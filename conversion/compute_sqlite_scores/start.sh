#!/bin/sh
if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi

GDV_HOME=$1
APP_PATH=$GDV_HOME/compute_sqlite_scores


if [ -f $APP_PATH/ActiveDaemonPID.pid ];then
echo "Daemon already in progress"
else
java -jar $APP_PATH/compute_to_sqlite.jar &
pid=$!
echo ${pid} > $APP_PATH/ActiveDaemonPID.pid
fi
#!/bin/sh

if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi

GDV_HOME=$1
PARAM_GDV=$2
APP_PATH=$GDV_HOME/transform_to_sqlite

if [ -f $APP_PATH/ActiveDaemonPID.pid ];then
echo "Daemon already in progress"
else
java -Xmx2048m -jar $APP_PATH/transform_to_sqlite.jar $PARAM_GDV &
pid=$!
echo ${pid} > $APP_PATH/ActiveDaemonPID.pid
fi
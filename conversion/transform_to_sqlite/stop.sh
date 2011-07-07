#!/bin/sh

if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi

GDV_HOME=$1
APP_PATH=$GDV_HOME/transform_to_sqlite

cat $APP_PATH/ActiveDaemonPID.pid | xargs kill
rm $APP_PATH/ActiveDaemonPID.pid
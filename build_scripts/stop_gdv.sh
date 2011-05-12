#/bin/sh

if [ -z $1]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi
if [ -z $2]; then
    echo 'CATALINA_HOME path is missing : provide the full path to the HOME of TOMCAT'
    exit 1
fi



CUR=$PWD
GDV_HOME=$1
CAT_HOME=$2

cd $GDV_HOME/compute_sqlite_scores/
sh stop.sh

cd $GDV_HOME/transform_to_sqlite/
sh stop.sh

rm $CAT_HOME/webapps/gdv.war

cd $CUR
exit 0
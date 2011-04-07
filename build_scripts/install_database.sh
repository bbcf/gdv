#/bin/sh

CUR=$PWD
if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi
GDV_HOME=$1
SQL_DB=$GDV_HOME/src/main/sql/browser.sql
SQL_CONF_FILE=$GDV_HOME/src/main/webapp/META-INF/gdv.yaml

echo "READ configuration file : $SQL_CONF_FILE ..."

GDV_DB=`awk 'BEGIN { RS = "\n" }; 
{if($1 == "psql_db") print $3;}' $SQL_CONF_FILE` 
if [ -z $GDV_DB ]; then
    echo '$SQL_CONF_FILE not well configurated : missing psql_db param'
    exit 1
fi
echo "database : "$GDV_DB

GDV_USER=`awk 'BEGIN { RS = "\n" }; 
{if($1 == "psql_user") print $3;}' $SQL_CONF_FILE` 
if [ -z $GDV_USER ]; then
    echo '$SQL_CONF_FILE not well configurated : missing psql_user param'
    exit 1
fi
echo "user : "$GDV_USER

GDV_PWD=`awk 'BEGIN { RS = "\n" }; 
{if($1 == "psql_pwd") print $3;}' $SQL_CONF_FILE` 
if [ -z $GDV_PWD ]; then
    echo '$SQL_CONF_FILE not well configurated : missing psql_pwd param'
    exit 1
fi
echo "password : ***** "

echo "### BUILD DATABASE ###"
createdb $GDV_DB
psql $GDV_DB < $SQL_DB

echo "DONE"
echo "WARNING : DATABASE NOT PROTECTED FOR THE MOMENT"
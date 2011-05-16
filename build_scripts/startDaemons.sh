#/bin/sh
if [ -z $GDV_HOME ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project via the export command'
    exit 1
fi
if [ -z $CATALINA_HOME ]; then
    echo 'CATALINA_HOME path is missing : provide the full path to the HOME dir of GDV project via the export command'
    exit 1
fi

CUR=$PWD

HOME_PROJECT=$GDV_HOME


echo 'START ...'

cd $HOME_PROJECT/compute_sqlite_scores/
sh start.sh $HOME_PROJECT 'GDV_HOME'

cd $HOME_PROJECT/transform_to_sqlite/
sh start.sh $HOME_PROJECT 'GDV_HOME'



cd $CUR
exit 0
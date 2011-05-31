#/bin/sh
if [ -z $GDV_HOME ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project via the export command'
    exit 1
fi

CUR=$PWD

HOME_PROJECT=$GDV_HOME


echo 'STOP ...'

cd $HOME_PROJECT/compute_sqlite_scores/
sh stop.sh $HOME_PROJECT

cd $HOME_PROJECT/transform_to_sqlite/
sh stop.sh $HOME_PROJECT


cd $CUR
exit 0
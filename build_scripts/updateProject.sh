#!/bin/sh
##############################################################
# Update gdv project & daemons.
# It should be editable if you don't use default project.
# you must have $GDV_HOME & $CATALINA_HOME in your
# environnement.
##############################################################

#can change here GDV_HOME to another variable
HOME_PROJECT=$GDV_HOME

#can change the version of gdv you are using
GDV_VERSION='gdv'

#temporary directory
TMP_DIR='tmp_build'

#version you wanna update
CURRENT='current'


if [ -z $HOME_PROJECT ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project via the export command'
    exit 1
fi

if [ -z $CATALINA_HOME ]; then
    echo 'CATALINA_HOME path is missing : provide the full path to the HOME dir of GDV project via the export command'
    exit 1
fi

CUR=$PWD


echo "## import new project ..."
#import new tarball
mkdir $HOME_PROJECT/$TMP_DIR
cd $HOME_PROJECT/$TMP_DIR
wget 'http://salt.epfl.ch/java/gdv-archive/'$CURRENT'/gdv.tgz'
tar -xzf gdv.tgz


#copy hook.sh
cp -r $HOME_PROJECT/$TMP_DIR/gdv/scripts/hook.sh $HOME_PROJECT/scripts/.

#execute hook.sh
sh $HOME_PROJECT/scripts/hook.sh $HOME_PROJECT $TMP_DIR $GDV_VERSION $CATALINA_HOME $CUR
exit 0


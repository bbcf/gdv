#/bin/sh
#update gdv project & daemons
#it should be editable if you don't use default project

if [ -z $GDV_HOME ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project via the export command'
    exit 1
fi
if [ -z $CATALINA_HOME ]; then
    echo 'CATALINA_HOME path is missing : provide the full path to the HOME dir of GDV project via the export command'
    exit 1
fi

CUR=$PWD

#can change here GDV_HOME to another variable (for the dev version)
HOME_PROJECT=$GDV_HOME

#import new tarball (can change here the version : current, dev, 1.0.3,....)
mkdir $HOME_PROJECT/'build'
cd $HOME_PROJECT/build
wget 'http://salt.epfl.ch/java/gdv-archive/current/gdv.tgz'
tar -xzf gdv.tgz 

#copy 1st daemon
cp -r $HOME_PROJECT/build/gdv/compute_sqlite_scores $HOME_PROJECT/compute_sqlite_scores
#copy 2nd daemon
cp -r $HOME_PROJECT/build/gdv/transform_to_sqlite $HOME_PROJECT/transform_to_sqlite
#copy WAR archive (change here to the actual version you wanna use : mv ...... ..../webapps/gdv_dev.war)
mv $HOME_PROJECT/build/gdv/bin/gdv.war $CATALINA_HOME/webapps/gdv.war


cd $HOME_PROJECT
rm -rf build/

cd $CUR

exit 0
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

#temporary directory
TMP_DIR='tmp_build'

#gdv version 
GDV_VERSION='gdv'

echo "## import new project ..."
#import new tarball (can change here the version : current, dev, 1.0.3,....)
mkdir $HOME_PROJECT/$TMP_DIR
cd $HOME_PROJECT/$TMP_DIR
wget 'http://salt.epfl.ch/java/gdv-archive/current/gdv.tgz'
tar -xzf gdv.tgz 
echo "## import new daemons ..."
#copy 1st daemon
cp -r $HOME_PROJECT/$TMP_DIR/gdv/compute_sqlite_scores $HOME_PROJECT/.
#copy 2nd daemon
cp -r $HOME_PROJECT/$TMP_DIR/gdv/transform_to_sqlite $HOME_PROJECT/.
echo "## unarchive project ..."
#unarchive archive
cd $HOME_PROJECT/$TMP_DIR/gdv/bin/
jar xf gdv.war
echo "## get old configuration ..."
#copy old yaml configuration script
cp $CATALINA_HOME/webapps/$GDV_VERSION/META-INF/gdv.yaml $HOME_PROJECT/$TMP_DIR/gdv/bin/META-INF/. 
echo "## archive project ..."
#archive it
jar cf gdv.war .
echo "## copy to Tomcat at "$CATALINA_HOME" ..."
#copy WAR archive (change here to the actual version you wanna use : mv ...... ..../webapps/gdv_dev.war)
mv $HOME_PROJECT/$TMP_DIR/gdv/bin/gdv.war $CATALINA_HOME/webapps/$GDV_VERSION.war

echo "## copy scripts ..."
cp -r $HOME_PROJECT/$TMP_DIR/gdv/bin/startDaemons.sh $HOME_PROJECT/bin/.
cp -r $HOME_PROJECT/$TMP_DIR/gdv/bin/stopDaemons.sh $HOME_PROJECT/bin/.
cp -r $HOME_PROJECT/$TMP_DIR/gdv/bin/hook.sh $HOME_PROJECT/bin/.
echo " execute hook ..."
#execute hook
sh $HOME_PROJECT/bin/hook.sh $HOME_PROJECT $TMP_DIR $CUR &
exit 0

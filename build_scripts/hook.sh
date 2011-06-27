#!bin/sh
HOME_PROJECT=$1
TMP_DIR=$2 
GDV_VERSION=$3 
CATALINA_HOME=$4
CUR=$5


#VERSION
echo "updating to version : "
cp $HOME_PROJECT/$TMP_DIR/gdv/VERSION $HOME_PROJECT/.
cat $HOME_PROJECT/VERSION


echo "## importing new daemons ..."
#copy 1st daemon
cp -r $HOME_PROJECT/$TMP_DIR/gdv/compute_sqlite_scores $HOME_PROJECT/.

#copy 2nd daemon
cp -r $HOME_PROJECT/$TMP_DIR/gdv/transform_to_sqlite $HOME_PROJECT/.


echo "## importing gfeatminer server ... "
cp -r $HOME_PROJECT/$TMP_DIR/gdv/post_to_gminer $HOME_PROJECT/.


echo "## importing new scripts ... " 
cp $HOME_PROJECT/$TMP_DIR/gdv/scripts/startDaemons.sh $HOME_PROJECT/scripts/.
cp $HOME_PROJECT/$TMP_DIR/gdv/scripts/stopDaemons.sh $HOME_PROJECT/scripts/.





echo "## getting old configuration ... (gdv.yaml)"
#copy old yaml configuration script
cp $CATALINA_HOME/webapps/$GDV_VERSION/META-INF/gdv.yaml $HOME_PROJECT/$TMP_DIR/gdv/bin/META-INF/.



echo "## archive project ..."

cd $HOME_PROJECT/$TMP_DIR/gdv/bin
#archive it
jar cf gdv.war META-INF/ WEB-INF/



echo "## copy to Tomcat at "$CATALINA_HOME/webapps" ..."
#copy WAR archive
mv $HOME_PROJECT/$TMP_DIR/gdv/bin/gdv.war $CATALINA_HOME/webapps/$GDV_VERSION.war


#delete tmp directory
rm -r  $HOME_PROJECT/$TMP_DIR/
cd $CUR

echo "Don't forget to restart the daemons (stopDaemons.sh , wait , startDaemons.sh)"
exit 0
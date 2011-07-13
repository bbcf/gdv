#!bin/sh
HOME_PROJECT=$1
TMP_DIR=$2
GDV_VERSION=$3
CATALINA_HOME=$4
CUR=$5
SERV=$6

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


echo "## reading some parameters ..."
SERV=`awk 'BEGIN { FS=" : " } ; {if($1=="gdv_proxy_url")print $2}' < $HOME_PROJECT/$TMP_DIR/gdv/bin/META-INF/gdv.yaml`
if [ -z $SERV ]; then
    echo 'gdv_proxy_url is missing in the conf file (gdv.yaml): exiting ...'
    exit 1
fi
echo "## service accessible at "$SERV

echo "## modify javascript files"
sed -i 's|var _GDV_PROXY="http://paprika.epfl.ch";|var _GDV_PROXY="'$SERV'";|g' $HOME_PROJECT/$TMP_DIR/gdv/public/jbrowse/javascript/js/gdv.js
sed -i 's|var _GDV_URL=_GDV_PROXY+"/gdv";|var _GDV_URL=_GDV_PROXY+"/'$GDV_VERSION'";|g' $HOME_PROJECT/$TMP_DIR/gdv/public/jbrowse/javascript/js/gdv.js

echo "## import them ..."
rm -rf $HOME_PROJECT/public
cp -r $HOME_PROJECT/$TMP_DIR/gdv/public $HOME_PROJECT/.
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

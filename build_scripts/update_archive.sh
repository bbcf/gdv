#!/bin/sh
#####################################################################
# This script will make a tarball of GDV project.
# You will have to copy it to the right location after.
# (e.g. http://salt.epfl.ch/java/gdv-archive/current/gdv.tgz)
# Launch it with $sh update_archive.sh $GDV_HOME $VERSION
######################################################################

CUR=$PWD

if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi
GDV_HOME=$1



if [ -z $2 ];then 
    echo "must provide a version number"
    exit 1
fi
VERSION=$2

cd $GDV_HOME

#temporary directory
TMP_DIR="tmp_build"
mkdir $TMP_DIR
mkdir $TMP_DIR/$VERSION



echo "#############################################################################################################"
echo "######################                          BUILD TREE                        ###########################"
echo "#############################################################################################################"

#build tree
sh $GDV_HOME/build_scripts/build_tree.sh $GDV_HOME $GDV_HOME/$TMP_DIR/$VERSION
mkdir $TMP_DIR/$VERSION/gdv/sql
mkdir $TMP_DIR/$VERSION/gdv/scripts

echo "#############################################################################################################"
echo "######################                          BUILD WAR                         ###########################"
echo "#############################################################################################################"

#build war archive & daemons 
mkdir $TMP_DIR/$VERSION/gdv/bin

#archive project
cd $GDV_HOME
mvn -U clean package
mv target/gdv-$VERSION.war target/gdv.war
chmod 777 target/gdv.war
cp target/gdv.war $TMP_DIR/$VERSION/gdv/bin/.
cd $TMP_DIR/$VERSION/gdv/bin
jar xvf gdv.war
rm gdv.war
rm META-INF/gdv.yaml
cd ../../../..

#copy scripts to bin directory
cp build_scripts/startDaemons.sh $TMP_DIR/$VERSION/gdv/scripts/.
cp build_scripts/stopDaemons.sh $TMP_DIR/$VERSION/gdv/scripts/.
cp build_scripts/hook.sh $TMP_DIR/$VERSION/gdv/scripts/.

cp src/main/sql/browser.sql $TMP_DIR/$VERSION/gdv/sql/.



#add VERSION
echo $VERSION > $TMP_DIR/$VERSION/gdv/VERSION

echo "#############################################################################################################"
echo "######################                       BUILD DAEMON  1                      ###########################"
echo "#############################################################################################################"

#archive daemons
cd conversion/compute_sqlite_scores
ant jar

cp compute_to_sqlite.jar $GDV_HOME/$TMP_DIR/$VERSION/gdv/compute_sqlite_scores/.
cp start.sh $GDV_HOME/$TMP_DIR/$VERSION/gdv/compute_sqlite_scores/.
cp stop.sh $GDV_HOME/$TMP_DIR/$VERSION/gdv/compute_sqlite_scores/.
cp -r lib $GDV_HOME/$TMP_DIR/$VERSION/gdv/compute_sqlite_scores/.

echo "#############################################################################################################"
echo "######################                        BUILD DAEMON  2                     ###########################"
echo "#############################################################################################################"

cd $GDV_HOME/conversion/transform_to_sqlite
ant jar
cp transform_to_sqlite.jar $GDV_HOME/$TMP_DIR/$VERSION/gdv/transform_to_sqlite/.
cp start.sh $GDV_HOME/$TMP_DIR/$VERSION/gdv/transform_to_sqlite/.
cp stop.sh $GDV_HOME/$TMP_DIR/$VERSION/gdv/transform_to_sqlite/.
cp -r lib $GDV_HOME/$TMP_DIR/$VERSION/gdv/transform_to_sqlite/.

echo "#############################################################################################################"
echo "######################                        ARCHIVE TREE                        ###########################"
echo "#############################################################################################################"

#archive tree
cd $GDV_HOME/$TMP_DIR/$VERSION
tar -czf gdv.tgz gdv
rm -r $GDV_HOME/$TMP_DIR/$VERSION/gdv
echo "done ... "




cd $CUR
exit 0
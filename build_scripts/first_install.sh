#/bin/sh
function isok {
    if [ $1 != 0 ]; then
	echo "must exit due to error"
	exit $1
    fi
}
CUR=$PWD
if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi
GDV_HOME=$1

BUILD_FILE=$GDV_HOME/build_scripts/gdv.properties

INSTALL_PATH=`awk 'BEGIN { RS = "\n" }; 
{if($1 == "gdv.working.directory") print $3;}' $BUILD_FILE`
echo $INSTALL_PATH

if [ -z $INSTALL_PATH ]; then
    echo '$BUILD_FILE not well configurated : missing gdv.working.directory param'
    exit 1
fi

echo "##################################"
echo "### WRITING CONFIGURATION FILE ###"
echo "##################################"
echo""
ant build
mv gdv.yaml $GDV_HOME/src/main/webapp/META-INF/gdv.yaml
isok $?
mv web.xml $GDV_HOME/src/main/webapp/WEB-INF/web.xml
isok $?
mv pom.xml $GDV_HOME/pom.xml
isok $?


echo "#####################"
echo "### BUILDING TREE ###"
echo "#####################"
echo ""

mkdir $INSTALL_PATH
cd $INSTALL_PATH
isok $?

#building directories
PUBLIC_DIR="public"
JBROWSE_DIR="jbrowse"
FILE_DIR="files"
TRACK_DIR="tracks"
mkdir $FILE_DIR
mkdir $FILE_DIR/tmp
mkdir $FILE_DIR/DAS
mkdir log
mkdir $PUBLIC_DIR
mkdir $PUBLIC_DIR/$JBROWSE_DIR
mkdir $PUBLIC_DIR/$JBROWSE_DIR/javascript
mkdir $TRACK_DIR
isok $?
cd $GDV_HOME
#clone jbrowse project
git clone git://github.com/bbcf/jbrowse.git
cd jbrowse
isok $?
#switch on the right branch
git checkout gdv
isok $?
#copy static files
cp -r css $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR
cp -r js $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR/javascript
cp -r jslib $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR/javascript
cp -r img  $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR
cp -r $GDV_HOME/src/main/img $PUBLIC_DIR
cp -r $GDV_HOME/src/main/css $PUBLIC_DIR
isok $?





echo "#########################"
echo "### INSTALLING DAMONS ###"
echo "#########################"
echo""
#instal daemons
cd $GDV_HOME
sh build_scripts/install_daemons.sh $GDV_HOME $FILE_DIR $TRACK_DIR
isok $?
cp -r conversion/compute_sqlite_scores $INSTALL_PATH
cp -r conversion/transform_to_sqlite $INSTALL_PATH
isok $?

#install db
echo "###########################"
echo "### INSTALLING DATABASE ###"
echo "###########################"
echo""
sh build_scripts/install_database.sh $GDV_HOME
isok $?
#install project
echo "#########################"
echo "### ARCHIVING PROJECT ###"
echo "#########################"
echo""
sh build_scripts/install_local_ressources.sh $GDV_HOME
isok $?
echo "#!/bin/sh" > build_scripts/archive_project.sh
echo "CUR=\$PWD" >> build_scripts/archive_project.sh
echo "if [ -z \$1 ]; then" >> build_scripts/archive_project.sh
echo "    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'" >> build_scripts/archive_project.sh
echo "    exit 1" >> build_scripts/archive_project.sh
echo "fi" >> build_scripts/archive_project.sh
echo "GDV_HOME=\$1" >> build_scripts/archive_project.sh
echo "cd \$GDV_HOME" >> build_scripts/archive_project.sh
echo "mvn clean package" >> build_scripts/archive_project.sh
echo "cd \$CUR" >> build_scripts/archive_project.sh
isok $?


echo "############"
echo "### DONE ###"
echo "############"



cd $CUR
exit $?

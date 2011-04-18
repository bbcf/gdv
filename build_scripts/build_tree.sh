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
if [ -z $2 ]; then
    echo 'INSTALL_PATH is missing : provide the full path where you want to install the project'
    exit 1
fi

INSTALL_PATH=$2



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
git checkout origin/gdv -b gdv
isok $?
#copy static files
cp -r css $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR
cp -r js $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR/javascript
cp -r jslib $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR/javascript
cp -r img  $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR
cp -r $GDV_HOME/src/main/img $PUBLIC_DIR
cp -r $GDV_HOME/src/main/css $PUBLIC_DIR
isok $?
 
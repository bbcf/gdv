#/bin/sh

CCUR=$PWD

GDV_HOME=$1
INSTALL_PATH=$2"/gdv"


mkdir $INSTALL_PATH
cd $INSTALL_PATH


#building directories
PUBLIC_DIR="public"
JBROWSE_DIR="jbrowse"
FILE_DIR="files"
TRACK_DIR="tracks"
GFEATMINER_DIR="gFeatMiner"
FASTA_DIR="fasta"
TMP_DIR="tmp"

mkdir $FILE_DIR
mkdir $FILE_DIR/tmp
mkdir $FILE_DIR/DAS
mkdir log
mkdir $PUBLIC_DIR
mkdir $PUBLIC_DIR/$JBROWSE_DIR
mkdir $PUBLIC_DIR/$JBROWSE_DIR/javascript
mkdir $TRACK_DIR
mkdir $GFEATMINER_DIR
mkdir $FASTA_DIR
mkdir compute_sqlite_scores
mkdir transform_to_sqlite
mkdir $TMP_DIR

cd $TMP_DIR


#COPY JBROWSE STATICS FILES

#clone jbrowse project
git clone git://github.com/bbcf/jbrowse.git
cd jbrowse

#switch on the right branch
git checkout origin/gdv -b gdv

#copy static files
cp -r css $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR
cp -r js $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR/javascript
cp -r jslib $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR/javascript
cp -r img  $INSTALL_PATH/$PUBLIC_DIR/$JBROWSE_DIR


#COPY GDV STATICS FILES

#clone gdv project

cp -r $GDV_HOME/src/main/img $INSTALL_PATH/$PUBLIC_DIR
cp -r $GDV_HOME/src/main/css $INSTALL_PATH/$PUBLIC_DIR

cd $INSTALL_PATH
#remove temporary directory
rm -r $TMP_DIR

cd $CCUR

exit 0

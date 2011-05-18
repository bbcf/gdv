#/bin/sh
HOME_PROJECT=$1
TMP_DIR=$2
CUR=$3

#copy build_scripts                                                                                                                                                                               
cp $HOME_PROJECT/$TMP_DIR/gdv/bin/updateProject.sh $HOME_PROJECT/bin/. 
echo " remove temporary build "$TMP_DIR" ..."
rm -rf $HOME_PROJECT/$TMP_DIR/
cd $CUR
exit 0
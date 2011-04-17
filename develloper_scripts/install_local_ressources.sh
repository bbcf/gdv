#/bin/sh

CUR=$PWD
if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi
GDV_HOME=$1
echo "##### DOWNLOAD LOCAL RESSOURCES #####"

echo "# clean..."
rm -r $GDV_HOME/resources
echo"# get ressources..."
mkdir $GDV_HOME/resources

cd $GDV_HOME/resources
echo "----- json -----"
wget http://salt.epfl.ch/javalib/json.jar
echo "----- dasobert -----"
wget http://salt.epfl.ch/javalib/dasobert.jar
echo "----- biojava -----"
wget http://salt.epfl.ch/javalib/biojava.jar
echo "----- genrepaccess -----"
wget http://salt.epfl.ch/javalib/bbcfutils.jar
echo "----- tequila -----"
wget http://tequila.epfl.ch/download/2.0/tequila-java-client-2.0.1.tgz
tar -xvzf tequila-java-client-2.0.1.tgz 
cp build/tequila-2.0.1/clients/java/tequila-client-2.0.1.jar .
rm -r build
rm tequila-java-client-2.0.1.tgz
echo "# DONE"
echo $GDV_HOME
cd $GDV_HOME
echo "##### INSTALLING LOCAL RESSOURCES ON LOCAL MAVEN REPOSITORY #####"
mvn install:install-file -DgroupId=ch.epfl -DartifactId=tequila-client -Dversion=2.0.1 -Dpackaging=jar -Dfile=$GDV_HOME/resources/tequila-client-2.0.1.jar
mvn install:install-file -DgroupId=ch.epfl -DartifactId=bbcfutils -Dversion=1 -Dpackaging=jar -Dfile=$GDV_HOME/resources/bbcfutils.jar
mvn install:install-file -DgroupId=org.biojava -DartifactId=biojava -Dversion=1 -Dpackaging=jar -Dfile=$GDV_HOME/resources/biojava.jar
mvn install:install-file -DgroupId=dasobert -DartifactId=dasobert -Dversion=1 -Dpackaging=jar -Dfile=$GDV_HOME/resources/dasobert.jar
mvn install:install-file -DgroupId=org.json -DartifactId=json -Dversion=1 -Dpackaging=jar -Dfile=$GDV_HOME/resources/json.jar


cd $CUR

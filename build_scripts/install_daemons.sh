#/bin/sh

CUR=$PWD
if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi
if [ -z $2 ]; then
    echo 'FILE_DIR path is missing : provide the full path to the tmp dir of GDV project'
    exit 1
fi
if [ -z $3 ]; then
    echo 'TRACK_DIR path is missing : provide the full path to the tmp dir of GDV project'
    exit 1
fi

#variables
GDV_HOME=$1
FILE_DIR=$2
TRACK_DIR=$3
COMP_HOME=$GDV_HOME/conversion/compute_sqlite_scores
TRANS_HOME=$GDV_HOME/conversion/transform_to_sqlite
POST_URL="http://svitsrv25.epfl.ch/gdv/post"

#install transform
echo "### INSTALL TRANSFORM TO SQLITE ###"
echo " ARCHIVE DAEMON "
cd $TRANS_HOME
#jar the project
ant jar
#write start & stop scripts
echo "#!/bin/sh" > start.sh
echo "if [ -f ${TRANS_HOME}/ActiveDaemonPID.pid ];then" >> start.sh
echo "echo 'Daemon already in progress'">> start.sh
echo "else">> start.sh
echo "java -jar ${TRANS_HOME}/transform_to_sqlite.jar &">> start.sh
echo "pid=\$!">> start.sh
echo "echo \${pid} > ${TRANS_HOME}/ActiveDaemonPID.pid">> start.sh
echo "fi" >> start.sh
chmod 755 start.sh

echo "#!/bin/sh" > stop.sh
echo "cat ${TRANS_HOME}/ActiveDaemonPID.pid | xargs kill" >> stop.sh
chmod 755 stop.sh
#write configuration file
mkdir conf
echo "# SQLite databases output directory" > conf.yaml
echo "sqlite_output_directory : ${GDV_HOME}/${FILE_DIR}" >> conf.yaml
echo "# JSON or calculated SQLite output directory" >> conf.yaml
echo "jbrowse_output_directory : ${GDV_HOME}/${TRACK_DIR}" >> conf.yaml
echo "# Where calculated SQLite database is" >> conf.yaml
echo "compute_sqlite_scores_database : ${TRANS_HOME}/jobs.db" >> conf.yaml
echo "# an url if you want feedback" >> conf.yaml
echo "feedback_url : ${POST_URL}" >> conf.yaml
echo "# where are various databases (chromosome names equivalence, yeat common and alternatives names)" >> conf.yaml
echo "database_link : /project/databases" >> conf.yaml
echo "# the url where jbrowse will take the ressources (urlTemplate for HistogramMeta in trackData.json)" >> conf.yaml
echo "jbrowse_ressource_url : ../../${TRACK_DIR}" >> conf.yaml
#install compute
echo "### INSTALL COMPUTE TO SQLITE SCORES ###"
echo " ARCHIVE DAEMON "
cd $COMP_HOME
#jar the project
ant jar
#write start & stop scripts
echo "#!/bin/sh" > start.sh
echo "if [ -f ${COMP_HOME}/ActiveDaemonPID.pid ];then" >> start.sh
echo "echo 'Daemon already in progress'">> start.sh
echo "else">> start.sh
echo "java -jar ${COMP_HOME}/compute_to_sqlite.jar &">> start.sh
echo "pid=\$!">> start.sh
echo "echo \${pid} > ${COMP_HOME}/ActiveDaemonPID.pid">> start.sh
echo "fi" >> start.sh
chmod 755 start.sh

echo "#!/bin/sh" > stop.sh
echo "cat ${COMP_HOME}/ActiveDaemonPID.pid | xargs kill" >> stop.sh
chmod 755 stop.sh
#write configuration file
mkdir conf
echo "# path to a temporary directory (cannot be .)" > conf.yaml
echo "tmp_directory : ${GDV_HOME}/${FILE_DIR}/tmp" >> conf.yaml
echo "# an url if you want feedback" >> conf.yaml
echo "feedback_url : ${POST_URL}" >> conf.yaml

cd $CUR

exit $?

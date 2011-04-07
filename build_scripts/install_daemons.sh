#/bin/sh

CUR=$PWD
if [ -z $1 ]; then
    echo 'GDV_HOME path is missing : provide the full path to the HOME dir of GDV project'
    exit 1
fi
GDV_HOME=$1
COMP_HOME=$GDV_HOME/conversion/compute_sqlite_scores
TRANS_HOME=$GDV_HOME/conversion/transform_to_sqlite

echo "### INSTALL TRANSFORM TO SQLITE ###"
echo " ARCHIVE DAEMON "
cd $TRANS_HOME
ant jar
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
ant clean
echo "### INSTALL COMPUTE TO SQLITE SCORES ###"
echo " ARCHIVE DAEMON "
cd $COMP_HOME
ant jar
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

ant clean

cd $CUR
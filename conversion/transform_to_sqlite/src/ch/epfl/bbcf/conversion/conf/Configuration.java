package ch.epfl.bbcf.conversion.conf;


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.daemon.Launcher;

public class Configuration {


	private static Configuration instance;
	private String workingDir;


	public static Logger logger;
	private static final String RELATIVE_PATH="transform_to_sqlite";
	private Configuration(){
	}

	public enum Extension {WIG,BEDGRAPH,BED,GFF,GTF,BAM};
	public static final String MD5_COMMAND = "md5sum";
	public static final String DAEMON_FILE = "/ActiveDaemonPID.pid";
	public static final String JOBS_FILE = "/jobs.db";
	public static final String LOG_FILE = "/sqlite.log";
	private static String gdvHome;

	public static boolean init(String gdv_home) {
		if(instance==null){
			synchronized(Configuration.class){
				instance = new Configuration();
			}
		}
		gdvHome = System.getenv(gdv_home);
		instance.workingDir = gdvHome+"/"+RELATIVE_PATH;
		logger = Launcher.initLogger(Configuration.class.getName());
		return instance!=null;
	}

	public static Logger getLoggerInstance(){
		return logger;
	}
	public static String getJobsFile(){
		return instance.workingDir+JOBS_FILE;
	}
	public static String getLogger(){
		return  gdvHome+"/"+RELATIVE_PATH+"/"+LOG_FILE;
	}

	public static boolean initJobsDatabase(File jobs) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC").newInstance();
		Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+jobs.getAbsolutePath());
		Statement stat = conn.createStatement();
		stat.execute("create table jobs " + 
				"(file text, " +
				"trackId integer," + //the trackId in gdv database
				"tmpdir text," + //a tmp directory to delete after usage 
				"extension text," + //extension of the file
				"mail text," + // the mail to feedback (nomail if no feedback)
				"nrassemblyid integer," +
				"outdir text," +
				"jbrowse_outdir text," +
				"jbrowse_ressource_url text," +
				"feedback_url text);");  // the assembly id of the genome in generep

		return true;
	}


	public static String getWorkingDirectory(){
		return instance.workingDir;
	}
}

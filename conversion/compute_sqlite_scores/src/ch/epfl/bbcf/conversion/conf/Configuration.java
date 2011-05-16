package ch.epfl.bbcf.conversion.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;
import org.yaml.snakeyaml.Yaml;



public class Configuration {



	public static final Logger logger = initLogger(Configuration.class.getName());

	private static final String RELATIVE_PATH="compute_sqlite_scores";
	//public static final String CONF_FILE = "/conf/conf.yaml";
	public static final String DAEMON_FILE = "ActiveDaemonPID.pid";
	public static final String LOG_FILE = "compute_scores.log";
	public static final String JOBS_DATABASE = "jobs.db";

	private Configuration(){}
	private static Configuration instance;

	private String workingDir;
	private static String gdvHome;

	public static boolean init(String gdvHome) {
		gdvHome = System.getenv(gdvHome);
		if(instance==null){
			synchronized(Configuration.class){
				instance = new Configuration();
			}
		}
		instance.workingDir = gdvHome+"/"+RELATIVE_PATH;
		return instance!=null;
	}


	public static String getWorkingDirectory(){
		return instance.workingDir;
	}
	public static String getLogger(){
		return gdvHome+"/"+RELATIVE_PATH+"/"+LOG_FILE;
	}

	public static Logger initLogger(String name) {
		Logger out = Logger.getLogger(name);
		out.setAdditivity(false);
		out.setLevel(Level.DEBUG);
		SimpleLayout layout = new SimpleLayout();
		RollingFileAppender appender = null;
		try {
			if(out.getAppender(name)!=null){
				appender = (RollingFileAppender) out.getAppender(name);
			} else {
				appender = new RollingFileAppender(layout,Configuration.getLogger(),true);
				appender.setName(name);
			}
		} catch (IOException e) {
			logger.error(e);
		}
		out.addAppender(appender);
		return out;
	}








	public static boolean initJobsDatabase(File jobs) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC").newInstance();
		Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+jobs.getAbsolutePath());
		Statement stat = conn.createStatement();
		return stat.execute("create table jobs " + 
				"(trackId integer, " + //the trackId in gdv database
				"indb text," +  //the name of your input sqlite database
				"inpath text," + //the path where to fetch the sqlite database 
				"outdb text," + //the name of your output directory
				"outpath text," + //the path of the output
				"feedback_url text,"+
				"tmp_dir text,"+
				"rapidity integer," + //0 : for small files , 1 for big
		"mail text);");  // the mail to feedback (nomail if no feedback)

	}


}

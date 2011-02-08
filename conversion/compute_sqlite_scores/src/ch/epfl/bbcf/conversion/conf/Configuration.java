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

	
	public static final String CONF_FILE = "/conf/conf.yaml";
	public static final String DAEMON_FILE = "ActiveDaemonPID.pid";
	public static final String LOG_FILE = "compute_scores.log";
	public static final String JOBS_DATABASE = "jobs.db";

	private Configuration(){}
	private static Configuration instance;

	private String workingDir;
	private String post_url,tmp_directory;

	public static boolean init() {
		if(instance==null){
			synchronized(Configuration.class){
				instance = new Configuration();
			}
		}
		instance.workingDir = System.getProperty("user.dir");
		return readYAMLFile(instance.workingDir+"/"+CONF_FILE);
	}

	private static boolean readYAMLFile(String yamlPath) {
		logger.info("reading configuration file");
		InputStream input = null;
		try {
			input = new FileInputStream(new File(yamlPath));
		} catch (FileNotFoundException e) {
			logger.error(e);
			return false;
		}
		if(null!=input){
			Yaml yaml = new Yaml();
			Map<String, String> data = (Map<String, String>)yaml.load(input);
			for(Map.Entry<String, String> entry : data.entrySet()){
				if(entry.getKey().equalsIgnoreCase("tmp_directory")){
					instance.tmp_directory = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("feedback_url")){
					instance.post_url = entry.getValue();
				} else {
					logger.warn("key : "+entry.getKey()+" not recognized");
				}
			}
		}
		if(null!=instance.tmp_directory &&
				null!=instance.post_url){
			logger.info("good conf file :D");
			return true;
		}
		return false;
	}




	public static String getFeedbackUrl(){
		return instance.post_url;
	}
	public static String getTmpDirectory(){
		return instance.tmp_directory;
	}
	public static String getWorkingDirectory(){
		return instance.workingDir;
	}
	public static String getLogger(){
		return System.getProperty("user.dir")+"/"+LOG_FILE;
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
		stat.execute("create table jobs " + 
				"(trackId text, " + //the trackId in gdv database
				"indb text," +  //the name of your input sqlite database
				"inpath text," + //the path where to fetch the sqlite database 
				"outdb text," + //the name of your output directory
				"outpath text," + //the path of the output
				"rapidity text," + //0 : for small files , 1 for big
		"mail text);");  // the mail to feedback (nomail if no feedback)

		return true;
	}


}

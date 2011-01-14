package ch.epfl.bbcf.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import ch.epfl.bbcf.daemon.Daemon;
import ch.epfl.bbcf.daemon.Launcher;

public class Configuration {


	private static Configuration instance;
	private String workingDir;
	private String sqliteOutputDirectory;
	private String jbrowseOutputDirectory;
	private String computeSqliteScoresDatabase;
	private String feedbackUrl;
	private String databasesLinksDirectory;
	private String jbrowseRessourcesUrl;

	public static final Logger logger = Launcher.initLogger(Configuration.class.getName());

	private Configuration(){
	}

	public enum Extension {WIG,BEDGRAPH,BED,GFF,GTF};
	public static final String MD5_COMMAND = "md5sum";
	public static final String CONF_FILE = "/conf/conf.yaml";
	public static final String DAEMON_FILE = "/ActiveDaemonPID.pid";
	public static final String JOBS_FILE = "/jobs.db";

	//public final static String RESSOURCES_URL = "../resources/org.apache.wicket.Application//";
	//NATACHA
	//public static final String GDV_VERSION ="gdv_dev"; 

	//public static final String DAEMON_DIR="/data/"+GDV_VERSION+"/transform_to_sqlite"; 
	//public static final String GDV_DIR="/data/"+GDV_VERSION;

	//public static final String FILES_DIRECTORY = GDV_DIR+"/files";
	//public static final String TRACKS_DIRECTORY = GDV_DIR+"/tracks";
	//public static final String TMP_DIRECTORY = GDV_DIR+"/files/tmp";


	//	public static final String COMPUTE_SQLITE_SCORES_DATABASE =GDV_DIR+"/compute_sqlite_scores/jobs.db";
	//	public static final String DAEMON_FILE = DAEMON_DIR+"/ActiveDaemonPID.pid";
	//	//public static final String LOG_FILE = DAEMON_DIR+"/daemon.log";
	//	public static final String SQLITE_LOG_FILE = DAEMON_DIR+"/sqlite.log";
	//	public static final String DATABASE = DAEMON_DIR+"/jobs.db"; 
	//	
	//	public static final String POST_URL = "http://svitsrv25.epfl.ch/"+GDV_VERSION+"/post";
	//	public final static String DATABASES_LINKS = "/data/"+GDV_VERSION+"/databases_link";


	public static boolean init() {
		if(instance==null){
			synchronized(Configuration.class){
				instance = new Configuration();
			}
		}
		instance.workingDir = System.getProperty("user.dir");
		return readYAMLFile(instance.workingDir+CONF_FILE);
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
				if(entry.getKey().equalsIgnoreCase("sqlite_output_directory")){
					instance.sqliteOutputDirectory = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("jbrowse_output_directory")){
					instance.jbrowseOutputDirectory = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("compute_sqlite_scores_database")){
					instance.computeSqliteScoresDatabase = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("feedback_url")){
					instance.feedbackUrl = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("database_link")){
					instance.databasesLinksDirectory = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("jbrowse_ressource_url")){
					instance.jbrowseRessourcesUrl = entry.getValue();
				} else {
					logger.warn("key : "+entry.getKey()+" not recognized");
				}
			}
		}
		if(null!=instance.sqliteOutputDirectory &&
				null!=instance.jbrowseOutputDirectory &&
				null!=instance.computeSqliteScoresDatabase &&
				null!=instance.feedbackUrl &&
				null!= instance.databasesLinksDirectory &&
				null!= instance.jbrowseRessourcesUrl){
			logger.info("good conf file :D");
			return true;
		}
		return false;
	}

	public static String getJobsFile(){
		return instance.workingDir+JOBS_FILE;
	}
	public static String getLogger(){
		return System.getProperty("user.dir")+"/sqlite.log";
	}
	public static String getSqliteOutput(){
		return instance.sqliteOutputDirectory;
	}
	public static String getJbrowseOutput(){
		return instance.jbrowseOutputDirectory;
	}
	public static String getComputeSqliteDatabase(){
		return instance.computeSqliteScoresDatabase;
	}
	public static String getFeedbackUrl(){
		return instance.feedbackUrl;
	}
	public static String getDatabasesLink(){
		return instance.databasesLinksDirectory;
	}
	public static String getRessourceURL(){
		return instance.jbrowseRessourcesUrl;
	}


	//LOCAL
	//	public static final String GDV_VERSION ="gdv_dev";
	//	public static final String DAEMON_DIR="/Users/jarosz/epfl/transform_to_sqlite";
	//	public static final String GDV_DIR="/Users/jarosz/epfl/transform_to_sqlite/data";
	//	
	//	public static final String FILES_DIRECTORY = GDV_DIR+"/files";
	//	public static final String TRACKS_DIRECTORY = GDV_DIR+"/tracks";
	//	public static final String TMP_DIRECTORY = GDV_DIR+"/files/tmp";
	//	
	//	
	//	public static final String COMPUTE_SQLITE_SCORES_DATABASE =DAEMON_DIR+"/jobs.db";//this is the link to another database
	//	public static final String DAEMON_FILE = DAEMON_DIR+"/ActiveDaemonPID.pid";
	//	public static final String LOG_FILE = DAEMON_DIR+"/daemon.log";
	//	public static final String SQLITE_LOG_FILE = DAEMON_DIR+"/sqlite.log";
	//	public static final String DATABASE = DAEMON_DIR+"/jobs.db"; 
	//	
	//	public static final String POST_URL = "";
	//	public static final String MD5_COMMAND = "md5";
	//	public final static String DATABASES_LINKS = "/data/"+GDV_VERSION+"/databases_link";
}

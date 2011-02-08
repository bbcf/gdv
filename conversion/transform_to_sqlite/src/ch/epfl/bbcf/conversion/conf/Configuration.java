package ch.epfl.bbcf.conversion.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import ch.epfl.bbcf.conversion.daemon.Launcher;

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

	public enum Extension {WIG,BEDGRAPH,BED,GFF,GTF,BAM};
	public static final String MD5_COMMAND = "md5sum";
	public static final String CONF_FILE = "/conf/conf.yaml";
	public static final String DAEMON_FILE = "/ActiveDaemonPID.pid";
	public static final String JOBS_FILE = "/jobs.db";

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

}

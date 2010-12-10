package ch.epfl.bbcf.gdv.config;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IResourceSettings;
import org.yaml.snakeyaml.Yaml;

import ch.epfl.bbcf.gdv.config.utility.RolesAuthorization;
import ch.epfl.bbcf.gdv.html.AddSequencePage;
import ch.epfl.bbcf.gdv.html.AdminPage;
import ch.epfl.bbcf.gdv.html.BrowserPage;
import ch.epfl.bbcf.gdv.html.HomePage;
import ch.epfl.bbcf.gdv.html.ImportFilePage;
import ch.epfl.bbcf.gdv.html.ImportUCSCPage;
import ch.epfl.bbcf.gdv.html.LoginPage;
import ch.epfl.bbcf.gdv.html.MagicPasswordPage;
import ch.epfl.bbcf.gdv.html.PostPage;
import ch.epfl.bbcf.gdv.html.ProjectPage;
import ch.epfl.bbcf.gdv.html.TrackStatus;
/**
 * Class called in the init() method of the Application class. It contains all about nice URL, 
 * ressources folders, and authorirization
 * 
 * @author yohan jarosz
 *
 */
public final class Configuration{

	public final static String LOG_DIRECTORY = "/data/gdv_dev/log/gdv.log";
	public final static String GDV_VERSION = "gdv_dev-1";
	public final static String CONF_FILE = "/data/gdv_dev/conf/gdv.yaml";
	//URL MAPPING
	//public static final String APPLI_SERV = "http://ptbbpc1.epfl.ch";
	//	public static final String APPLI_URL = APPLI_SERV+"/"+GDV_VERSION;

	//public static final String PROXY_URL = "http://svitsrv25.epfl.ch";
	//public static final String PROXY_APPLI = PROXY_URL+"/gdv";
	//public static final String FILES_ACCESS_URL = PROXY_URL+"/files";


	//DIRECTORY MAPPING

	//public static final String WORKING_DIRECTORY ="/data/gdv";
	//public static final String PUBLIC_URL = "/public";

	//	public static final String FILES_DIRECTORY = WORKING_DIRECTORY+"/files";
	//	public static final String TMP_DIRECTORY = FILES_DIRECTORY+"/tmp";
	//	//public static final String LOG_DIRECTORY = WORKING_DIRECTORY+"/log";
	//	public static final String TRACK_DIRECTORY =WORKING_DIRECTORY+"/tracks";
	//	public static final String TRACKS_DIRECTORY =WORKING_DIRECTORY+"/tracks/";
	//	//public static final String BIN_DIRECTORY =WORKING_DIRECTORY+"/bin";
	//	public static final String PUBLIC_DIRECTORY = WORKING_DIRECTORY+"/public";
	//
	//	public static final String JS_DIRECTORY =PUBLIC_URL+"/javascript";
	//	public static final String CSS_DIRECTORY =PUBLIC_URL+"/css";
	//	public static final String CSS_JBROWSE =PUBLIC_URL+"/css/jbrowsecss";
	//	public static final String DAS_DIRECTORY = FILES_DIRECTORY+"/DAS";
	//	//public final static String SRV_JBROWSE = "/srv/jbrowsor/current/jbrowse/";
	//	//public final static String DATA_JBROWSE = "/data/jbrowse/";
	//	public final static String DATABASES_LINKS =  WORKING_DIRECTORY+"/databases_link";
	//	public static final String WICKET_MAIN_FOLDER ="/local_home/java/apache-tomcat-6.0.26" +
	//	"/webapps/"+GDV_VERSION+"/WEB-INF/classes/";
	//
	//	public static final String CONF_DIR = WORKING_DIRECTORY+"/conf";
	//	//DAEMON MAPPING
	//	public static final String COMPUTE_SCORES_DIR = WORKING_DIRECTORY+"/compute_sqlite_scores";
	//	public static final String CALCULATE_SCORES_DAEMON = COMPUTE_SCORES_DIR+"/jobs.db";

	//	public static final String TRANSFORM_TO_SQLITE_DIR = WORKING_DIRECTORY+"/transform_to_sqlite";
	//	public static final String TRANSFORM_TO_SQLITE_DAEMON = TRANSFORM_TO_SQLITE_DIR+"/jobs.db";
	//
	//	public static final String PROJECTS_URL = PROXY_APPLI+"/projects";













	//	public static final String[] BROWSER_JS = {
	//		"/javascript/js_compressed/Browser.js_min.js" ,
	//		"/javascript/js_compressed/Util.js_min.js" ,
	//		"/javascript/js_compressed/NCList.js_min.js" ,
	//		"/javascript/js_compressed/LazyPatricia.js_min.js" ,
	//		"/javascript/js_compressed/Track.js_min.js" ,
	//		"/javascript/js_compressed/SequenceTrack.js_min.js" ,
	//		"/javascript/js_compressed/FeatureTrack.js_min.js" ,
	//		"/javascript/js_compressed/StaticTrack.js_min.js" ,
	//		"/javascript/js_compressed/ImageTrack.js_min.js" ,
	//		"/javascript/js_compressed/prototype_reduced.js_min.js" ,
	//		"/javascript/js_compressed/rectmarquee.js_min.js" ,
	//		"/javascript/js_compressed/GenomeView.js_min.js" ,
	//		"/javascript/jslib/dojo/dojo.js",
	//		"/javascript/jslib/dojo/jbrowse_dojo.js"	,	
	//		"/javascript/js_compressed/gdv_link.js_min.js" ,
	//		"/javascript/js_compressed/gdv_canvas.js_min.js" ,
	//		"/javascript/js_compressed/gdv_selection.js_min.js" ,
	//		"/javascript/js_compressed/SelectionHandler.js_min.js" ,
	//		"/javascript/js_compressed/NodeList-traverse.js_min.js" 
	//
	//	};
	private static String[] buildJSFile() {
		String[] js = new String[18];
		js[0]=instance.javascript_dir+"/gdv_js/gdv_link.js";
		js[1]=instance.javascript_dir+"/gdv_js/gdv_canvas.js";
		js[2]=instance.javascript_dir+"/jb_js/Browser.js";
		js[3]=instance.javascript_dir+"/jb_js/Track.js";
		js[4]=instance.javascript_dir+"/jb_js/FeatureTrack.js";
		js[5]=instance.javascript_dir+"/jb_js/SequenceTrack.js";
		js[6]=instance.javascript_dir+"/jb_js/touchjBrowse.js";
		js[7]=instance.javascript_dir+"/jb_js/ImageTrack.js";
		js[8]=instance.javascript_dir+"/jb_js/Layout.js";
		js[9]=instance.javascript_dir+"/jb_js/LazyArray.js";
		js[10]=instance.javascript_dir+"/jb_js/LazyPatricia.js";
		js[11]=instance.javascript_dir+"/jb_js/NCList.js";
		js[12]=instance.javascript_dir+"/jb_js/UITracks.js";
		js[13]=instance.javascript_dir+"/jb_js/Util.js";
		js[14]=instance.javascript_dir+"/jb_js/GenomeView.js";
		js[15]=instance.javascript_dir+"/jslib/dojo/dojo.js";
		js[16]=instance.javascript_dir+"/jslib/dojo/jbrowse_dojo.js";
		js[17]=instance.javascript_dir+"/jb_js/Selection.js";
		return js;
	}
	private static String[] buildJbrowseCSSFiles() {
		String[] css = new String[3];
		css[0]=instance.css_jbrowse_dir+"/jslib/dijit/themes/tundra/tundra.css";
		css[1]=instance.css_jbrowse_dir+"/jslib/dojo/resources/dojo.css";
		css[2]=instance.css_jbrowse_dir+"/genome.css";
		return css;
	}
	private static String[] buidGDVCSSFiles(){
		String[] css = new String[1];
		css[0]=instance.css_dir+"/gdv_style.css";
		return css;
	}
	public static final String URL_LINK_TEMPLATE = "\" onClick=\"javascript:showLinkPanel('{name}'); return false\";";

	public final static Class[] admin_pages = {AddSequencePage.class,AdminPage.class,ImportUCSCPage.class};
	public final static Class[] user_pages = {HomePage.class,ImportFilePage.class,ProjectPage.class,
		BrowserPage.class};







	public static void addRessourcesLocations(IResourceSettings resourceSettings){
		resourceSettings.addResourceFolder("conf/");
		//resourceSettings.addResourceFolder(SRV_JBROWSE);
		//resourceSettings.addResourceFolder(DATA_JBROWSE);
		//		resourceSettings.addResourceFolder(TRACKS_DIRECTORY);
		//		//resourceSettings.addResourceFolder("/data/jbrowse/data/");
		//		resourceSettings.addResourceFolder(Configuration.WICKET_MAIN_FOLDER+"/html/");
		//		resourceSettings.addResourceFolder(Configuration.WICKET_MAIN_FOLDER+"/js/");
		//		resourceSettings.addResourceFolder(Configuration.WICKET_MAIN_FOLDER+"/css/");
		//		resourceSettings.addResourceFolder(Configuration.WICKET_MAIN_FOLDER+"/img/");
		//		//		resourceSettings.addResourceFolder(Configuration.WICKET_MAIN_FOLDER+"/../../jb/");
		//		//		resourceSettings.addResourceFolder(Configuration.WICKET_MAIN_FOLDER+"/../../views/");
		resourceSettings.setResourceStreamLocator(new ch.epfl.bbcf.gdv.config.utility.RessourceLocator());
	}


	public static void addURLMounting(Application application) {
		//SIMPLE MOUNT
		//application.
		application.mount(new IndexedParamUrlCodingStrategy("/home", HomePage.class));
		application.mount(new IndexedParamUrlCodingStrategy("/projects", ProjectPage.class));
		application.mount(new IndexedParamUrlCodingStrategy("/login", LoginPage.class));
		application.mount(new IndexedParamUrlCodingStrategy("/import_UCSC", ImportUCSCPage.class));
		application.mount(new IndexedParamUrlCodingStrategy("/admin", AdminPage.class));
		application.mount(new IndexedParamUrlCodingStrategy("/post", PostPage.class));
		application.mount(new IndexedParamUrlCodingStrategy("/log", MagicPasswordPage.class));
		HybridUrlCodingStrategy addSequence = new HybridUrlCodingStrategy(
				"/add_sequence",AddSequencePage.class);
		application.mount(addSequence);
		application.mount(new IndexedParamUrlCodingStrategy("/import_file", ImportFilePage.class));
		//application.mount(new IndexedParamUrlCodingStrategy("/import_admin", ImportFileAdminPage.class));
		application.mount(new IndexedParamUrlCodingStrategy("/tracks_status", TrackStatus.class));
		//application.mount(new IndexedParamUrlCodingStrategy("/views_status", ViewStatus.class));
		application.mount(new MixedParamUrlCodingStrategy("/browser", BrowserPage.class,new String[] {"id"}));
		//MOUNT WITH PARAMS
		//		MixedParamUrlCodingStrategy confirmation = new MixedParamUrlCodingStrategy(
		//				"confirm",ComfirmPage.class, new String[]{"confirm-user"});
		//		application.mount(confirmation);


	}

	public static void authorirization(Application application){
		application.getSecuritySettings().setAuthorizationStrategy(
				new RoleAuthorizationStrategy(new RolesAuthorization()));
		for(Class clazz : admin_pages){
			MetaDataRoleAuthorizationStrategy.authorize(clazz, Roles.ADMIN);
		}
		for(Class clazz : user_pages){
			MetaDataRoleAuthorizationStrategy.authorize(clazz, Roles.USER);
		}


	}
	public static void setErrorPages(Application application){
		//		application.getApplicationSettings().setPageExpiredErrorPage(ExpiredPage.class);
		//		application.getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);
		//		application.getApplicationSettings().setInternalErrorPage(ErrorPage.class);
		application.getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_EXCEPTION_PAGE);

	}

	private Configuration(){
	}
	private static Configuration instance;


	//GDV
	private static String gdv_tomcat_server;
	private static String gdv_proxy_url;
	private static String gdv_working_directory;
	private static String gdv_public_url;
	private static String wicket_main_folder;


	private static String gdv_appli_proxy,files_dir,tmp_dir,log_dir,
	tracks_dir,public_dir,javascript_dir,css_dir,css_jbrowse_dir,
	das_dir,databases_link_dir,compute_scores_daemon,
	transform_to_sqlite_daemon,project_url;

	private static String[] javascript_files;
	private static String[] jbrowse_css_files;
	private static String[] gdv_css_files;
	//MAIL
	private static String mail_adress;
	private static String mail_transport;
	private static String mail_host;
	private static String mail_user;
	private static String mail_passwd;


	public static boolean init() {
		Application.info("THIS IS GREAAT !!!");
		if(instance==null){
			synchronized(Configuration.class){
				instance = new Configuration();
			}
		}
		if(instance!=null){
			return readConfigurationFile();
		} 
		return false;
	}

	private static boolean readConfigurationFile() {
		Application.info("reading conf file");
		InputStream input = null;
		try {
			input = new FileInputStream(new File(Configuration.CONF_FILE));
		} catch (FileNotFoundException e) {
			Application.error(e);
			return false;
		}
		if(null!=input){
			Yaml yaml = new Yaml();
			Map<String, String> data = (Map<String, String>)yaml.load(input);
			for(Map.Entry<String, String> entry : data.entrySet()){
				if(entry.getKey().equalsIgnoreCase("gdv_tomcat_server")){
					instance.gdv_tomcat_server= entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("gdv_proxy_url")){
					instance.gdv_proxy_url = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("gdv_working_directory")){
					instance.gdv_working_directory = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("gdv_public_url")){
					instance.gdv_public_url = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("wicket_main_folder")){
					instance.wicket_main_folder = entry.getValue();
				} else {
					Application.warn("key : "+entry.getKey()+" not recognized");
				}
			}
			if(null!= instance.gdv_tomcat_server &&
					null!=instance.gdv_proxy_url &&
					null!=instance.gdv_working_directory &&
					null!=instance.gdv_public_url &&
					null!=instance.wicket_main_folder){
				Application.info("init parameters");
				instance.gdv_appli_proxy=instance.gdv_proxy_url+"/gdv";
				instance.files_dir=instance.gdv_working_directory+"/files";
				instance.tmp_dir=instance.gdv_working_directory+"/files/tmp";
				instance.log_dir=instance.gdv_working_directory+"/log";
				instance.tracks_dir=instance.gdv_working_directory+"/tracks";
				instance.public_dir=instance.gdv_working_directory+"/public";
				instance.javascript_dir=instance.public_dir+"/javascript";
				instance.css_dir=instance.public_dir+"/css";
				instance.css_jbrowse_dir=instance.public_dir+"/css/jbrowsecss";
				instance.das_dir=instance.files_dir+"/DAS";
				instance.databases_link_dir=instance.gdv_working_directory+"/databases_link";
				instance.compute_scores_daemon=instance.gdv_working_directory+"/compute_sqlite_scores/jobs.db";
				instance.transform_to_sqlite_daemon=instance.gdv_working_directory+"/transform_to_sqlite/jobs.db";
				instance.project_url = instance.gdv_appli_proxy+"/projects";
				instance.javascript_files = buildJSFile();
				instance.jbrowse_css_files = buildJbrowseCSSFiles();
				instance.gdv_css_files = buidGDVCSSFiles();
				return true;
			}
			Application.error("parameters not corrects");
		}
		return false;
	}







	public static Configuration getConf(){
		if(null==instance){
			init();
		}
		return instance;
	}






	public static boolean initMail() {
		Application.info("reading mail conf file");
		InputStream input = null;
		try {
			input = new FileInputStream(new File("conf/mail.yaml"));
		} catch (FileNotFoundException e) {
			Application.error(e);
			return false;
		}
		if(null!=input){
			Yaml yaml = new Yaml();
			Map<String, String> data = (Map<String, String>)yaml.load(input);
			for(Map.Entry<String, String> entry : data.entrySet()){
				if(entry.getKey().equalsIgnoreCase("adress")){
					instance.mail_adress = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("transport")){
					instance.mail_transport = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("host")){
					instance.mail_host = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("user")){
					instance.mail_user = entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("passwd")){
					instance.mail_passwd = entry.getValue();
				} else {
					Application.warn("key : "+entry.getKey()+" not recognized");
				}
			}
			return true;
		}
		return false;
	}





	//MAIL CONF
	public String getMailAdress(){
		return instance.mail_adress;
	}
	public String getMailTransport(){
		return instance.mail_transport;
	}
	public String getMailHost(){
		return instance.mail_host;
	}
	public String getMailUser(){
		return instance.mail_user;
	}
	public String getMailPasswd(){
		return instance.mail_passwd;
	}


	/**
	 * @return the gdv_appli_proxy
	 */
	public static String getGdv_appli_proxy() {
		return instance.gdv_appli_proxy;
	}

	/**
	 * @return the log_dir
	 */
	public static String getLog_dir() {
		return instance.log_dir;
	}

	/**
	 * @return the tmp_dir
	 */
	public static String getTmp_dir() {
		return instance.tmp_dir;
	}

	/**
	 * @return the css_dir
	 */
	public static String getCss_dir() {
		return instance.css_dir;
	}


	/**
	 * @return the css_jbrowse
	 */
	public static String getCssJbrowse_dir() {
		return instance.css_jbrowse_dir;
	}


	/**
	 * @return the javascript_dir
	 */
	public static String getJavascript_dir() {
		return instance.javascript_dir;
	}


	/**
	 * @return the tracks_dir
	 */
	public static String getTracks_dir() {
		return instance.tracks_dir;
	}



	/**
	 * @return the das_dir
	 */
	public static String getDas_dir() {
		return instance.das_dir;
	}


	/**
	 * @return the compute_scores_daemon
	 */
	public static String getCompute_scores_daemon() {
		return instance.compute_scores_daemon;
	}



	/**
	 * @return the databases_link_dir
	 */
	public static String getDatabases_link_dir() {
		return instance.databases_link_dir;
	}



	/**
	 * @return the transform_to_sqlite_daemon
	 */
	public static String getTransform_to_sqlite_daemon() {
		return instance.transform_to_sqlite_daemon;
	}



	public static String getGdvTomcatServ(){
		return instance.gdv_tomcat_server;
	}
	public static String getGdvProxyUrl(){
		return instance.gdv_proxy_url;
	}
	public static String getGdvWorkingDir(){
		return instance.gdv_working_directory;
	}
	public static String getGdvPublicUrl(){
		return instance.gdv_public_url;
	}
	public static String getWicketMainFolder(){
		return instance.wicket_main_folder;
	}
	public static String getFilesDir(){
		return instance.files_dir;
	}
	public static String getProjectUrl(){
		return instance.project_url;
	}
	public static String[] getJavascriptFiles(){
		return instance.javascript_files;
	}
	public static String[] getJbrowseCSSFiles(){
		return instance.jbrowse_css_files;
	}
	public static String[] getGDVCSSFiles(){
		return instance.gdv_css_files;
	}
}

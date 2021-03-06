package ch.epfl.bbcf.gdv.config;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IResourceSettings;
import org.yaml.snakeyaml.Yaml;

import ch.epfl.bbcf.gdv.config.utility.RolesAuthorization;
import ch.epfl.bbcf.gdv.html.AddSequencePage;
import ch.epfl.bbcf.gdv.html.AdminPage;
import ch.epfl.bbcf.gdv.html.AlternativeLoginPage;
import ch.epfl.bbcf.gdv.html.AlternativeProjectPage;
import ch.epfl.bbcf.gdv.html.BrowserPage;
import ch.epfl.bbcf.gdv.html.ConfigureTrackPage;
import ch.epfl.bbcf.gdv.html.ErrorPage;
import ch.epfl.bbcf.gdv.html.ImageDisplayer;
import ch.epfl.bbcf.gdv.html.PreferencesPage;
import ch.epfl.bbcf.gdv.html.HomePage;
import ch.epfl.bbcf.gdv.html.ImportFilePage;
import ch.epfl.bbcf.gdv.html.LoginPage;
import ch.epfl.bbcf.gdv.html.ProjectPage;
import ch.epfl.bbcf.gdv.html.TrackStatus;
import ch.epfl.bbcf.gdv.html.utility.MenuElement;
/**
 * Class called in the init() method of the Application class. It contains all about nice URL, 
 * resources folders, and authorizations
 * 
 * @author yohan jarosz
 *
 */
public final class Configuration{


	private static String[] buildJSFile() {
		String[] js = new String[25];
		js[0]=instance.jbrowse_javascript_url+"/js/gdv.js";
		js[1]=instance.jbrowse_javascript_url+"/js/Jobs.js";
		js[2]=instance.jbrowse_javascript_url+"/js/TabContainer.js";
		js[3]=instance.jbrowse_javascript_url+"/js/MenuBar.js";
		js[4]=instance.jbrowse_javascript_url+"/js/Canvas.js";
		js[5]=instance.jbrowse_javascript_url+"/js/Browser.js";
		js[6]=instance.jbrowse_javascript_url+"/js/Track.js";
		js[7]=instance.jbrowse_javascript_url+"/js/LinkPanel.js";
		js[8]=instance.jbrowse_javascript_url+"/js/GeneNames.js";
		js[9]=instance.jbrowse_javascript_url+"/js/FeatureTrack.js";
		js[10]=instance.jbrowse_javascript_url+"/js/SequenceTrack.js";
		js[11]=instance.jbrowse_javascript_url+"/js/touchjBrowse.js";
		js[12]=instance.jbrowse_javascript_url+"/js/ImageTrack.js";
		js[13]=instance.jbrowse_javascript_url+"/js/Layout.js";
		js[14]=instance.jbrowse_javascript_url+"/js/LazyArray.js";
		js[15]=instance.jbrowse_javascript_url+"/js/LazyPatricia.js";
		js[16]=instance.jbrowse_javascript_url+"/js/NCList.js";
		js[17]=instance.jbrowse_javascript_url+"/js/UITracks.js";
		js[18]=instance.jbrowse_javascript_url+"/js/Util.js";
		js[19]=instance.jbrowse_javascript_url+"/js/GenomeView.js";
		js[20]=instance.jbrowse_javascript_url+"/js/TrackSelection.js";
		js[21]=instance.jbrowse_javascript_url+"/js/ZoneSelection.js";
		js[22]=instance.jbrowse_javascript_url+"/js/GFeatMiner.js";
		//js[20]="http://ajax.googleapis.com/ajax/libs/dojo/1.5/dojo/dojo.xd.js";
		js[23]=instance.jbrowse_javascript_url+"/jslib/dojo/dojo.js";
		js[24]=instance.jbrowse_javascript_url+"/jslib/dojo/jbrowse_dojo.js";
		return js;
	}
	private static String[] buildJbrowseCSSFiles() {
		String[] css = new String[3];
		css[0]=instance.jbrowse_javascript_url+"/jslib/dijit/themes/tundra/tundra.css";
		css[1]=instance.jbrowse_javascript_url+"/jslib/dojo/resources/dojo.css";
		css[2]=instance.jbrowse_css_url+"/genome.css";
		return css;
	}
	private static String[] buidGDVCSSFiles(){
		String[] css = new String[1];
		css[0]=instance.gdv_public_url+"/css/gdv_style.css";
		return css;
	}


	public static final String URL_LINK_TEMPLATE = "\" onClick=\"javascript:showLinkPanel('{name}'); return false\";";

	public final static Class[] admin_pages = {AddSequencePage.class,AdminPage.class};
	public final static Class[] user_pages = {ImportFilePage.class,ProjectPage.class,PreferencesPage.class};

	public final static MenuElement[] navigation_links = {
		new MenuElement(HomePage.class,"Home"),
		new MenuElement(AdminPage.class,"Admin"),
		new MenuElement(ProjectPage.class,"Projects"),
		new MenuElement(PreferencesPage.class,"Preferences")};

	public static List<MenuElement> getNavigationLinks() {
		return Arrays.asList(navigation_links);
	}

	public static void addRessourcesLocations(IResourceSettings resourceSettings){
		resourceSettings.addResourceFolder("conf/");
		resourceSettings.addResourceFolder("/html/");
		resourceSettings.addResourceFolder(Configuration.getWicketMainFolder()+"/html/");
		resourceSettings.setResourceStreamLocator(new ch.epfl.bbcf.gdv.config.utility.RessourceLocator());
		System.out.println("FOUND WICKET :"+Configuration.getWicketMainFolder()+"/html/");
	}


	public static void addURLMounting(Application application) {
		//SIMPLE MOUNT
		application.mount(new HybridUrlCodingStrategy("/home", HomePage.class));
		application.mount(new HybridUrlCodingStrategy("/error", ErrorPage.class));
		application.mount(new HybridUrlCodingStrategy("/projects", ProjectPage.class));
		application.mount(new HybridUrlCodingStrategy("/public_project", AlternativeProjectPage.class));
		application.mount(new HybridUrlCodingStrategy("/login", LoginPage.class));
		application.mount(new HybridUrlCodingStrategy("/login2", AlternativeLoginPage.class));
		application.mount(new HybridUrlCodingStrategy("/admin", AdminPage.class));
		//application.mount(new IndexedParamUrlCodingStrategy("/log", MagicPasswordPage.class));
		HybridUrlCodingStrategy addSequence = new HybridUrlCodingStrategy(
				"/add_sequence",AddSequencePage.class);
		application.mount(addSequence);
		application.mount(new HybridUrlCodingStrategy("/import_file", ImportFilePage.class));
		application.mount(new MixedParamUrlCodingStrategy("/preferences", PreferencesPage.class,new String[]{"project_id"}));
		application.mount(new MixedParamUrlCodingStrategy("/image", ImageDisplayer.class,new String[]{"id"}));
		application.mount(new QueryStringUrlCodingStrategy("/configure_track", ConfigureTrackPage.class));
		application.mount(new HybridUrlCodingStrategy("/tracks_status", TrackStatus.class));
		application.mount(new QueryStringUrlCodingStrategy("/browser", BrowserPage.class));
		//application.mount(new QueryStringUrlCodingStrategy("/browser2", BrowserPage2.class));
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
	private static String login_type;

	private static String gdv_appli_proxy,files_dir,tmp_dir,log_dir,
	tracks_dir,public_dir,jbrowse_static_files_url,jbrowse_css_url,
	das_dir,databases_link_dir,compute_scores_daemon,jbrowse_javascript_url,
	transform_to_sqlite_daemon,project_url,gdv_version,images_url,
	jbrowse_images_url,log_directory,jb_browser_root,jb_data_root,
	psql_db,psql_user,psql_pwd,gdv_browser_page_url,gFeatMinerDirectory,fastaDirectory;

	private static String gdv_post_access;
	//private static List<String> gdv_types_access;

	private static String[] javascript_files;
	private static String[] jbrowse_css_files;
	private static String[] gdv_css_files;
	//MAIL
	private static String mail_adress;
	private static String mail_transport;
	private static String mail_host;
	private static String mail_user;
	private static String mail_passwd;


	public static boolean init(String catalina_home,String metaInf, File confFile) {
		if(instance==null){
			synchronized(Configuration.class){
				instance = new Configuration();
			}
		}
		
		
		if(instance!=null){
			return readConfigurationFile(catalina_home,metaInf,confFile);
		} 
		return false;
	}

	private static boolean readConfigurationFile(String catalina_home,String metaInfPath, File confFile) {
		Application.info("reading conf file");
		InputStream input = null;
		try {
			input = new FileInputStream(confFile);
		} catch (FileNotFoundException e) {
			Application.error(e);
			return false;
		}
		if(null!=input){
			Yaml yaml = new Yaml();
			Map<String, Object> data = (Map<String, Object>)yaml.load(input);
			for(Map.Entry<String, Object> entry : data.entrySet()){
				if(entry.getKey().equalsIgnoreCase("gdv_tomcat_server")){
					instance.gdv_tomcat_server= (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("gdv_proxy_url")){
					instance.gdv_proxy_url = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("gdv_working_directory")){
					instance.gdv_working_directory = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("gdv_public_url")){
					instance.gdv_public_url = (String)entry.getValue();
					//				} else if(entry.getKey().equalsIgnoreCase("wicket_main_folder")){
					//					instance.wicket_main_folder = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("gdv_version")){
					instance.gdv_version = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("gdv_post_access")){
					instance.gdv_post_access = (String)entry.getValue();
					//				} else if(entry.getKey().equalsIgnoreCase("gdv_types_access")){
					//					instance.gdv_types_access = (List<String>)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("jb_browser_root")){
					instance.jb_browser_root = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("jb_data_root")){
					instance.jb_data_root = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("psql_db")){
					instance.psql_db = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("psql_user")){
					instance.psql_user = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("psql_pwd")){
					instance.psql_pwd = (String)entry.getValue();
				} else if(entry.getKey().equalsIgnoreCase("login_type")){
					instance.login_type = (String)entry.getValue();
				} else {
					Application.warn("key : "+entry.getKey()+" not recognized");
				}
			}
			if(null!= instance.gdv_tomcat_server &&
					null!=instance.gdv_proxy_url &&
					null!=instance.gdv_working_directory &&
					null!=instance.gdv_public_url &&
					//					null!=instance.wicket_main_folder && 
					null!=instance.gdv_version){
				Application.info("init parameters");
				instance.wicket_main_folder=catalina_home+"/webapps/"+instance.gdv_version+"/WEB-INF/classes";
				instance.gdv_appli_proxy=instance.gdv_proxy_url+"/"+instance.gdv_version;
				instance.files_dir=instance.gdv_working_directory+"/files";
				instance.tmp_dir=instance.gdv_working_directory+"/files/tmp";
				instance.log_dir=instance.gdv_working_directory+"/log";
				instance.tracks_dir=instance.gdv_working_directory+"/tracks";
				instance.public_dir=instance.gdv_working_directory+"/public";

				instance.jbrowse_static_files_url=instance.gdv_public_url+"/jbrowse";
				instance.jbrowse_css_url=instance.jbrowse_static_files_url+"/css";
				instance.jbrowse_javascript_url=instance.jbrowse_static_files_url+"/javascript";
				instance.jbrowse_images_url=instance.jbrowse_static_files_url+"/img";
				instance.images_url=instance.gdv_public_url+"/img";
				instance.das_dir=instance.files_dir+"/DAS";

				instance.databases_link_dir=instance.gdv_working_directory+"/databases_link";
				instance.compute_scores_daemon=instance.gdv_working_directory+"/compute_sqlite_scores/jobs.db";
				instance.transform_to_sqlite_daemon=instance.gdv_working_directory+"/transform_to_sqlite/jobs.db";
				instance.project_url = instance.gdv_appli_proxy+"/projects";
				instance.javascript_files = buildJSFile();
				instance.jbrowse_css_files = buildJbrowseCSSFiles();
				instance.gdv_css_files = buidGDVCSSFiles();
				instance.log_directory = metaInfPath+"/logs/";
				instance.gdv_browser_page_url=instance.gdv_appli_proxy+"/browser";
				instance.gFeatMinerDirectory=instance.gdv_working_directory+"/gFeatMiner";
				instance.fastaDirectory=instance.gdv_working_directory+"/fasta";
				return true;
			}
			Application.error("parameters not corrects");
		}
		return false;
	}







	//	public static Configuration getConf(){
	//		if(null==instance){
	//			init();
	//		}
	//		return instance;
	//	}






	@SuppressWarnings("unchecked")
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
	public static String getMailAdress(){
		return instance.mail_adress;
	}
	public static String getMailTransport(){
		return instance.mail_transport;
	}
	public static String getMailHost(){
		return instance.mail_host;
	}
	public static String getMailUser(){
		return instance.mail_user;
	}
	public static String getMailPasswd(){
		return instance.mail_passwd;
	}

	/**
	 * Return the login type
	 * either : "tequila" or "alternative"
	 * @return
	 */
	public static String getLoginType() {
		return instance.login_type;
	}
	
	/**
	 * Context that will match gdv
	 *(gdv)
	 * @return
	 */
	public static String getVersion() {
		return instance.gdv_version;
	}

	/**
	 * Get the URL that match the application URL
	 * (http://my_proxy.com/gdv)
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
		return instance.jbrowse_css_url;
	}




	/**
	 * @return the javascript_dir
	 */
	public static String getJavascript_dir() {
		return instance.jbrowse_static_files_url;
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


	/**
	 * Get the URL where tomcat is located
	 * (http://localhost)
	 * @return
	 */
	public static String getGdvTomcatServ(){
		return instance.gdv_tomcat_server;
	}

	/**
	 * Get the URL where the application
	 * should be accessed
	 * (http://my_proxy.com)
	 * @return
	 */
	public static String getGdvProxyUrl(){
		return instance.gdv_proxy_url;
	}
	/**
	 * Where the project is located on the 
	 * filesystem ($GDV_HOME)
	 * @return
	 */
	public static String getGdvWorkingDir(){
		return instance.gdv_working_directory;
	}
	/**
	 * the context that will match public directory
	 * (/public)
	 * @return
	 */
	public static String getGdvPublicUrl(){
		return instance.gdv_public_url;
	}
	//	public static String getWicketMainFolder(){
	//		return instance.wicket_main_folder;
	//	}
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
	public static void setGdv_post_access(String gdv_post_access) {
		Configuration.gdv_post_access = gdv_post_access;
	}
	public static String getGdv_post_access() {
		return "gdv_post";
	}
	public static String getJavascriptUrl(){
		return instance.jbrowse_javascript_url;
	}
	//	public static List<String> getGdv_types_access() {
	//		return instance.gdv_types_access;
	//	}
	public static String getGdv_Images_url(){
		return instance.images_url;
	}
	public static String getJbrowse_Images_url(){
		return instance.jbrowse_images_url;
	}
	public static String getJbrowse_static_files_url(){
		return instance.jbrowse_static_files_url;
	}
	public static String getLog_directory() {
		return instance.log_directory;
	}
	public static String getJb_browser_root() {
		return instance.jb_browser_root;
	}
	public static String getJb_data_root() {
		return instance.jb_data_root;
	}
	public static String getPsql_db() {
		return instance.psql_db;
	}
	public static String getPsql_user() {
		return instance.psql_user;
	}
	public static String getPsql_pwd() {
		return instance.psql_pwd;
	}
	public static String getBrowserUrl(){
		return instance.gdv_browser_page_url;
	}
	public static void setgFeatMinerDirectory(String gFeatMinerDirectory) {
		instance.gFeatMinerDirectory = gFeatMinerDirectory;
	}
	public static String getgFeatMinerDirectory() {
		return instance.gFeatMinerDirectory;
	}
	public static void setFatstaDirectory(String fastaDirectory) {
		instance.fastaDirectory = fastaDirectory;
	}
	public static String getFastaDirectory() {
		return instance.fastaDirectory;
	}

	public static String getWicketMainFolder() {
		return instance.wicket_main_folder;
	}
}

package ch.epfl.bbcf.conversion.daemon;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import ch.epfl.bbcf.bbcfutils.Utility;
import ch.epfl.bbcf.bbcfutils.access.InternetConnection;
import ch.epfl.bbcf.bbcfutils.access.gdv.RemoteAccess;
import ch.epfl.bbcf.bbcfutils.conversion.json.ConvertToJSON;
import ch.epfl.bbcf.bbcfutils.conversion.sqlite.ConvertToSQLite;
import ch.epfl.bbcf.bbcfutils.exception.ConvertToJSONException;
import ch.epfl.bbcf.bbcfutils.exception.ExtensionNotRecognisedException;
import ch.epfl.bbcf.bbcfutils.exception.ParsingException;
import ch.epfl.bbcf.bbcfutils.parsing.Extension;
import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
import ch.epfl.bbcf.conversion.conf.Configuration;
import ch.epfl.bbcf.utility.file.FileManagement;




/**
 * Class which choose what parsing to launch in function
 * of the parameters given
 * @author Yohan Jarosz
 *
 */
public class Launcher extends Thread{

	private Job job;


	public void process(Job job) {
		this.job = job;
		ManagerService.submitSQLiteProcess(this);
	}

	public void run(){
		long start = System.currentTimeMillis();

		Configuration.getLoggerInstance().debug("getting sha1sum");

		//////////   GETTING SHA1   //////////
		Configuration.getLoggerInstance().debug(this.getId()+" run : "+job.toString());
		File file = new File(job.getFile());
		String sha1;
		try {
			sha1 = Utility.getFileDigest(file.getAbsolutePath(),"SHA1");
		} catch (IOException e2) {
			Configuration.getLoggerInstance().error(e2);
			return;
		} catch (NoSuchAlgorithmException e) {
			Configuration.getLoggerInstance().error(e);
			return;
		}
		if(null==sha1){
			try {
				RemoteAccess.sendTrackErrorMessage(job.getFeedbackUrl(),
						"cannot get sha1","md5",Integer.toString(job.getTrackId()), job.getFile());
			} catch (IOException e) {
				Configuration.getLoggerInstance().error(e);
			}
			return;
		}


		boolean doJSON = false;
		boolean doSQLite = false;

		Configuration.getLoggerInstance().debug("getting extension & connection to db");

		//////////   EXTENSION   //////////
		Extension ext = null;
		String type = "";
		if(job.getExtension().equalsIgnoreCase("wig")){
			doSQLite = true;
			doJSON = true;
			ext = Extension.WIG;
			type="quantitative";
		} else if(job.getExtension().equalsIgnoreCase("bedgraph")){
			ext = Extension.BEDGRAPH;
			doJSON = true;
			doSQLite = true;
			type="quantitative";
		}else if(job.getExtension().equalsIgnoreCase("bed")){
			ext = Extension.BED;
			doSQLite = true;
			doJSON = true;
			type="qualitative";
			doJSON=true;
		}else if(job.getExtension().equalsIgnoreCase("gff")){
			ext = Extension.GFF;
			doSQLite = true;
			doJSON = true;
			type="qualitative";
			doJSON=true;
		}else if(job.getExtension().equalsIgnoreCase("gtf")){
			ext = Extension.GFF;
			doSQLite = true;
			doJSON=true;
			type="qualitative";
		}else if(job.getExtension().equalsIgnoreCase("db")||job.getExtension().equalsIgnoreCase("sql")){
			doJSON=true;
			try {
				SQLiteAccess access = SQLiteAccess.getConnectionWithDatabase(job.getFile());
				type = access.testIfQualitative();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}


		}else if(job.getExtension().equalsIgnoreCase("bam")||
				job.getExtension().equalsIgnoreCase("sam")){
			ext = Extension.BAM;
			type="qualitative";
			try {
				String body ="id=track_error&job_id="+job.getTrackId()+"&data=extension "+ext+"not supported";
				InternetConnection.sendPOSTConnection(job.getFeedbackUrl(), body,InternetConnection.MIME_TYPE_FORM_APPLICATION);
			} catch (IOException e) {
				Configuration.getLoggerInstance().error(e);
			}
		}else {
			try {
				String body ="id=track_error&job_id="+job.getTrackId()+"&data=extension not recognized";
				InternetConnection.sendPOSTConnection(job.getFeedbackUrl(), body,InternetConnection.MIME_TYPE_FORM_APPLICATION);
			} catch (IOException e) {
				Configuration.getLoggerInstance().error(e);
			}
			return;
		}


		String database = sha1+".db";
		Configuration.getLoggerInstance().info(this.getId()+" start of convertion to SQLite :  "+job.getFile()+" to "
				+job.getOutputDirectory()+"/"+database);

		boolean wellParsed = false;
		String error ="";
		//SQLite
		if(doSQLite){
			Exception ex = null;
			try {
				ConvertToSQLite convertor = new ConvertToSQLite(job.getFile(), ext,job.getNrAssemblyId());
				wellParsed = convertor.convert(job.getOutputDirectory()+"/"+database,type);
			} catch (IOException e1) {
				Configuration.getLoggerInstance().error(e1);
				ex=e1;
			} catch (ParsingException e1) {
				Configuration.getLoggerInstance().error(e1);
				ex=e1;
			} catch (ExtensionNotRecognisedException e) {
				Configuration.getLoggerInstance().error(e);
				ex=e;
			} finally {
				if(null!=ex){
					for(StackTraceElement el : ex.getStackTrace()){
						Configuration.getLoggerInstance().error(el.getClassName()+"."+el.getClassName()+"."+el.getMethodName()+" at line "+el.getLineNumber());
					}
					String body ="id=track_error&job_id="+job.getTrackId()+"&data=parsing error  ("+this.getId()+")  :  "+ex.getMessage();
					try {
						InternetConnection.sendPOSTConnection(job.getFeedbackUrl(), body,InternetConnection.MIME_TYPE_FORM_APPLICATION);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
			}
		}
		Configuration.getLoggerInstance().info("end sqlite conversion for "+this.getId());







		//JSON 
		if(doJSON){
			Configuration.getLoggerInstance().debug(this.getId()+" start of convertion to JSON :  " +
					""+database+" to "+job.getJbrowseOutputDirectory()+"/"+database +"\nand file : "+file.getName());
			ConvertToJSON convertor = new ConvertToJSON(job.getOutputDirectory()+"/"+database, type);
			/**
			 * String outputPath,String dbName,String ressourceUrl,String trackName
			 */
			try {
				wellParsed = convertor.convert(job.getJbrowseOutputDirectory(),
						database,job.getJbrowseRessourcesUrl(),file.getName());
			} catch (ConvertToJSONException e) {
				Configuration.getLoggerInstance().error(e);
				wellParsed=false;
				error+=e.getMessage();
			}
		}
		Configuration.getLoggerInstance().info("end JSON conversion for "+this.getId());








		//////////   FEEDBACK   //////////
		if(wellParsed){
			Configuration.getLoggerInstance().debug(this.getId()+" well parsed");
			try {
				String body ="id=track_success&job_id="+job.getTrackId()+"&db_type="+type;
				InternetConnection.sendPOSTConnection(job.getFeedbackUrl(), body,InternetConnection.MIME_TYPE_FORM_APPLICATION);
			} catch (IOException e) {
				Configuration.getLoggerInstance().error(e);
			}

			//DELETING TMPDIR
			Configuration.getLoggerInstance().debug("delete tmpdir : "+job.getTmpdir());
			if(job.getTmpdir()!=null && !job.getTmpdir().equalsIgnoreCase("") && !job.getTmpdir().equalsIgnoreCase("tmp")){
				Configuration.getLoggerInstance().debug("deleting ...");
				FileManagement.deleteInTMPDirectory(job.getTmpdir());
			}
		} else {
			try {
				String body ="id=track_error&job_id="+job.getTrackId()+"&data=parsing error for "+this.getId()+"  :  "+error;
				InternetConnection.sendPOSTConnection(job.getFeedbackUrl(), body,InternetConnection.MIME_TYPE_FORM_APPLICATION);
				Configuration.getLoggerInstance().debug(this.getId()+" bad parsed : "+error);
				return;
			} catch (IOException e) {
				Configuration.getLoggerInstance().error(e);
			}
		}
		Configuration.getLoggerInstance().debug("end job on track "+job.getTrackId()+" at "+new Date()+".");
		long end = System.currentTimeMillis();
		long time = (end-start);
		Configuration.getLoggerInstance().debug("time elapsed : ~"+time/1000+" sec.");
	}









	//LOGGER

	public static Logger initLogger(String name) {
		Logger out = Logger.getLogger(name);
		out.setAdditivity(false);
		out.setLevel(Level.DEBUG);
		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
		RollingFileAppender appender = null;
		try {
			appender = new RollingFileAppender(layout,Configuration.getLogger(),true);
		} catch (IOException e) {
			Configuration.getLoggerInstance().error(e);
		}
		out.addAppender(appender);
		return out;
	}
}

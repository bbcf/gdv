package ch.epfl.bbcf.conversion.daemon;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import ch.epfl.bbcf.bbcfutils.Utility;
import ch.epfl.bbcf.bbcfutils.access.gdv.RemoteAccess;
import ch.epfl.bbcf.bbcfutils.conversion.json.ConvertToJSON;
import ch.epfl.bbcf.bbcfutils.conversion.sqlite.ConvertToSQLite;
import ch.epfl.bbcf.bbcfutils.conversion.sqlite.ConvertToSQLite.Extension;
import ch.epfl.bbcf.bbcfutils.exception.ExtensionNotRecognisedException;
import ch.epfl.bbcf.bbcfutils.parser.exception.ParsingException;
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



		//////////   GETTING MD5   //////////
		Configuration.getLoggerInstance().debug(this.getId()+" run : "+job.getFile());
		File file = new File(job.getFile());
		String md5;
		try {
			md5 = Utility.getFileDigest(file.getAbsolutePath(),"SHA1");
		} catch (IOException e2) {
			Configuration.getLoggerInstance().error(e2);
			return;
		} catch (NoSuchAlgorithmException e) {
			Configuration.getLoggerInstance().error(e);
			return;
		}
		if(null==md5){
			try {
				RemoteAccess.sendTrackErrorMessage(job.getFeedbackUrl(),
						"cannot get md5","md5",Integer.toString(job.getTrackId()), job.getFile());
			} catch (IOException e) {
				Configuration.getLoggerInstance().error(e);
			}
			return;
		}


		boolean doJSON = false;
		boolean doSQLite = false;
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
		}else if(job.getExtension().equalsIgnoreCase("bam")||
				job.getExtension().equalsIgnoreCase("sam")){
			ext = Extension.BAM;
			type="qualitative";
			try {
				RemoteAccess.sendTrackErrorMessage(job.getFeedbackUrl(),
						"extension not supported : "+ext,"ext",Integer.toString(job.getTrackId()),
						job.getFile());
			} catch (IOException e) {
				Configuration.getLoggerInstance().error(e);
			}
		}else {
			try {
				RemoteAccess.sendTrackErrorMessage(job.getFeedbackUrl(),
						"extension not recognized : "+ext,"ext",Integer.toString(job.getTrackId()),
						job.getFile());
			} catch (IOException e) {
				Configuration.getLoggerInstance().error(e);
			}
			return;
		}
		String database = md5+".db";
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
				}
			}
		}
		Configuration.getLoggerInstance().info("end sqlite conversion");
		//JSON 
		if(doJSON){
			Configuration.getLoggerInstance().debug(this.getId()+" start of convertion to JSON :  " +
					""+database+" to "+job.getJbrowseOutputDirectory()+"/"+database);
			ConvertToJSON convertor = new ConvertToJSON(job.getOutputDirectory()+"/"+database, type);
			/**
			 * String outputPath,String dbName,String ressourceUrl,String trackName
			 */
			try {
				wellParsed = convertor.convert(job.getJbrowseOutputDirectory(),
						database,job.getJbrowseRessourcesUrl(),file.getName());
			} catch (ParsingException e) {
				Configuration.getLoggerInstance().error(e);
				wellParsed=false;
				error+=e.getMessage();
			}
		}
		Configuration.getLoggerInstance().info("end JSON conversion");
		//////////   FEEDBACK   //////////
		if(wellParsed){
			Configuration.getLoggerInstance().debug(this.getId()+" well parsed");
			try {
				RemoteAccess.sendTrackSucceed(job.getFeedbackUrl(),Integer.toString(job.getTrackId()),database,job.getMail(),type);
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
				RemoteAccess.sendTrackErrorMessage(
						job.getFeedbackUrl(),"parsing error "+error,"parsing", Integer.toString(job.getTrackId()),job.getFile());
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

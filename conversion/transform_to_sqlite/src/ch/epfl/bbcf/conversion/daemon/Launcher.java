package ch.epfl.bbcf.conversion.daemon;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.json.JSONException;

import ch.epfl.bbcf.bbcfutils.access.gdv.RemoteAccess;
import ch.epfl.bbcf.bbcfutils.access.genrep.GenRepAccess;
import ch.epfl.bbcf.conversion.conf.Configuration;
import ch.epfl.bbcf.conversion.conf.Configuration.Extension;
import ch.epfl.bbcf.conversion.convertor.Convertor;
import ch.epfl.bbcf.conversion.convertor.Convertor.FileExtension;
import ch.epfl.bbcf.conversion.exception.JSONConversionException;
import ch.epfl.bbcf.conversion.parser.BAMParser;
import ch.epfl.bbcf.conversion.parser.GFFParser;
import ch.epfl.bbcf.utility.ProcessLauncher;
import ch.epfl.bbcf.utility.ProcessLauncherError;
import ch.epfl.bbcf.utility.file.FileManagement;




/**
 * Class which choose what parsing to launch in function
 * of the parameters given
 * @author Yohan Jarosz
 *
 */
public class Launcher extends Thread{

	public static final Logger logger = initLogger(Launcher.class.getName());
	/**
	 * the track id in your database
	 */
	private String trackId;
	/**
	 * the extension of the file (must be part of Configuration.Extension)
	 */
	private String extension;
	/**
	 * the nr assembly id in Genrep
	 */
	private String nrAssemblyId;

	/**
	 * a directory to delete after use
	 */
	private String tmpdir;

	/**
	 * mail of an user (to send feedback when a track succeed)
	 */
	private String mail;
	/**
	 * the parh of the file to parse
	 */
	private String filePath;


	public void process(String filePath,String trackId,String tmpdir,
			String extension,String mail,String nrAssemblyId) {
		this.filePath = filePath;
		this.trackId = trackId;
		this.tmpdir=tmpdir;
		this.mail = mail;
		this.extension = extension;
		this.nrAssemblyId = nrAssemblyId;
		ManagerService.submitSQLiteProcess(this);
	}

	public void run(){
		long start = System.currentTimeMillis();



		//////////   GETTING MD5   //////////
		logger.debug(this.getId()+" run : "+filePath);
		File file = new File(filePath);
		String md5 = null;
		try {
			md5 = ProcessLauncher.getFileMD5(file);
		} catch (ProcessLauncherError e) {
			logger.error(e);
		}
		if(null==md5){
			try {
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),"cannot get md5","md5",trackId, filePath);
			} catch (IOException e) {
				logger.error(e);
			}
			return;
		}



		//////////   EXTENSION   //////////
		Extension ext = null;
		if(extension.equalsIgnoreCase("wig")){
			ext = Extension.WIG;
		} else if(extension.equalsIgnoreCase("bedgraph")){
			ext = Extension.BEDGRAPH;
		}else if(extension.equalsIgnoreCase("bed")){
			ext = Extension.BED;
		}else if(extension.equalsIgnoreCase("gff")){
			ext = Extension.GFF;
		}else if(extension.equalsIgnoreCase("gtf")){
			ext = Extension.GFF;
		}else if(extension.equalsIgnoreCase("db")){

		}else if(extension.equalsIgnoreCase("bam")||
				extension.equalsIgnoreCase("sam")){
			ext = Extension.BAM;
		}else {
			try {
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),"extension not recognized : "+ext,"ext",trackId, filePath);
			} catch (IOException e) {
				logger.error(e);
			}
			return;
		}
		//////////   GET LIST OF CHR TO PROCESS   //////////
		List<String> chrList = new ArrayList<String>();
		try {
			chrList = GenRepAccess.getChromosomesListByAssemblyId(nrAssemblyId);
		} catch (JSONException e2) {
			logger.error(e2);
		} catch (IOException e2) {
			logger.error(e2);
		}
		logger.debug(this.getId()+" chrlist from generep : "+chrList.toString());
		if(chrList.isEmpty()){
			try {
				RemoteAccess.sendChromosomeErrorMessage(Configuration.getFeedbackUrl(),"chromosome list is empty","chrlist",trackId,nrAssemblyId);
			} catch (IOException e) {
				logger.error(e);
			}
			return;
		}
		boolean doJSON = true;
		//////////   PARSING   //////////
		logger.debug(this.getId()+" start of parsing "+md5);
		String database = md5+".db";
		String type = "";
		boolean wellParsed = false;
		Parser parser = null;
		Convertor convertor = null;
		switch(ext){
		case WIG:case BEDGRAPH:
			type = "quantitative";
			parser = new WIGParser(Processing.SEQUENCIAL);
			convertor = new Convertor(
					file.getAbsolutePath(),
					FileExtension.WIG,nrAssemblyId);
			doJSON = false;
			break;
		case BED:
			type = "qualitative";
			parser = new BEDParser(Processing.SEQUENCIAL);
			convertor = new Convertor(
					file.getAbsolutePath(),
					FileExtension.BED,nrAssemblyId);
			break;
		case GFF:
			type = "qualitative";
			parser = new GFFParser(Processing.SEQUENCIAL);
			convertor = new Convertor(
					file.getAbsolutePath(),
					FileExtension.GFF,nrAssemblyId);
			break;
		case BAM:
			type="qualitative";
			parser = new BAMParser(Processing.SEQUENCIAL);
			convertor = new Convertor(
					file.getAbsolutePath(),
					FileExtension.BAM,nrAssemblyId);
			break;
		}


		convertor.setParameters(nrAssemblyId,chrList);

		try {
			//do sqlite
			convertor.doSqlite(Configuration.getSqliteOutput(),database);
		} catch (InstantiationException e2) {
			try {
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),e2.getMessage(),"parsing", trackId,filePath);
			} catch (IOException e) {
				logger.error(e);
			}
		} catch (IllegalAccessException e2) {
			try {
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),e2.getMessage(),"parsing", trackId,filePath);
			} catch (IOException e) {
				logger.error(e);
			}
		} catch (ClassNotFoundException e2) {
			try {
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),e2.getMessage(),"parsing", trackId,filePath);
			} catch (IOException e) {
				logger.error(e);
			}
		} catch (SQLException e2) {	
			try {
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),e2.getMessage(),"parsing", trackId,filePath);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		try {
			//do JSON
			if(doJSON){
				convertor.doJBrowse(Configuration.getSqliteOutput()+"/"+database,
						Configuration.getJbrowseOutput(), Configuration.getRessourceURL(),database);
			}
		} catch (JSONConversionException e1) {
			try {
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),e1.getMessage(),"parsing", trackId,filePath);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		try{
			try {
				logger.debug("parse");
				//parse file
				parser.parse(file,convertor);
				logger.debug("end parse");
			} catch (IOException e1) {
				logger.error(e1);
				wellParsed = false;
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),e1.getMessage(),"parsing", trackId,filePath);
				return;
			} catch (ParsingException e1) {
				logger.error(e1);
				wellParsed = false;
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),e1.getMessage(),"parsing", trackId,filePath);
				return;
			}
		}catch(IOException e){
			logger.error(e);
		}
		wellParsed = true;
		logger.debug(this.getId()+" end of parsing");
		//////////   FEEDBACK   //////////
		if(wellParsed){
			logger.debug(this.getId()+"well parsed");
			try {
				RemoteAccess.sendTrackSucceed(Configuration.getFeedbackUrl(),trackId,database,mail,type);
			} catch (IOException e) {
				logger.error(e);
			}

			//DELETING TMPDIR
			logger.debug("delete tmpdir : "+tmpdir);
			if(tmpdir!=null && !tmpdir.equalsIgnoreCase("") && !tmpdir.equalsIgnoreCase("tmp")){
				logger.debug("deleting ...");
				FileManagement.deleteInTMPDirectory(tmpdir);
			}
		} else {
			try {
				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),"parsing error","parsing", trackId,filePath);
				return;
			} catch (IOException e) {
				logger.error(e);
			}
		}
		logger.debug("end job on track "+trackId+" at "+new Date()+".");
		long end = System.currentTimeMillis();
		long time = (end-start);
		logger.debug("time elapsed : ~"+time/1000+" sec.");
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
			logger.error(e);
		}
		out.addAppender(appender);
		return out;
	}
}

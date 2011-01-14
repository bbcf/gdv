package ch.epfl.bbcf.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.biojava.bio.BioException;
import org.biojava.bio.program.gff.GFFParser;
import org.biojava.utils.ParserException;

import ch.epfl.bbcf.conf.Configuration;
import ch.epfl.bbcf.conf.Configuration.Extension;
import ch.epfl.bbcf.connection.RemoteAccess;
import ch.epfl.bbcf.connection.GenRepAccess;
import ch.epfl.bbcf.formats.json.BASICCreator;
import ch.epfl.bbcf.formats.json.GFFCreator;
import ch.epfl.bbcf.formats.parser.BEDParser;
import ch.epfl.bbcf.formats.parser.CustomGFFHandler;
import ch.epfl.bbcf.formats.parser.WIGParser;
import ch.epfl.bbcf.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.formats.sqlite.SQLiteQualitativeHandler;
import ch.epfl.bbcf.formats.sqlite.SQLiteQuantitativeHandler;
import ch.epfl.bbcf.transform.GTF2GFF;
import ch.epfl.bbcf.utility.FileManagement;
import ch.epfl.bbcf.utility.ManagerService;
import ch.epfl.bbcf.utility.ParsingException;
import ch.epfl.bbcf.utility.ProcessLauncher;
import ch.epfl.bbcf.utility.ProcessLauncherError;



//TODO cannot make admin wig tracks
public class Launcher extends Thread{

	public static final Logger logger = initLogger(Launcher.class.getName());
	private String trackId;
	private String extension;
	private String nrAssemblyId;
	private String tmpdir;
	private String mail;
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
		//GETTING MD5
		File file = new File(filePath);
		String md5 = null;
		try {
			md5 = ProcessLauncher.getFileMD5(file);
		} catch (ProcessLauncherError e) {
			logger.error(e);
		}
		//md5 = "AAA";
		if(null==md5){
			RemoteAccess.sendTrackErrorMessage("cannot get md5","md5",trackId, filePath);
			return;
		}
		//EXTENSION
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
			String newPath = filePath.substring(0,filePath.length()-4)+".gff";
			GTF2GFF.transform(filePath,newPath);
			FileManagement.deleteDirectory(new File(filePath));
			file = new File(newPath);
			ext = Extension.GFF;
		}else if(extension.equalsIgnoreCase("db")){
			
		}else {
			RemoteAccess.sendTrackErrorMessage("extension not recognized : "+ext,"ext",trackId, filePath);
			return;
		}
		//GET LIST OF CHR TO PROCESS
		List<String> chrList = GenRepAccess.getChromosomesListByAssemblyId(nrAssemblyId);
		logger.debug("chrlist from generep : "+chrList.toString());
		if(chrList.isEmpty()){
			RemoteAccess.sendChromosomeErrorMessage("chromosome list is empty","chrlist",trackId,nrAssemblyId);
			return;
		}
		//PARSING
		String database = md5+".db";
		String type = "";
		boolean wellParsed = false;
		try{
			switch(ext){
			case WIG:case BEDGRAPH:
				type = "quantitatif";
				wellParsed = processWig(file,database,ext,chrList);
				break;
			case BED:
				type = "qualitatif";
				wellParsed = processBED(file,database,chrList);
				break;
			case GFF:
				type = "qualitatif";
				wellParsed = processGFF(file,database,nrAssemblyId,chrList);
				break;
			}
		}catch(Exception e){
			logger.debug("error in parsing "+file+" : "+e);
			for(StackTraceElement el : e.getStackTrace()){
				logger.debug(el.getClassName()+" "+el.getMethodName()+" "+el.getLineNumber());
			}

			RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId,filePath);
			return;
		}
		//FEEDBACK TO GDV
		if(wellParsed){
			RemoteAccess.sendTrackSucceed(trackId,database,mail,type);

			//DELETING TMPDIR
			logger.debug("TMPDIR : "+tmpdir);
			if(tmpdir!=null && !tmpdir.equalsIgnoreCase("") && !tmpdir.equalsIgnoreCase("tmp")){
				FileManagement.deleteInTMPDirectory(tmpdir);
			}
		} else {
			RemoteAccess.sendTrackErrorMessage("parsing error","parsing", trackId,filePath);
		}
		logger.debug("end job on track "+trackId+" at "+new Date()+".");
		long end = System.currentTimeMillis();
		long time = (end-start);
		logger.debug("time elapsed : ~"+time/1000+" sec.");
	}






	//PROCESSING METHODS
	private boolean processBED(File file,String database, List<String> chrList){
		logger.debug("processing bed file "+file.getName()+" and make database "+database); 
		SQLiteQualitativeHandler handler = new SQLiteQualitativeHandler(database);
		BASICCreator writer = new BASICCreator(database,file,file.getName());
		if(!handler.exist()){
			handler.createNewDatabase();
			BEDParser parser = new BEDParser(handler,writer,file,nrAssemblyId,chrList);
			try {
				parser.parse(file);
			} catch (IOException e) {
				logger.error(e);
				RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId, filePath);
				return false;
			} catch (ParsingException e) {
				logger.error(e);
				RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId, filePath);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * processing GFF file
	 * @param file - the file to be processed
	 * @param database - the output database
	 * @param nrAssemblyId - the species the file is about
	 * @param chrList - the chromosomes the file should have
	 * @return
	 */
	private boolean processGFF(File file,String database, String nrAssemblyId, List<String> chrList){
		logger.debug("processing gff file "+file.getName()+" a make database "+database);
		//Map<String, String> altsNames = getAltNames(nrAssemblyId);
		SQLiteQualitativeHandler handler = new SQLiteQualitativeHandler(database);

		//List<String> chrList = null;
		GFFCreator writer = new GFFCreator(database,file,file.getName());
		if(!handler.exist()){
			handler.createNewDatabase();
			BufferedReader br = null;
			try {
				br = new BufferedReader(
						new FileReader(file));
			} catch (FileNotFoundException e) {
				logger.error(e);
				RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId, filePath);
				return false;
			}

			CustomGFFHandler gff = new CustomGFFHandler(handler,writer,file,nrAssemblyId,chrList);
			GFFParser parser = new GFFParser();
			try {
				parser.parse(br, gff);
			} catch (IOException e) {
				logger.error(e);
				RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId, filePath);
				return false;
			} catch (BioException e) {
				logger.error(e);
				RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId, filePath);
				return false;
			} catch (ParserException e) {
				logger.error(e);
				RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId, filePath);
				return false;
			}
		}
		return true;
	}

	private boolean processWig(File file,String database, Extension ext, List<String> chrList)  {
		logger.debug("processing wig file "+file.getName()+" a make database "+database);
		boolean b = false;
		SQLiteQuantitativeHandler sqliteHandler = new SQLiteQuantitativeHandler(database);
		if(!sqliteHandler.exist()){
			sqliteHandler.createNewDatabase();
			int trackType;
			switch(ext){
			case WIG:trackType = WIGParser.WIG_TRACK_TYPE;break;
			case BEDGRAPH:trackType = WIGParser.BEDGRAPH_TRACK_TYPE;break;
			default:trackType = WIGParser.WIG_TRACK_TYPE;
			}
			WIGParser parser = new WIGParser(sqliteHandler,trackType,nrAssemblyId,chrList);

			try {
				b = parser.parse(file);
			} catch (IOException e) {
				logger.error(e);
				RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId, filePath);
				return false;
			} catch (ParsingException e) {
				logger.error(e);
				RemoteAccess.sendTrackErrorMessage(e.toString(),"parsing", trackId, filePath);
				return false;
			}
		} else {
			sqliteHandler.close();
		}
		return b;
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



	//ALTS NAMES
	/**
	 * get the alternatives names a gene can have. (gene name : [alt1,alt2,....])
	 * the assembly id is from genrep
	 * @param species
	 * @return
	 */
	private static Map<String, String> getAltNames(String assemblyId) {
		if(null!=assemblyId){
			if(assemblyId.equalsIgnoreCase("170") || assemblyId.equalsIgnoreCase("170")){
				return SQLiteAccess.getYeastHash();
			} else if(assemblyId.equalsIgnoreCase("Mus musculus")){
				return null;
			}
		}
		return null;
	}	

	//JSON

	/**
	 * get the JSON String to record in file, that JBrowse need 
	 * in order to show qualitative tracks
	 * @param altsNames optionnal (the species : to replace the name of
	 * the gene by a more common name)
	 */
	public static void writeJSONDescriptor(String database, Map<String, String> descriptors,File file,String outpath) {
		logger.debug("write json desc : "+file.getName()+" -> "+database);
		File directory = new File(outpath+"/"+database);
		directory.mkdir();
		Iterator<String> it = descriptors.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String params = descriptors.get(key);
			int featureCount = SQLiteAccess.getFeatureCountForChromosome(database,key);
			params=params.substring(0, params.length()-1);
			params+="]";
			String finalParam = "{\"headers\":[\"start\",\"end\",\"strand\",\"id\",\"name\"]," +
			"\"subfeatureClasses\":null," +
			"\"featureCount\":"+featureCount+"," +
			"\"key\":\""+file.getName()+"\"," +
			"\"featureNCList\":[" +params+


			",\"className\":\"feature2\"," +
			"\"clientConfig\":null," +
			"\"rangeMap\":[]," +
			"\"arrowheadClass\":null," +
			"\"subfeatureHeaders\":[\"start\",\"end\",\"strand\",\"id\",\"type\"]," +
			"\"type\":\"FeatureTrack\"," +
			"\"label\":\"Alignments\"," +
			"\"sublistIndex\":5}";

			File tmp = new File(outpath+"/"+database+"/"+key+".json");
			logger.debug("file : "+tmp.getAbsolutePath());
			try {
				tmp.createNewFile();
				FileManagement.writeTo(finalParam, tmp);
			} catch (IOException e) {
				logger.error(e);
			}
		}




	}
}

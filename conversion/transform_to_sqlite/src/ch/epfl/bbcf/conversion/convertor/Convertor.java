package ch.epfl.bbcf.conversion.convertor;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;

import ch.epfl.bbcf.bbcfutils.access.genrep.pojo.Chromosome;
import ch.epfl.bbcf.bbcfutils.parser.Handler;
import ch.epfl.bbcf.bbcfutils.parser.feature.Feature;
import ch.epfl.bbcf.bbcfutils.parser.feature.Track;
import ch.epfl.bbcf.conversion.daemon.Launcher;
import ch.epfl.bbcf.conversion.exception.JSONConversionException;
import ch.epfl.bbcf.conversion.feature.JSONFeature;
import ch.epfl.bbcf.utility.ChromosomeNameHandler;

public class Convertor implements Handler{

	public static final Logger logger = Launcher.initLogger(Convertor.class.getName());

	public enum FileExtension {BED,WIG,BAM,SAM,GFF};

	private FileExtension extension;

	private boolean doSqlite;
	private SQLiteConvertor sqlite_handler;

	private boolean doJSON;
	private JBrowseConvertor json_handler;

	private String inputPath;

	private boolean endCalled;

	/**
	 * the name of the input file
	 */
	private String fileName;

	private List<Chromosome> chromosomes;

	ChromosomeNameHandler nameHandler;

	private String nrAssemblyId;

	/**
	 * will convert a parsed file to sqlite and/or json
	 * doSqlite() and/or doJson() is/are called
	 * @param inputPath - the path of the file being parsed
	 * @param outputDirectoryPath - the output directory 
	 * @param extension - the extension of the parsed file
	 */
	public Convertor(String inputPath,FileExtension extension,String nrAssemblyId){
		this.setInputPath(inputPath);
		this.nrAssemblyId = nrAssemblyId;
		this.fileName = inputPath.substring(inputPath.lastIndexOf("/")+1,inputPath.lastIndexOf("."));
		this.extension = extension;
		nameHandler = new ChromosomeNameHandler();
		endCalled = false;
	}

	/**
	 * call this method if you want to enable
	 * the conversion to sqlite databases
	 * @param outputDirectoryPath - where the output should go
	 * @param limitQueriesSize - the limit of queries on the
	 * database connection before doing a commit
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void doSqlite(String outputDirectoryPath,String outputdbName,int limitQueriesSize) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		this.doSqlite = true;
		this.sqlite_handler = new SQLiteConvertor(this,outputDirectoryPath+"/"+outputdbName+".db",extension,limitQueriesSize,nrAssemblyId);
	}
	/**
	 * call this method if you want to enable
	 * the conversion to sqlite databases
	 * @param outputDirectoryPath - where the output should go
	 * @param outputFileName - the name you give to the database
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void doSqlite(String outputDirectoryPath,String outputFileName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		this.doSqlite = true;
		this.sqlite_handler = new SQLiteConvertor(this,outputDirectoryPath+"/"+outputFileName,extension,nrAssemblyId);
	}

	public boolean isDoSqlite() {
		return doSqlite;
	}


	/**
	 * call this method if you want to enable
	 * the conversion to JSON 
	 * @param outputDirectoryPath - where the output should go
	 * @param ressourceUrl - the url where the browser will link to fetch the resources (tracks)
	 * @param fullPathDatabase - the full path name to the database
	 * WARNING : if you call this method
	 * you must ensure that your gff file is sorted by chromosomes 
	 * @param name - name of the outputDirectory 
	 * @throws JSONConversionException 
	 */
	public void doJBrowse(String fullPathDatabase,String outputDirectoryPath,String ressourceUrl,String outputName) throws JSONConversionException {
		this.doJSON = true;
		this.json_handler = new JBrowseConvertor(inputPath,fullPathDatabase,outputDirectoryPath,ressourceUrl,fileName,outputName,extension);
	}

	public boolean isDoJSON() {
		return doJSON;
	}






	@Override
	public void newFeature(Feature feature) {
		String chr = feature.getChromosome();
		boolean contains = false;
		for(Chromosome chromosome : chromosomes){
			if(chromosome.getName().equalsIgnoreCase(chr)){
				contains=true;
			}
		}
		if(!contains){
			chr = nameHandler.getChromosomeAltName(nrAssemblyId,chr);
		}
//		if(!chromosomes.contains(chr)){
//			chr = nameHandler.getChromosomeAltName(nrAssemblyId,chr);
//		}
//		if(null==chr){
//			return;
//		}
		feature.setChromosome(chr);
		if(doSqlite){
			try {
				sqlite_handler.newFeature(feature);
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		if(doJSON){
			try {
				JSONFeature feat= new JSONFeature(feature);
				json_handler.newFeature(feat);
			} catch (JSONException e) {
				logger.error(e);
			} catch (FileNotFoundException e) {
				logger.error(e);
			}
		}
	}

	@Override
	public void newTrack(Track track) {
		if(doSqlite){
			try {
				sqlite_handler.newTrack(track);
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		if(doJSON){
			json_handler.newTrack(track);
		}

	}

	@Override
	public void start() {
		logger.debug("START");
		if(doSqlite){
			sqlite_handler.start();
		}
		if(doJSON){
			json_handler.start();
		}

	}

	@Override
	public void end() {
		if(!endCalled){
			endCalled = true;
			if(doSqlite){
				try {
					sqlite_handler.end();
					logger.debug("end sqlite");
				} catch (SQLException e) {
					logger.error(e);
				}
			}
			if(doJSON){
				json_handler.end();
				logger.debug("end json");
			}
		}
	}

	public JBrowseConvertor getJSONConvertor(){
		return json_handler;
	}
	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public String getInputPath() {
		return inputPath;
	}

	/**
	 * set the chromosome names list accepted by jbrowse
	 * if a chromosome encountered in the file is not in 
	 * this list, perhaps it's an other and need to be converted
	 * (e.g chr1 to chrI)
	 * @param nrAssemblyId - nrAssembly id form Genrep
	 * @param chrList - the chromosome listw
	 */
	public void setParameters(String nrAssemblyId, List<Chromosome> chrList) {
		this.nrAssemblyId = nrAssemblyId;
		this.chromosomes = chrList;
	}

	public List<Chromosome> getChromosomes(){
		return this.chromosomes;
	}
}

package ch.epfl.bbcf.conversion.convertor;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.json.JSONException;

import ch.epfl.bbcf.conversion.exception.JSONConversionException;
import ch.epfl.bbcf.conversion.feature.JSONFeature;
import ch.epfl.bbcf.feature.Feature;
import ch.epfl.bbcf.feature.Track;
import ch.epfl.bbcf.parser.Handler;

public class Convertor implements Handler{

	public enum FileExtension {BED,WIG,BAM,SAM,GFF};
	
	private FileExtension extension;
	
	private boolean doSqlite;
	private SQLiteConvertor sqlite_handler;
	
	private boolean doJSON;
	private JBrowseConvertor json_handler;
	
	private String inputPath;

	/**
	 * the name of the input file
	 */
	private String fileName;
	
	
	/**
	 * will convert a parsed file to sqlite and/or json
	 * doSqlite() and/or doJson() is/are called
	 * @param inputPath - the path of the file being parsed
	 * @param outputDirectoryPath - the output directory 
	 * @param extension - the extension of the parsed file
	 */
	public Convertor(String inputPath,FileExtension extension){
		this.setInputPath(inputPath);
		this.fileName = inputPath.substring(inputPath.lastIndexOf("/")+1,inputPath.lastIndexOf("."));
		this.extension = extension;
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
		this.sqlite_handler = new SQLiteConvertor(outputDirectoryPath+"/"+outputdbName+".db",extension,limitQueriesSize);
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
		this.sqlite_handler = new SQLiteConvertor(outputDirectoryPath+"/"+outputFileName,extension);
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
	 * @throws JSONConversionException 
	 */
	public void doJBrowse(String fullPathDatabase,String outputDirectoryPath,String ressourceUrl) throws JSONConversionException {
		this.doJSON = true;
		this.json_handler = new JBrowseConvertor(inputPath,fullPathDatabase,outputDirectoryPath,ressourceUrl,fileName,extension);
	}
	
	public boolean isDoJSON() {
		return doJSON;
	}

	
	
	
	
	
	@Override
	public void newFeature(Feature feature) {
		if(doSqlite){
			try {
				sqlite_handler.newFeature(feature);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(doJSON){
			try {
				JSONFeature feat = new JSONFeature(feature);
				json_handler.newFeature(feat);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void newTrack(Track track) {
		if(doSqlite){
			try {
				sqlite_handler.newTrack(track);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(doJSON){
			json_handler.newTrack(track);
		}
		
	}

	@Override
	public void start() {
		if(doSqlite){
				sqlite_handler.start();
		}
		if(doJSON){
			json_handler.start();
		}
		
	}

	@Override
	public void end() {
		if(doSqlite){
			try {
				sqlite_handler.end();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(doJSON){
			json_handler.end();
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
	
}

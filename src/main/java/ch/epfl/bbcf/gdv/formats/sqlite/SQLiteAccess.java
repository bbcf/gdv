package ch.epfl.bbcf.gdv.formats.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.bbcf.gdv.access.sqlite.pojo.ChromosomeFeature;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

public class SQLiteAccess {

	////////////////////////////
	////// FILE DIRECTORY //////
	////////////////////////////
	/**
	 * get a sqlite connection on the file directory in gdv
	 * @param database
	 * @return
	 */
	public static Connection getConnectionOnFileDirectory(String database){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getFilesDir()+"/"+database);
			return conn;
		} catch (InstantiationException e) {
			Application.error(e);
		} catch (IllegalAccessException e) {
			Application.error(e);
		} catch (ClassNotFoundException e) {
			Application.error(e);
		} catch (SQLException e) {
			Application.error(e);
		}
		return null;
	}

	public static Connection getConnectionOnCalculatedSQLite(String database){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getTracks_dir()+"/"+database);
			return conn;
		} catch (InstantiationException e) {
			Application.error(e);
		} catch (IllegalAccessException e) {
			Application.error(e);
		} catch (ClassNotFoundException e) {
			Application.error(e);
		} catch (SQLException e) {
			Application.error(e);
		}
		return null;
	}

	public static ResultSet getResultSet(Statement stat,String query) throws SQLException{
		return stat.executeQuery(query);
	}


	public static Float getMaxScoreForChr(String database, String chr) {
		try{
			Connection conn = getConnectionOnFileDirectory(database);
			Statement stat = conn.createStatement();
			String query = "SELECT max(score) FROM "+chr+";";
			ResultSet rs = getResultSet(stat, query);
			Float f = null;
			while (rs.next()) {
				f= rs.getFloat(1);
			}
			rs.close();
			return f;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * get the maximum length of a chromosome
	 * in the database specified
	 * @param chr
	 * @param database
	 * @return
	 */
	public static int getMaxLengthForChromosome(String chr,String database) {
		int f = 0;
		try{
			Connection conn = getConnectionOnFileDirectory(database);
			Statement stat = conn.createStatement();
			String query = "SELECT max(end) FROM "+chr+";";
			ResultSet rs = getResultSet(stat, query);
			while (rs.next()) {
				f= rs.getInt(1);
			}
			rs.close();
			return f;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return f;
	}
	public static ChromosomeFeature getScoresForChromosome(String database,String chrName){
		try{
			Connection conn = getConnectionOnFileDirectory(database);
			Statement stat = conn.createStatement();
			String query ="select * from "+chrName+";";
			ResultSet rs = getResultSet(stat, query);
			ChromosomeFeature feat = new ChromosomeFeature();
			while (rs.next()) {
				feat.addStart(rs.getInt("start"));
				feat.addScore(rs.getFloat("score"));
				feat.addStop(rs.getInt("end"));
			}
			rs.close();
			conn.close();
			return feat;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * get the all the chromosomes names in a sqlite database
	 * @param dbName
	 * @return
	 */
	public static List<String> getChromosomesNames(String dbName) {
		List<String> chrNames = new ArrayList<String>();
		Connection conn;
		try {
			conn = getConnectionOnFileDirectory(dbName);
			Statement stat = conn.createStatement();
			String query = "SELECT name FROM sqlite_master where type='table'and name!='attributes';";
			ResultSet rs = getResultSet(stat, query);
			while (rs.next()) {
				chrNames.add(rs.getString(1));
			}
			rs.close();
		} catch (SQLException e) {
			Application.error(e);
		}
		return chrNames;
	}


	/**
	 * get the all the chromosomes names in a sqlite database
	 * @param dbName
	 * @return
	 */
	public static List<String> getChromosomesNames(Connection conn) {
		List<String> chrNames = new ArrayList<String>();
		try {
			Statement stat = conn.createStatement();
			String query = "SELECT name FROM sqlite_master where type='table'and name!='attributes';";
			ResultSet rs = getResultSet(stat, query);
			while (rs.next()) {
				chrNames.add(rs.getString(1));
			}
			rs.close();
		} catch (SQLException e) {
			Application.error(e);
		}
		return chrNames;
	}









	public static Map<String, Integer> getChromosomesAndLength(String dbName) {
		Map<String,Integer> result = new HashMap<String,Integer>();
		Connection conn;
		try {
			conn = getConnectionOnFileDirectory(dbName);
			Statement stat = conn.createStatement();
			String query = "SELECT * FROM chrNames;";
			ResultSet rs = getResultSet(stat, query);
			while (rs.next()) {
				result.put(rs.getString("name"),
						rs.getInt("length"));
			}
			rs.close();
		} catch (SQLException e) {
			Application.error(e);
		}
		return result;
	}
	public static String getStringAttribute(String database, String key) {
		String result = null;
		Connection conn;
		try {
			conn = getConnectionOnFileDirectory(database);
			Statement stat = conn.createStatement();
			String query = "select * from attributes where key = ? ;";
			PreparedStatement prep = conn.prepareStatement(query);
			prep.setString(1, key);
			prep.execute();
			ResultSet rs = prep.getResultSet();//getResultSet(stat, prep);
			while (rs.next()) {
				result = rs.getString("value");
			}
			rs.close();
		} catch (SQLException e) {
			Application.error(e);
		}
		return result;
	}

	public static boolean 	dbAlreadyCreated(String database) {
		File test = new File(Configuration.getFilesDir()+"/"+database);
		return test.exists();
	}







	/////////////////////
	////// DAEMON //////
	///////////////////

	private static Connection calculatingScoreConnection;
	/**
	 * get a sqlite connection on the daemon directory in gdv
	 * @return
	 */
	private static Connection getConnectionOnCalculatingScoresDaemonDirectory(){
		if(null==calculatingScoreConnection){
			try {
				Class.forName("org.sqlite.JDBC").newInstance();
				calculatingScoreConnection = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getCompute_scores_daemon());
			} catch (InstantiationException e) {
				Application.error(e);
			} catch (IllegalAccessException e) {
				Application.error(e);
			} catch (ClassNotFoundException e) {
				Application.error(e);
			} catch (SQLException e) {
				Application.error(e);
			}
		}
		return calculatingScoreConnection;
	}
	private static Connection transformSQLiteConnection;
	private static Connection getConnectionOnTransformToSQLiteDaemonDirectory() {
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			transformSQLiteConnection = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getTransform_to_sqlite_daemon());
		} catch (InstantiationException e) {
			Application.error(e);
		} catch (IllegalAccessException e) {
			Application.error(e);
		} catch (ClassNotFoundException e) {
			Application.error(e);
		} catch (SQLException e) {
			Application.error(e);
		}
		return transformSQLiteConnection;
	}


	public static void writeNewJobCalculScores(String trackId, String indb,
			String inPath, String outdb, String outPath,
			String fast, String usermail) {
		Connection conn;
		try {
			conn = getConnectionOnCalculatingScoresDaemonDirectory();
			PreparedStatement stat = conn.prepareStatement("insert into jobs values (?,?,?,?,?,?,?); ");
			stat.setString(1,trackId);
			stat.setString(2,indb);
			stat.setString(3,inPath);
			stat.setString(4,outdb);
			stat.setString(5,outPath);
			stat.setString(6,fast);
			stat.setString(7,usermail);
			conn.setAutoCommit(false);
			stat.execute();
			conn.commit();
			Application.debug(trackId+" "+indb+" "+inPath+" "+outdb+" "+outPath+" "+fast+" "+usermail);
		} catch (SQLException e) {
			Application.error(e);
			writeNewJobCalculScores(trackId, indb, inPath, outdb, outPath, fast, usermail);
		}

	}



	public static void writeNewJobTransform(String filePath,int trackId, String tmpDir,
			String extension,String mail, String nrAssemblyId,int userId) {
		Application.info("write new sqlite job : file("+filePath+"),trackId("+trackId+"),tmpDir("+tmpDir+"),mail("+mail+")," +
				"nrAssembly("+nrAssemblyId+"),userId("+userId+")");
		Connection conn;
		try {
			conn = getConnectionOnTransformToSQLiteDaemonDirectory();
			PreparedStatement stat = conn.prepareStatement("insert into jobs values (?,?,?,?,?,?); ");
			stat.setString(1, filePath);
			stat.setInt(2, trackId);
			stat.setString(3,tmpDir);
			stat.setString(4,extension);
			stat.setString(5,mail);
			stat.setString(6,nrAssemblyId);
			conn.setAutoCommit(false);
			stat.execute();
			conn.commit();
		} catch (SQLException e) {
			Application.error(e,userId);
		}

	}



	/////////////////////////////
	////// DATABASES LINK //////
	////////////////////////////



	//-> yeast
	private static Connection getConnectionOnYeast() {
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getDatabases_link_dir()+"/sgd_names.db");
			return conn;
		} catch (InstantiationException e) {
			Application.error(e);
		} catch (IllegalAccessException e) {
			Application.error(e);
		} catch (ClassNotFoundException e) {
			Application.error(e);
		} catch (SQLException e) {
			Application.error(e);
		}
		return null;
	}


	//	public static String getSynonyms(String name) {
	//		Application.debug("get synonyms of : "+name);
	//		Connection conn;
	//		try {
	//			conn = getConnectionOnSQLiteDaemonDirectory();
	//			PreparedStatement stat = conn.prepareStatement("select alt from names where usual = ? ;");
	//			stat.setString(1,name);
	//			conn.setAutoCommit(false);
	//			stat.execute();
	//			ResultSet rs = stat.getResultSet();
	//			while (rs.next()) {
	//				return rs.getString(0);
	//			}
	//			rs.close();
	//			conn.commit();
	//		} catch (SQLException e) {
	//			Application.error(e);
	//		}
	//		return null;
	//		
	//	}



	public static Map<String,String> getYeastHash() {
		Application.debug("get map of : yeast");
		Connection conn;
		try {
			Map<String,String> map = new HashMap<String, String>();
			conn = getConnectionOnYeast();
			Statement stat = conn.createStatement();
			stat.execute("select * from names");
			ResultSet rs = stat.getResultSet();
			while (rs.next()) {
				map.put(rs.getString(1), rs.getString(2));
			}
			rs.close();
			return map;
		} catch (SQLException e) {
			Application.error(e);
		}
		return null;

	}

	/**
	 * get the scores for the html canvas 
	 * @param db the database
	 * @param img the image's number
	 * @return
	 */
	//	public static String getScoresForDatabase(String db, String img) {
	//		String result = "{";
	//		Connection conn;
	//		try {
	//			Map<String,String> map = new HashMap<String, String>();
	//			conn = getConnectionOnCalculatedSQLite(db);
	//			PreparedStatement stat = conn.prepareStatement("select pos,score from sc where number= ? order by pos asc");
	//			stat.setString(1, img);
	//			ResultSet rs = stat.executeQuery();
	//			while (rs.next()) {
	//				result+=""+rs.getString(1)+":"+rs.getString(2)+",";
	//			}
	//			rs.close();
	//			if(result.length()>1){
	//				result=result.substring(0, result.length()-1);
	//			}
	//			result+="}";
	//			return result;
	//		} catch (SQLException e) {
	//			Application.error(e);
	//		}
	//		return null;
	//
	//	}

	public static String getScoresForDatabaseByIdList(String db, String[] idList) {
		//result+="$"+id+"="+SQLiteAccess.getScoresForDatabase(params.getDb(),id);
		String result = "";
		Connection conn;
		try {
			conn = getConnectionOnCalculatedSQLite(db);
			PreparedStatement stat = conn.prepareStatement("select pos,score from sc where number= ? order by pos asc");
			for(String img:idList){
				boolean isData = false;
				result+="$"+img+"={";
				stat.setString(1, img);
				ResultSet rs = stat.executeQuery();
				while (rs.next()) {
					isData=true;
					result+=""+rs.getString(1)+":"+rs.getString(2)+",";
				}
				rs.close();
				if(isData){
					result=result.substring(0, result.length()-1);
				}
				if(img.length()>0){
					result+="}";
				}
			}
			return result;
		} catch (SQLException e) {
			Application.error(e);
		}
		return null;

	}


	/**
	 * try to find coordinates (start,end) of a gene by it's name
	 * @param chr - the chromosome
	 * @param name - the gene name
	 * @param db - the database
	 * @return a list of coordinates (start,end,start,end,start,end,....)
	 * @throws SQLException
	 */
	public static List<Integer> searchForGeneNameOnChromosome(String db,String chr,String name) throws SQLException {
		Connection conn;
		conn = getConnectionOnFileDirectory(db);
		List<Integer> result = new ArrayList<Integer>();
		String query = "SELECT start,end FROM \""+chr+"\" where name = ?; ";
		PreparedStatement prep = conn.prepareStatement(query);
		prep.setString(1, name);
		ResultSet r = getResultSet(prep, query);
		while(r.next()){
			result.add(r.getInt(1));
			result.add(r.getInt(2));
		}
		r.close();
		return result;
	}

	public static ResultSet getValuesForChromosome(
			Connection conn, String chr) {
		try {
			PreparedStatement prep = conn.prepareStatement("select * from "+chr+" ;");
			return prep.executeQuery();
		} catch (SQLException e) {
			Application.error(e);
		}
		return null;
	}

	public static int getFeatureCountForChromosome(Connection conn, String chr) {
		try {
			PreparedStatement prep = conn.prepareStatement("select count(*) from "+chr+" ;");
			ResultSet r = prep.executeQuery();
			if(r.next()){
				return r.getInt(1);	
			}

		} catch (SQLException e) {
			Application.error(e);
		}
		return 0;
	}




}

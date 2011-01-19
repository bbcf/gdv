package ch.epfl.bbcf.formats.sqlite;

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
import java.util.Random;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conf.Configuration;
import ch.epfl.bbcf.daemon.Launcher;


public class SQLiteAccess {

	///////////////////////////
	////////// INIT //////////
	//////////////////////////
	public static boolean initJobsDatabase(File jobs){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+jobs.getAbsolutePath());
			Statement stat = conn.createStatement();
			stat.execute("create table jobs " + 
					"(file text, " +
					"trackId text," + //the trackId in gdv database
					"tmpdir text," + //a tmp directory to delete after usage 
					"extension text," + //extension of the file
					"mail text," + // the mail to feedback (nomail if no feedback)
					"nrassemblyid text);");  // the assembly id of the genome in generep
			
		} catch (InstantiationException e) {
			logger.debug(e);
			return false;
		} catch (IllegalAccessException e) {
			logger.debug(e);
			return false;
		} catch (ClassNotFoundException e) {
			logger.debug(e);
			return false;
		} catch (SQLException e) {
			logger.debug(e);
			return false;
		}
		return true;
	}
	
	
	
	
	
	public static Logger logger = Launcher.initLogger(SQLiteAccess.class.getName());
	/////////////////////////////
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
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getSqliteOutput()+"/"+database);
			//Connection conn = DriverManager.getConnection("jdbc:sqlite:/Users/jarosz/Desktop/"+database);
			return conn;
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
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
			String query = "SELECT max(score) FROM \""+chr+"\";";
			ResultSet rs = getResultSet(stat, query);
			Float f = null;
			while (rs.next()) {
				f= rs.getFloat(1);
			}
			rs.close();
			conn.close();
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
			String query = "SELECT max(end) FROM \""+chr+"\";";
			ResultSet rs = getResultSet(stat, query);
			while (rs.next()) {
				f= rs.getInt(1);
			}
			rs.close();
			conn.close();
			return f;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return f;
	}
	//	public static ChromosomeFeature getScoresForChromosome(String database,String chrName){
	//		try{
	//			Connection conn = getConnectionOnFileDirectory(database);
	//			Statement stat = conn.createStatement();
	//			String query ="select * from "+chrName+";";
	//			ResultSet rs = getResultSet(stat, query);
	//			ChromosomeFeature feat = new ChromosomeFeature();
	//			while (rs.next()) {
	//				feat.addStart(rs.getInt("start"));
	//				feat.addScore(rs.getFloat("score"));
	//				feat.addStop(rs.getInt("end"));
	//			}
	//			rs.close();
	//			conn.close();
	//			return feat;
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}


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
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return chrNames;
	}



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
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
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
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
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
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return result;
	}

	public static boolean dbAlreadyCreated(String database) {
		File test = new File(Configuration.getSqliteOutput()+"/"+database);
		return test.exists();
	}

	/**
	 * Count the number of feature a chromosome have
	 * @param chr
	 * @return
	 */
	public static int getFeatureCountForChromosome(String database,String chr) {
		Connection conn;
		try {
			conn = getConnectionOnFileDirectory(database);
			PreparedStatement prep = conn.prepareStatement("select count(*) from \""+chr+"\" ;");
			ResultSet r = prep.executeQuery();
			if(r.next()){
				int result = r.getInt(1); 
				r.close();
				conn.close();
				return result;	
			}

		} catch (SQLException e) {
			logger.error(e);
		}
		return 0;
	}


	public static int getLengthForChromosome(String database, String chr) {
		Connection conn;
		try {
			conn = getConnectionOnFileDirectory(database);
			PreparedStatement stat = conn.prepareStatement("SELECT length FROM chrNames where name = ? limit 1;");
			stat.setString(1, chr);
			ResultSet rs = stat.executeQuery();
			while (rs.next()) {
				int result = rs.getInt(1); 
				rs.close();
				return result;	
			}
			rs.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return -1;
	}

	public static ResultSet getStartEndForChromosome(Connection conn, String database2,
			String chr) {
		try {
			Statement stat = conn.createStatement();
			String query = "SELECT start,end FROM \""+chr+"\"; ";
			ResultSet r = getResultSet(stat, query);
			return r;
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;
	}

	public static PreparedStatement initCountForChromosomePositionWindow(Connection conn,String chr){
		try {
			PreparedStatement stat = conn.prepareStatement("SELECT count(*) FROM \""+chr+"\" where start >= ? and end<= ?; ");
			return stat;
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;
	}

	public static int getCountForChromosomePositionWindow(
			PreparedStatement prep, int start, int end) {
		try {
			prep.setInt(1, start);
			prep.setInt(2, end);
			ResultSet r = prep.executeQuery();
			int result = r.getInt(1); 
			r.close();
			return result;
			} catch (SQLException e) {
			logger.equals(e);
		}
		return -1;
	}

	/////////////////////////////////////
	////// COMPUTE SCORES DATABASE //////
	/////////////////////////////////////

	/**
	 * get a sqlite connection on the daemon directory in gdv
	 * @return
	 */
	private static Connection getConnectionOnComputeSqliteScoreDaemonDirectory(){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getComputeSqliteDatabase());
			return conn;
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;
	}

	public static void writeNewJob(String trackId, String indb, String inPath,
			String outdb, String outPath, String fast, String mail) {
		Connection conn;
		try {
			conn = getConnectionOnComputeSqliteScoreDaemonDirectory();
			PreparedStatement stat = conn.prepareStatement("insert into jobs values (?,?,?,?,?,?,?); ");
			stat.setString(1,trackId);
			stat.setString(2,indb);
			stat.setString(3,inPath);
			stat.setString(4,outdb);
			stat.setString(5,outPath);
			stat.setString(6,fast);
			stat.setString(7,mail);
			conn.setAutoCommit(false);
			stat.execute();
			conn.commit();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}















	/////////////////////////////
	////// DATABASES LINK //////
	////////////////////////////



	//-> yeast alts names 
	private static Connection getConnectionOnYeast() {
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getDatabasesLink()+"/sgd_names.db");
			return conn;
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;
	}

	//-> chr alts names
	private static Connection getConnectionOnMouseAltsChromosomesNames(){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+Configuration.getDatabasesLink()+"/mouse_chr.db");
			return conn;
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;
	}
	public static String getMouseChromosomeAltName(String chr){
		Connection conn = getConnectionOnMouseAltsChromosomesNames();
		try {
			PreparedStatement prep = conn.prepareStatement("select 1 from chromosome_names where used = ? limit 1;");
			prep.setString(1, chr);
			ResultSet r = prep.executeQuery();
			if(r.next()){
				r.close();
				conn.close();
				return chr;
			}
			prep = conn.prepareStatement("select used from chromosome_names where alt = ? limit 1;");
			prep.setString(1, chr);
			ResultSet n = prep.executeQuery();
			if(n.next()){
				String result = n.getString(1);
				n.close();
				conn.close();
				return result; 
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return chr;
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
		logger.debug("get map of : yeast");
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
			logger.error(e);
		}
		return null;

	}


	public static void main(String[] args){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String db = "/Users/jarosz/Desktop/dumdekk.db";
		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+db);
			Statement stat = conn.createStatement();
			stat.execute("create table test(one text,two text,three integer);");
			String[] tab = new String[200000];
			Random r = new Random();
			for(int i=0;i<tab.length;i++){
				int nextInt = r.nextInt();
				tab[i]=Integer.toString(nextInt);
			}
			long start = System.currentTimeMillis();
			conn.setAutoCommit(false);

			for(int i=0;i<100000;i+=2){
				PreparedStatement prep = conn.prepareStatement("insert into test values (?,?,?); ");
				prep.setString(1,tab[i]);
				prep.setString(2,tab[i+1]);
				prep.setInt(3, i);
				prep.execute();
			}
			conn.commit();
			conn.close();
			long end = System.currentTimeMillis();
			long elapsed = (end -start);
			System.out.println("time : "+elapsed+" ms.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}







}

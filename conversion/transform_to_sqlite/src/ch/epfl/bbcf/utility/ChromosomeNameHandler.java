package ch.epfl.bbcf.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.conf.Configuration;
import ch.epfl.bbcf.conversion.daemon.Launcher;


public class ChromosomeNameHandler {

	public static final Logger logger = Launcher.initLogger(ChromosomeNameHandler.class.getName());



	private Map<String,String> altsNames;

	public ChromosomeNameHandler(){
		this.altsNames = new HashMap<String, String>();
	}




	public String getChromosomeAltName(String assemblyId, String chr) {
		if(altsNames.containsKey(chr)){
			return altsNames.get(chr);
		} else {
			String newChr = getAlternativeNames(assemblyId, chr);
			altsNames.put(chr,newChr);
			if(null==newChr){
				logger.warn("chromosome "+ chr+" not processed (no equivalence is found on genrep).");
			}
			return newChr;
		}
	}







	public static String getAlternativeNames(String assemblyId, String chr) {
		Connection conn = getConnection(assemblyId);
		if(null!=conn){
			try {
				PreparedStatement prep = conn.prepareStatement("select 1 from chromosome_names where used = ? limit 1;");
				prep.setString(1, chr);
				ResultSet r = prep.executeQuery();
				if(r.next()){
					r.close();
					return chr;
				} else {
					prep = conn.prepareStatement("select used from chromosome_names where alt = ? limit 1;");
					prep.setString(1, chr);
					ResultSet n = prep.executeQuery();
					if(n.next()){
						String result = n.getString(1);
						n.close();
						return result;
					}
				}
				conn.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return null;
	}


	private static Connection getConnection(String assemblyId) {
		if(assemblyId.equalsIgnoreCase("67")){
			return getConnectionOnMouseAltsChromosomesNames();
		}
		return null;
	}


//	public static String getUsedName(String chr,String assemblyId){
//		if(assemblyId.equalsIgnoreCase("67")){
//			return getMouseChromosomeAltName(chr);
//		}
//		return chr;
//	}





	private static String getMouseChromosomeAltName(String chr){
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

	private static Connection getConnectionOnMouseAltsChromosomesNames(){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite://"+Configuration.getDatabasesLink()+"/mouse_chr.db");
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









}

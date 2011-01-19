package ch.epfl.bbcf.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conf.Configuration;
import ch.epfl.bbcf.daemon.Launcher;

public class ChromosomeNameHandler {

	public static final Logger logger = Launcher.initLogger(ChromosomeNameHandler.class.getName());



	private Map<String,String> altsNames;



	private String species;

	public ChromosomeNameHandler(String assemblyId){
		this.altsNames = new HashMap<String, String>();
		this.species = assemblyId;
	}



	/**
	 * return the alternative name chr can have
	 * @param assemblyId
	 * @param chr
	 * @return
	 */
	public String getChromosomeAltName(String chr) {
		if(altsNames.containsKey(chr)){
			return altsNames.get(chr);
		} else {
			String newChr = getAlternativeNames(chr);
			altsNames.put(chr,newChr);
			if(null==newChr){
				logger.warn("chromosome "+ chr+" not processed (no equivalence is found on genrep).");
			}
			return newChr;
		}
	}







	public String getAlternativeNames(String chr) {
		if(species.equalsIgnoreCase("105")){
			return "chr";
		} else {
			Connection conn = getConnection(species);
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
		}
		return null;
	}


	private Connection getConnection(String assemblyId) {
		if(assemblyId.equalsIgnoreCase("70")||
				assemblyId.equalsIgnoreCase("76")){
			return getConnectionOnMouseAltsChromosomesNames();
		} else if(assemblyId.equalsIgnoreCase("75")||
				assemblyId.equalsIgnoreCase("98")){
			return getConnectionOnYeastAltsChromosomesNames();
		} else if(assemblyId.equalsIgnoreCase("15")) {
			return getConnectionOnCaulobacterCrescentusAltsChromosomesNames();
		}
		return null;
	}


	//	public static String getUsedName(String chr,String assemblyId){
	//		if(assemblyId.equalsIgnoreCase("67")){
	//			return getMouseChromosomeAltName(chr);//SQLiteAccess.getMouseChromosomeAltName(chr);
	//		}
	//		return chr;
	//	}





	private Connection getConnectionOnCaulobacterCrescentusAltsChromosomesNames() {
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite://"+Configuration.getDatabasesLink()+"/CaulobacterCrescentus_chr.db");
			return conn;
		} catch (InstantiationException e) {
			System.out.println(e);
		} catch (IllegalAccessException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		} catch (SQLException e) {
			System.out.println(e);
		}
		return null;
	}



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
			System.out.println(e);
		}
		return chr;
	}

	private static Connection getConnectionOnMouseAltsChromosomesNames(){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite://"+Configuration.getDatabasesLink()+"/mouse_chr.db");
			return conn;
		} catch (InstantiationException e) {
			System.out.println(e);
		} catch (IllegalAccessException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		} catch (SQLException e) {
			System.out.println(e);
		}
		return null;
	}
	private static Connection getConnectionOnYeastAltsChromosomesNames(){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite://"+Configuration.getDatabasesLink()+"/yeast_chr.db");
			return conn;
		} catch (InstantiationException e) {
			System.out.println(e);
		} catch (IllegalAccessException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		} catch (SQLException e) {
			System.out.println(e);
		}
		return null;
	}








}

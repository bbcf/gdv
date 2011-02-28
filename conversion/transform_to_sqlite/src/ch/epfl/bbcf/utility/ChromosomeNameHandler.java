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



	/**
	 * get the chromosome alternative name
	 * Process
	 * @param assemblyId - the assembly id from Genrep
	 * @param chr - the chromosome
	 * @return
	 */
	public String getChromosomeAltName(String assemblyId, String chr) {
		if(altsNames.containsKey(chr)){
			return altsNames.get(chr);
		} else {
			String newChr = getAlternativeNames(assemblyId, chr);
			altsNames.put(chr,newChr);
			if(null==newChr){
				logger.warn("chromosome "+ chr+" not processed (no equivalence is found on genrep)  for nr_assembly id = "+assemblyId+".");
			}
			return newChr;
		}
	}






	/**
	 * get the chromosome alternative name
	 * SQL query
	 * @param assemblyId - the assembly id from Genrep
	 * @param chr - the chromosome
	 * @return
	 */
	private static String getAlternativeNames(String assemblyId, String chr) {
		//logger.debug("get alt name for :"+chr);
		Connection conn = getConnection(assemblyId);
		if(null!=conn){
			try {
				PreparedStatement prep = conn.prepareStatement("select 1 from chromosome_names where used = ? limit 1;");
				prep.setString(1, chr);
				ResultSet r = prep.executeQuery();
				if(r.next()){
					r.close();
					//logger.debug("\t: "+chr);
					return chr;
				} else {
					prep = conn.prepareStatement("select used from chromosome_names where alt = ? limit 1;");
					prep.setString(1, chr);
					ResultSet n = prep.executeQuery();
					if(n.next()){
						String result = n.getString(1);
						n.close();
						//logger.debug("\t: "+result);
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

	/**
	 * retrieve the right connection
	 * depending on the assembly id
	 * @param assemblyId
	 * @return
	 */
	private static Connection getConnection(String assemblyId) {
		if(assemblyId.equalsIgnoreCase("70")){
			return getConnectionToDatabase("mouse_chr.db");
		} else if(assemblyId.equalsIgnoreCase("105")){
			return getConnectionToDatabase("CaulobacterCrescentus_chr.db");
		} else if(assemblyId.equalsIgnoreCase("75")||assemblyId.equalsIgnoreCase("98")){
			return getConnectionToDatabase("yeast_chr.db");
		}
		return getConnectionToDatabase("default.db");
	}


	/**
	 * retrieve the connection to the database specified
	 * @param database - the database
	 * @return - a Connection you have to close after use
	 */
	private static Connection getConnectionToDatabase(String database){
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite://"+Configuration.getDatabasesLink()+"/"+database);
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

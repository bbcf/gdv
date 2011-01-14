package ch.epfl.bbcf.formats.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import ch.epfl.bbcf.connection.GenRepAccess;
import ch.epfl.bbcf.connection.JbrowsoRAccess;
import ch.epfl.bbcf.connection.RemoteAccess;
import ch.epfl.bbcf.daemon.Launcher;
import ch.epfl.bbcf.utility.ConnectionStore;

abstract class SQLiteHandler {

	protected final static int LIMIT_BATCH_SIZE = 100000;

	protected static Logger logger = Launcher.initLogger(SQLiteHandler.class.getName());
	protected Connection connection;
	protected String dbName;

	protected boolean exist;

	protected ConnectionStore store;

	protected SQLiteHandler(String dbName){
		store = new ConnectionStore();
		this.exist = SQLiteAccess.dbAlreadyCreated(dbName);
		this.connection = SQLiteAccess.getConnectionOnFileDirectory(dbName);
		this.dbName = dbName;
		try {
			this.connection.setAutoCommit(false);
		} catch (SQLException e) {
			logger.error("cannot set autocommit to false "+e);
		}
	}

	public boolean exist(){
		return exist;
	}


	public abstract void createNewDatabase();


	public boolean finalizeDatabase(String assemblyId){
		try {
			List<String> chrNames = getChromosomesNames();
			if(!chrNames.isEmpty()){
				Statement stat = this.connection.createStatement();
				stat.executeUpdate("create table chrNames (name text, length integer);");
				GenRepAccess ga = new GenRepAccess(assemblyId);
				for(String chr : chrNames){
					int length = 0;
					if(chr.equalsIgnoreCase("IImicron")){
						length = ga.getChrLength("2micron");
					} else {
						length = ga.getChrLength(chr);
					}
					if(length==0){
						return false;
					}
					if(chr.equalsIgnoreCase("IImicron")){
						chr = "2micron";
					}
					PreparedStatement prep = this.connection.prepareStatement("insert into chrNames values (?,?);");
					prep.setString(1, chr);
					prep.setInt(2,length);
					prep.executeUpdate();
				}
				this.connection.commit();
				this.connection.close();
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return true;
	}

	protected int getMaxLengthForChromosome(String chr) {
		try{

			Statement stat = this.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT max(end) FROM "+chr+";";
			ResultSet rs = stat.executeQuery(query);
			if (rs.next()) {
				int f = rs.getInt(1);
				rs.close();
				return f;
			}
		} catch (SQLException e) {
			logger.error("getMaxLengthForChromosome : "+e);
		}
		return -1;
	}

	protected List<String> getChromosomesNames() {
		List<String> chrNames = new ArrayList<String>();
		try {
			Statement stat = this.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT name FROM sqlite_master where type='table' and name!='attributes' and name!='chrNames';";
			ResultSet rs = stat.executeQuery(query);
			while (rs.next()) {
				String chr = rs.getString(1);
				chrNames.add(chr);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("getChromosomesNames "+e);
		}
		return chrNames;
	}

	public void close() {
		try {
			this.connection.commit();
			this.connection.close();
		} catch (SQLException e) {
			logger.error(e);
		}
	}


	public abstract void newChromosome(String chr);
	public abstract void writeValues(String chr,int start,int end,float score,String name,int strand,Map<String,List<String>> attributes);


}

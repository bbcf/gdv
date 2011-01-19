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

import ch.epfl.bbcf.daemon.Launcher;
import ch.epfl.bbcf.utility.ConnectionStore;

public class SQLiteQuantitativeHandler extends SQLiteHandler{


	public SQLiteQuantitativeHandler(String dbName){
		super(dbName);
	}
	public void createNewDatabase(){
		Statement stat;
		try {
			stat = this.connection.createStatement();
			stat.executeUpdate("drop table if exists attributes;");
			stat.executeUpdate("create table attributes (key text, value text);");
			PreparedStatement prep = this.connection.prepareStatement("insert into attributes values (?,?);");
			prep.setString(1, "datatype");
			prep.setString(2,"quantitative");
			prep.executeUpdate();
		} catch (SQLException e) {
			logger.error(e);
		}
	}

		public void newChromosome(String chr){
			try {
				PreparedStatement stat = this.connection.prepareStatement("create table if not exists \""+chr+"\" (start integer,end integer,score real);");
				//stat.executeUpdate("create table if not exists "+chr+" (start,end,score);");
				stat.execute();
				this.connection.commit();
				PreparedStatement prep = this.connection.prepareStatement("insert into \""+chr+"\" values (?,?,?);");
				store.addChromosome(chr, prep);

			} catch (SQLException e) {
				logger.error("cannot prepare statement "+e);
			}
		}

		@Override
		public void writeValues(String chr,int start,int end,float score,String name, int strand, Map<String,List<String>> attributes){
			try {
				PreparedStatement prep = store.getPreparedStatement(chr);
				prep.setInt(1,start);
				prep.setInt(2,end);
				prep.setFloat(3,score);
				prep.execute();
				int nbQueries = store.getNbQueries(chr);
				nbQueries++;
				if(nbQueries>LIMIT_BATCH_SIZE){
					this.connection.commit();
					nbQueries=0;
				}
				store.setNbQueries(chr, nbQueries);
			} catch (SQLException e) {
				logger.error("cannot write values : "+e);
			}
		}
	}

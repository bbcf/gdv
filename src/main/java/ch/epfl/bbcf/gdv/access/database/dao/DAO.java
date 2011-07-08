package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Logs;

/**
 * Basic abstract class for the data access object
 * @author Yohan Jarosz
 *
 * @param <T> the POJO to set (Plain Old Java Object)
 */
public abstract class DAO<T> {

	private Connection connection = null;
	protected static final Logger logger = Logs.initLogger("sql.log", DAO.class);
	/**
	 * Constructeur
	 * @param conn
	 */
	public DAO(){
		this.connection = Conn.get();
	}

	protected void executer(PreparedStatement statement) throws SQLException {
		logger.debug(statement.toString());
		statement.execute();
	}
	protected ResultSet executeQuery(PreparedStatement statement) throws SQLException{
		logger.debug(statement.toString());
		ResultSet r = statement.executeQuery();
		return r;
		
	}

	protected boolean databaseConnected(){
		return connection!=null;
	}

	protected void startQuery(){
		try {
			this.connection.setAutoCommit(false);
		} catch (SQLException e) {
			Application.error(e);
		}
	}

	protected void endQuery(boolean wellFinished){
		try {
			if(wellFinished){
				this.connection.commit();
			} else {
				this.connection.rollback();
			}
		} catch (SQLException e) {
			Application.error(e);
		} 
	}

	public void release(){
		Conn.returnToPool(connection);
	}
	protected void finalize(){
		try {
			this.connection.close();
		} catch (SQLException e) {
			Application.error(e);
		}
	}

	protected PreparedStatement prepareStatement(String query,int resultSetType,int resultSetConcurrency) throws SQLException{
		return this.connection.prepareStatement(query, resultSetType, resultSetConcurrency);
	}
	protected void executeUpdate(PreparedStatement statement) throws SQLException {
		logger.debug(statement.toString());
		statement.execute();
	}
}

package ch.epfl.bbcf.gdv.access.gdv_prod.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;

/**
 * Basic abstract class for the data access object
 * @author Yohan Jarosz
 *
 * @param <T> the POJO to set (Plain Old Java Object)
 */
public abstract class DAO<T> {

	private Connect connection = null;
	protected static final Logger logger = Logs.initSQLLogger();
	/**
	 * Constructeur
	 * @param conn
	 */
	public DAO(Connect connection){
		this.connection = connection;
	}

	protected void executer(PreparedStatement statement) throws SQLException {
		logger.debug(statement.toString());
		this.connection.execute(statement);
	}
	protected ResultSet executeQuery(PreparedStatement statement) throws SQLException{
		logger.debug(statement.toString());
		return this.connection.executeQuery(statement);
	}

	protected boolean databaseConnected(){
		return Connect.isConnected();
	}

	protected void startQuery(){
		this.connection.startTransaction();
	}

	protected void endQuery(boolean wellFinished){
		this.connection.endTransaction(wellFinished);
	}

	protected void finalize(){
		try {
			//this.connection.finalize();
		} catch (Throwable e) {
			Application.error(e, connection.getSession().getUserId());
		}
	}

	protected PreparedStatement prepareStatement(String query,int resultSetType,int resultSetConcurrency) throws SQLException{
		return this.connection.prepareStatement(query, resultSetType, resultSetConcurrency);
	}
	protected void executeUpdate(PreparedStatement statement) throws SQLException {
		logger.debug(statement.toString());
		this.connection.executeUpdate(statement);
	}
}

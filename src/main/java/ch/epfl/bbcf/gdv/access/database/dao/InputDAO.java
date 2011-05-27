package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.pojo.Input;

public class InputDAO extends DAO<Input>{


	public InputDAO(Connect connection) {
		super(connection);
	}

	/**
	 * Create a new input in the database
	 * @param md5 - the md5sum of the input
	 * @return
	 */
	public int createNewInput(String md5) {
		int inputId = -1;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into input values (default , " +
				" ?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, md5);
				this.executeUpdate(statement);
				query = "select currval('input_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                              
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					inputId = resultSet.getInt(1);
				}
				this.endQuery(true);
				return inputId;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return inputId;

	}
	
	/**
	 * link an input to an user
	 * @param userId
	 * @param inputId
	 * @return
	 */
	public boolean linkToUser(int userId, int inputId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into userToInput values (" +
				" ?, ? ,? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, userId);
				statement.setInt(2,inputId);
				statement.setDate(3, new Date(new java.util.Date().getTime()));
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return false;

	}
	/**
	 * remove the link between an user input and an user
	 * @param userId
	 * @param filename
	 */
	public boolean removeConnection(int userId, String filename) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from usertoinput as t1 " +
				"where t1.user_id = ? and t1.input_id in " +
				"(select id from input as t2 " +
				"where t2.file = ? );";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, userId);
				statement.setString(2,filename);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return false;

	}

	public boolean linkToSequence(int inputId, String assemblyId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into sequencetouserinput values (" +
				" ?, ? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, Integer.valueOf(assemblyId));
				statement.setInt(2,inputId);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return false;
	}

	public boolean delete(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from input where id = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return false;
	}

	public int getIdFromAnnotationId(int annotationId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.input_id  from annotationtouserinput as t1 " +
				"where t1.annotation_id = ? limit 1; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,annotationId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					int inputId = resultSet.getInt(1);
					this.endQuery(true);
					return inputId;
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return -1;
	}

	public String getFileDirectory(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select file from input " +
				"where id = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					String filePath = resultSet.getString(1);
					this.endQuery(true);
					return filePath;
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return null;
	}

	public Input getUserInputByFileName(String database) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from input as t1 " +
				"where t1.md5 = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,database);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					return getUserInput(resultSet);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("getTrackByUserInput : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}

	private Input getUserInput(ResultSet resultSet) {
		Input ui = new Input();
		if(this.databaseConnected()){
			try {
				ui.setId(resultSet.getInt("id"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				ui.setMd5(resultSet.getString("md5"));
			} catch (SQLException e) {
				logger.error(e);
			}
			return ui;
		}
		return null;
	}

	public java.util.Date getDateFromTrackId(int trackid,int userId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select the_date from userToInput as t1 " +
				"inner join inputtotrack as t2 on t1.input_id = t2.input_id " +
				"where t1.user_id  = ? and t2.track_id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,userId);
				statement.setInt(2, trackid);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					return resultSet.getDate(1);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("getDateFromTrackId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}

	/**
	 * look if this md5 is in the database
	 * @param md5
	 * @return
	 */
	public boolean exist(String md5) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select 1 from input where md5 = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,md5);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					return true;
				}
				this.endQuery(true);
				return false;
			} catch (SQLException e) {
				logger.error("exist : "+e);
				this.endQuery(false);
			}
		}
		return false;
	}
	/**
	 * remove an input from the db
	 * @param input - the name of the input (generally the md5)
	 */
	public void remove(String input) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from input where md5 = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,input);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("remove : "+e);
				this.endQuery(false);
			}
		}
	}
}



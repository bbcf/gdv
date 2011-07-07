package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;


public class StatusDAO  extends DAO<Status>{

	public StatusDAO() {
		super();
	}


	private Status getStatus(ResultSet r){
		Status status = new Status();
		if(this.databaseConnected()){
			try {
				status.setId(r.getInt(1));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				status.setStatus(r.getString(2));
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return status;
	}

	public Status getStatus(int id){
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from statuses as t1 " +
				"where t1.id = ? ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					return getStatus(resultSet);
				}
			} catch (SQLException e) {
				logger.error("getStatus : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}
}

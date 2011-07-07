package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.pojo.GFeatMinerJob;

public class GFeatMinerJobDAO extends DAO<GFeatMinerJob>{

	public GFeatMinerJobDAO() {
		super();
	}

	
	
	private GFeatMinerJob getJob(ResultSet r){
		GFeatMinerJob job = new GFeatMinerJob();
		try {
			job.setId(r.getInt(1));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			job.setProjectId(r.getInt(2));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			job.setStatus(r.getInt(3));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			job.setResult(r.getString(4));
		} catch (SQLException e) {
			logger.error(e);
		}
		return job;
	}
	
	/**
	 * get a job from it's id
	 * @param id - the job id
	 * @return
	 */
	public GFeatMinerJob getJob(int id){
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from gfeatminerjob as t1 " +
				"where t1.id = ? ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					return getJob(resultSet);
				}
			} catch (SQLException e) {
				logger.error("getInput : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Create a new job in the database
	 * @param projectId - the project identifier
	 * @param status - the status : must be part of ch.epfl.bbcf.gdv.access.database.pojo.Status
	 * @param result - the result as a JSON
	 * @return the job id just created
	 */
	public int createNewJob(int projectId,int status,String result) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into  gfeatminerjob values (default,?,?,?); ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				statement.setInt(2,status);
				statement.setString(3, result);
				this.executeUpdate(statement);
				query = "select currval('gfeatminerjob_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                               
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					int jobId = resultSet.getInt(1);
					this.endQuery(true);
					return jobId;
				}
			} catch (SQLException e) {
				logger.error("createNewJob "+e);
				this.endQuery(false);
			}
		}
		return -1;
	}



	public void updateJob(int jobId, int status, String result) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update gfeatminerjob set status = ? , result = ? " +
						"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,status);
				statement.setString(2,result);
				statement.setInt(3, jobId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("updateJob : "+e);
				this.endQuery(false);
			}
		}
	}


	/**
	 * get the status of a job
	 * @param jobId - the job id
	 * @return
	 */
	public int getStatus(int jobId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select status from gfeatminerjob " +
						"where id = ? limit 1;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, jobId);
				ResultSet r = this.executeQuery(statement);
				int status = -1;
				if(r.first()){
					status = r.getInt(1);
				}
				this.endQuery(true);
				return status;
			} catch (SQLException e) {
				logger.error("updateJob : "+e);
				this.endQuery(false);
			}
		}
		return -1;
	}
	
	
	
	
	
}

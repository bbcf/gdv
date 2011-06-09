package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.pojo.Job;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;

public class JobDAO extends DAO<Job>{

	public JobDAO(Connect connection) {
		super(connection);
	}


	private Job getJob(ResultSet r){
		Job job = new Job();
		if(this.databaseConnected()){
			try {
				job.setId(r.getInt("id"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				job.setStatus(r.getInt("status"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				job.setProjectId(r.getInt("project_id"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				job.setData(r.getString("data"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				job.setType(r.getString("type"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				job.setOutput(r.getString("output"));
			} catch (SQLException e) {
				logger.error(e);
			}
			return job;
		}
		return null;
	}

	private List<Job> getJobs(ResultSet r){
		List<Job> jobs = new ArrayList<Job>();
		try {
			while(r.next()){
				jobs.add(getJob(r));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return jobs;
	}


	/**
	 * create a new job
	 * @param projectId - the project identifier the job references to
	 * @return the job id
	 */
	public int createJob(int projectId,Job.JOB_TYPE type,Job.JOB_OUTPUT output){
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into jobs values (default,?,?,?,?::job_type,?::job_output); ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,Status.RUNNING);
				statement.setInt(2,projectId);
				statement.setString(3,"");
				statement.setString(4,type.name());
				statement.setString(5,output.name());
				this.executeUpdate(statement);
				query = "select currval('jobs_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                               
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					int jobId = resultSet.getInt(1);
					this.endQuery(true);
					return jobId;
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return -1;
	}

	/**
	 * get the status of a job
	 * @param id - the id of the job
	 * @return the status
	 */
	public int getJobStatus(int id){
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select status from jobs values where id = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					int status = resultSet.getInt(1);
					this.endQuery(true);
					return status;
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return -1;
	}

	/**
	 * get a job
	 * @param id - the id of the job
	 * @return the job
	 */
	public Job getJob(int id){
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from jobs where id = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					Job job = getJob(resultSet);
					this.endQuery(true);
					return job;
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}

	/**
	 * update a job
	 * @param jobId the id
	 * @param status the status
	 * @param data the data if one
	 * @return
	 */
	public boolean updateJob(int jobId, int status, String data) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update jobs set status = ? , data = ? where id = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,status);
				if(null==data){
					statement.setString(2,"");
				} else {
					statement.setString(2,data);
				}
				statement.setInt(3, jobId);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return false;
	}


	public List<Job> getGFeatMinerJobsAndNotTerminatedFromProjectId(
			int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from jobs where project_id = ? and type = ?::job_type " +
				"union select * from jobs where project_id = ? and type!= ?::job_type and status = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				statement.setString(2,Job.JOB_TYPE.gfeatminer.toString());
				statement.setInt(3,projectId);
				statement.setString(4,Job.JOB_TYPE.gfeatminer.toString());
				statement.setInt(5,Status.RUNNING);
				ResultSet resultSet = this.executeQuery(statement);
				List<Job> jobs = getJobs(resultSet);
				this.endQuery(true);
				return jobs;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}


	public boolean removeJob(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from jobs where id = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return false;
	}
}
package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.bbcf.bbcfutils.parsing.SQLiteExtension;
import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;


public class TrackDAO extends DAO<Track>{

	private static String[] fields = {
		"id","job_id","name","paramaters","status","type"
	};

	public TrackDAO(Connect connection) {
		super(connection);
	}


	private Track getTrack(ResultSet resultSet) {
		Track track = new Track();
		if(this.databaseConnected()){
			try {
				track.setId(resultSet.getInt(fields[0]));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				track.setJob_id(resultSet.getInt(fields[1]));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				track.setName(resultSet.getString(fields[2]));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				track.setParameters(resultSet.getString(fields[3]));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				track.setStatus(resultSet.getString(fields[4]));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				String t = resultSet.getString(fields[5]);
				if(t.equalsIgnoreCase(SQLiteExtension.QUALITATIVE.toString())){
					track.setType(SQLiteExtension.QUALITATIVE);
				} else if(t.equalsIgnoreCase(SQLiteExtension.QUALITATIVE_EXTENDED.toString())){
					track.setType(SQLiteExtension.QUALITATIVE_EXTENDED);
				} else if(t.equalsIgnoreCase(SQLiteExtension.QUANTITATIVE.toString())){
					track.setType(SQLiteExtension.QUANTITATIVE);
				}
				
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		track.setInput(getInput(track.getId()));
		return track;
	}

	/**
	 * get the md5 of a track
	 * @param id
	 * @return
	 */
	private String getInput(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select md5 from input as t1 " +
						"inner join inputtotrack as t2 on t1.id = t2.input_id " +
						"where t2.track_id = ? ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					return resultSet.getString(1);
				}
			} catch (SQLException e) {
				logger.error("getInput : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}

	private List<Track> getTracks(ResultSet resultSet) {
		List<Track> jbTracks = new ArrayList<Track>();
		try {
			while (resultSet.next()) {
				jbTracks.add(getTrack(resultSet));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return jbTracks;
	}

	

	/**
	 * create a new track in GDV database
	 */
	public int createTmpTrack(String status,int job_id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into tracks values (" +
				"default, ? ,? , ? , ? ,?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,job_id);
				statement.setString(2,"in process");
				statement.setString(3, "params");
				statement.setString(4, status);
				statement.setString(5,TrackControl.NOT_DETEMINED);
				this.executeUpdate(statement);
				query = "select currval('tracks_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                              
				ResultSet resultSet = this.executeQuery(statement);
				int trackId = -1;
				if(resultSet.first()){
					trackId = resultSet.getInt(1);
				}
				this.endQuery(true);
				return trackId;
			} catch (SQLException e) {
				logger.error("createNewTrack : "+e);
				Application.debug("serror"+e);
			}
		}
		return -1;
	}
	
	/**
	 * create a new track in gdv database
	 * @param assemblyId
	 * @param name
	 * @param filetype
	 * @param always
	 * @param status
	 */
	public int createNewTrack(int job_id,String assemblyId, String name, String filetype,
			boolean always, String status) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into tracks values (" +
				"default, ? , ? , ? , ? ,?, ? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,job_id);
				statement.setString(2, name);
				statement.setString(3, "params");
				statement.setString(4, filetype);
				statement.setBoolean(5,always);
				statement.setString(6,status);
				this.executeUpdate(statement);
				query = "select currval('tracks_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                              
				ResultSet resultSet = this.executeQuery(statement);
				int trackId = -1;
				if(resultSet.first()){
					trackId = resultSet.getInt(1);
				}
				this.endQuery(true);
				return trackId;
			} catch (SQLException e) {
				logger.error("createNewTrack : "+e);
				Application.debug("serror"+e);
			}
		}
		return -1;
	}
	/**
	 * link the track to admin
	 * @param trackId
	 */
	public void linkToAdmin(int trackId,String sequence_id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into admintrack values (" +
				"?,? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,trackId);
				statement.setInt(2, Integer.parseInt(sequence_id));
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("linkToAdmin : "+e);
				this.endQuery(false);
			}
		}
		
	}
	
	
	/**
	 * link a track to an user
	 * @param userid
	 * @param trackId
	 */
	public boolean linkToUseer(int userid, int trackId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into userToTrack values (" +
				"? , ?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,userid);
				statement.setInt(2,trackId);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error("linkToUser : "+e);
				this.endQuery(false);
			}
		}
		return false;
	}
	/**
	 * remove the connection between the track and the user
	 * @param userId
	 * @param trackId
	 */
	public void removeConnection(int userId, int trackId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from userToTrack " +
						"where user_id = ? and track_id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,userId);
				statement.setInt(2,trackId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("removeConnection : "+e);
				this.endQuery(false);
			}
		}
	}
	/**
	 * delete a track from database
	 * @param trackId
	 */
	public void deleteTrack(int trackId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from tracks " +
						"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,trackId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("deleteTrack : "+e);
				this.endQuery(false);
			}
		}
	}
	/**
	 * update the status of a track in the database
	 * (When the calculated SQLite has finished its job,
	 * it call this method )
	 * @param trackId
	 * @param status
	 */
	public void updateTrack(int trackId, String status) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update tracks set status = ? where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,status);
				statement.setInt(2,trackId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("updateTrack : "+e);
				this.endQuery(false);
			}
		}
	}
	/**
	 * get the POJO Track from it's id
	 * @param trackId
	 * @return
	 */
	public Track getTrackById(int trackId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from tracks where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,trackId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					Track track = getTrack(resultSet);
					this.endQuery(true);
					return track;
				}
			} catch (SQLException e) {
				logger.error("getTrackById : "+e);
				this.endQuery(false);
			}
			this.endQuery(true);
			logger.debug("Rturn null");
		}
		return null;
	}
	


	public Track getTrackIdWithJobId(int jobId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from tracks where job_id = ?;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,jobId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					Track track = getTrack(resultSet);
					this.endQuery(true);
					return track;
				}
			} catch (SQLException e) {
				logger.error("getTrackIdWithJobId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}

	/**
	 * link a track to a project
	 * @param trackId
	 * @param viewId
	 */
	public boolean linkToProject(int trackId, int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into projectToTrack values (" +
				"? , ?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				statement.setInt(2,trackId);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error("linkToProject : "+e);
				this.endQuery(false);
			}
		}
		return false;
	}

	/**
	 * link a track to an input
	 * @param trackId
	 * @param intputId
	 */
	public void linkToInput(int trackId, int intputId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into inputToTrack values (" +
				"? , ?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,intputId);
				statement.setInt(2,trackId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("link to input : "+e);
				this.endQuery(false);
			}
		}
	}

	public Track getTrackWithInputName(String database) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select distinct t1.* from tracks as t1 " +
				"inner join filetotrack as t2 on t1.id = t2.track_id " +
				"where t2.file_name = ? limit 1;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,database);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					Track track = getTrack(resultSet);
					this.endQuery(true);
					return track;
				}
			} catch (SQLException e) {
				logger.error("getTrackIdWithInputName : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}
	

	public List<Track> getTracksFromUserId(int userId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select distinct t1.* from tracks as t1 " +
				"inner join usertotrack as t2 on t1.id = t2.track_id " +
				"where t2.user_id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,userId);
				ResultSet resultSet = this.executeQuery(statement);
				List<Track> tracks = getTracks(resultSet);
				this.endQuery(true);
				return tracks;
			} catch (SQLException e) {
				logger.error("getTracksFromUserId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}





	public List<Track> getTracksFromProjectId(int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select distinct t1.* from tracks as t1 " +
				"inner join projectToTrack as t2 on t1.id = t2.track_id " +
				"where t2.project_id = ?;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				ResultSet resultSet = this.executeQuery(statement);
				List<Track> tracks = getTracks(resultSet);
				this.endQuery(true);
				return tracks;
			} catch (SQLException e) {
				logger.error("getTracksFromProjectId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}

	public String getFileFromTrackId(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.md5 from input as t1 " +
				"inner join inputToTrack as t2 on t1.id = t2.input_id " +
				"where t2.track_id = ? limit 1;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					String result = resultSet.getString(1);
					this.endQuery(true);
					return result;
				}
			} catch (SQLException e) {
				logger.error("getFileFromTrackId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}

	public void setParams(int id, String params) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update tracks set paramaters = ? " +
				"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(2,id);
				statement.setString(1,params);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("setParams : "+e);
				this.endQuery(false);
			}
		}
	}

	/**
	 * update the fields of a tracks
	 * @param trackId
	 * @param name
	 * @param filetype
	 * @param status
	 */
	public void updateTrackFields(int trackId,String name,
			SQLiteExtension filetype, String status) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update tracks set name = ? , type = ? , status = ? " +
				"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,name);
				statement.setString(2,filetype.toString());
				statement.setString(3,status);
				statement.setInt(4, trackId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("updateTrackFields : "+e);
				this.endQuery(false);
			}
		}
	}
	
	
	
	
	/**
	 * get an admin track for this sequence id
	 * @param sequenceId
	 * @return
	 */
	public Set<Track> getAdminTracksFromSequenceId(int sequenceId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from tracks as t1 " +
				"inner join admin_tracks as t2 on t1.id = t2.track_id " +
				"where t2.sequence_id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,sequenceId);
				ResultSet resultSet = this.executeQuery(statement);
				Set<Track> tracks = getSetTracks(resultSet);
				this.endQuery(true);
				return tracks;
			} catch (SQLException e) {
				logger.error("getAdminTracksFromSequenceId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}

	public Set<Track> getAllAdminTracks() {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from tracks as t1 " +
				"inner join admin_tracks as t2 on t1.id = t2.track_id ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = this.executeQuery(statement);
				Set<Track> tracks = getSetTracks(resultSet);
				this.endQuery(true);
				return tracks;
			} catch (SQLException e) {
				logger.error("getAdminTracksFromSequenceId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}
	
	/**
	 * create a track visible for all users
	 * @param speciesId
	 * @param trackId
	 * @return
	 */
	public boolean createAdminTrack(int speciesId, int trackId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into admin_tracks values (" +
				"? , ?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,trackId);
				statement.setInt(2,speciesId);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error("createAdminTrack : "+e);
				this.endQuery(false);
			}
		}
		return false;
	}

	public boolean renameTrack(int id, String input) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update tracks set name = ? " +
						"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,input);
				statement.setInt(2,id);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error("renameTrack : "+e);
				this.endQuery(false);
			}
		}
		return false;
	}

	public boolean resetParams(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update tracks set paramaters = ? " +
						"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,"params");
				statement.setInt(2,id);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error("renameTrack : "+e);
				this.endQuery(false);
			}
		}
		return false;
	}

	/**
	 * get the tracks from a project id
	 * @param projectId
	 * @return
	 */
	public Set<Track> getCompletedTracksFromProjectId(int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select distinct t1.* from tracks as t1 " +
				"inner join projectToTrack as t2 on t1.id = t2.track_id " +
				"where t2.project_id = ? and t1.status = ? and t1.name!= ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				statement.setString(2,"completed");
				statement.setString(3,"in process");
				ResultSet resultSet = this.executeQuery(statement);
				Set<Track> tracks = getSetTracks(resultSet);
				this.endQuery(true);
				return tracks;
			} catch (SQLException e) {
				logger.error("getTracksFromProjectId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}
	
	/**
	 * get the tracks from a project id, filter by name
	 * @param projectId
	 * @param tracks names
	 * @return
	 */
	public Set<Track> getCompletedTracksFromProjectIdAndTrackNames(
			int projectId, List<String> names) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select distinct t1.* from tracks as t1 " +
				"inner join projectToTrack as t2 on t1.id = t2.track_id " +
				"where t2.project_id = ? and t1.status = ? and t1.name!= ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				statement.setString(2,"completed");
				statement.setString(3,"in process");
				ResultSet resultSet = this.executeQuery(statement);
				Set<Track> tracks = getSetTracks(resultSet,names);
				this.endQuery(true);
				return tracks;
			} catch (SQLException e) {
				logger.error("getTracksFromProjectId : "+e);
				this.endQuery(false);
			}
		}
		return null;
	}

	private Set<Track> getSetTracks(ResultSet resultSet, List<String> names) {
		Set<Track> jbTracks = new HashSet<Track>();
		try {
			while (resultSet.next()) {
				Track t = getTrack(resultSet);
				if(names.contains(t.getName())){
					jbTracks.add(t);
				}
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return jbTracks;
	}
	private Set<Track> getSetTracks(ResultSet resultSet) {
		Set<Track> jbTracks = new HashSet<Track>();
		try {
			while (resultSet.next()) {
				jbTracks.add(getTrack(resultSet));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return jbTracks;
	}



	

	



	


	
}

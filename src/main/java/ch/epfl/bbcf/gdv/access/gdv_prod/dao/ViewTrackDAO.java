//package ch.epfl.bbcf.gdv.access.gdv_prod.dao;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
//import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Track;
//
//public class ViewTrackDAO extends DAO{
//
//	public ViewTrackDAO(Connect connection) {
//		super(connection);
//	}
//
//	public void createViewTrack(int jbId, String name, String params, boolean sendMail) {
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "insert into viewTracks values (? , " +
//				" ? , ? , ? ) ; ";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1, jbId);
//				statement.setString(2, name);
//				statement.setString(3, params);
//				int status = -1;
//				if(sendMail){
//					status = -2;
//				}
//				statement.setInt(4,status);
//				this.executeUpdate(statement);
//				this.endQuery(true);
//			} catch (SQLException e) {
//				logger.error(e);
//			}
//		}
//	}
//
//	public void updateTrack(int trackId, int status) {
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "UPDATE viewtracks set status = ? where id = ?  ; ";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1,status);
//				statement.setInt(2,trackId);
//				this.executeUpdate(statement);
//				this.endQuery(true);
//			} catch (SQLException e) {
//				logger.error(e);
//			}
//		}
//		this.endQuery(false);
//
//	}
//	public Track getTrackById(int trackId) {
//		Track t = new Track();
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "select * from viewtracks where id = ? limit 1 ;";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1, trackId);
//				ResultSet resultSet = this.executeQuery(statement);
//				if(resultSet.next()){
//					t = getTrack(resultSet);
//				}
//				this.endQuery(true);
//				return t;
//			} catch (SQLException e) {
//				logger.error(e);
//				this.endQuery(false);
//			}
//		}
//		return null;
//	}
//	public boolean linkToAnnotation(int annotationId, int viewTrackId) {
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "insert into viewtracktoannotation values (" +
//				" ?, ? ) ; ";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1, annotationId);
//				statement.setInt(2,viewTrackId);
//				this.executeUpdate(statement);
//				this.endQuery(true);
//				return true;
//			} catch (SQLException e) {
//				logger.error(e);
//			}
//		}
//		this.endQuery(false);
//		return false;
//
//	}
//
//	private Track getTrack(ResultSet resultSet) {
//		Track track = new Track();
//		if(this.databaseConnected()){
//			try {
//				track.setName(resultSet.getString("name"));
//			} catch (SQLException e) {
//				logger.error(e);
//			}
//			try {
//				track.setParameters(resultSet.getString("parameters"));
//			} catch (SQLException e) {
//				logger.error(e);
//			}
//			try {
//				track.setStatus(resultSet.getInt("status"));
//			} catch (SQLException e) {
//				logger.error(e);
//			}
//			try {
//				track.setId(resultSet.getInt("id"));
//			} catch (SQLException e) {
//				logger.error(e);
//			}
//		}
//		return track;
//	}
//
//	private List<Track> getTracks(ResultSet resultSet) {
//		List<Track> jbTracks = new ArrayList<Track>();
//		try {
//			while (resultSet.next()) {
//				jbTracks.add(getTrack(resultSet));
//			}
//		} catch (SQLException e) {
//			logger.error(e);
//		}
//		return jbTracks;
//	}
//	public List<Track> getTracksFromAnnotationId(int annotationId) {
//		List<Track> annots = new ArrayList<Track>();
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "select t1.* from viewtracks as t1 " +
//				"inner join viewtracktoannotation as t2 on t1.id = t2.viewtrack_id " +
//				"where t2.annotation_id = ? ;";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1, annotationId);
//				ResultSet resultSet = this.executeQuery(statement);
//				annots = getTracks(resultSet);
//				this.endQuery(true);
//			} catch (SQLException e) {
//				logger.error(e);
//				this.endQuery(false);
//			}
//		}
//		return annots;
//	}
//
//	public boolean isCreated(Track t) {
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "select 1 from viewtracks where id = ? and status = 3 ;";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1, t.getId());
//				ResultSet resultSet = this.executeQuery(statement);
//				if (resultSet.first()) {
//					this.endQuery(true);
//					return true;
//				}
//			} catch (SQLException e) {
//				logger.error(e);
//				this.endQuery(false);
//			}
//		}
//		return false;
//	}
//
//
//	public void deleteTrack(int trackId) {
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "delete from tracks where id = ? ;";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1, trackId);
//				this.executeUpdate(statement);
//			} catch (SQLException e) {
//				logger.error(e);
//				this.endQuery(false);
//			}
//		}
//	}
//
//
//	public List<Track> getTracksFromInternId(int defaultViewId) {
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "select t1.* from viewtracks as t1 " +
//				"inner join internViewToTracks as t2 on t1.id = t2.viewtrack_id " +
//				"where t2.internView_id = ? ;";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1, defaultViewId);
//				ResultSet resultSet = this.executeQuery(statement);
//				List<Track> annots = getTracks(resultSet);
//				this.endQuery(true);
//				return annots;
//			} catch (SQLException e) {
//				logger.error(e);
//				this.endQuery(false);
//			}
//		}
//		return null;
//	}
//
//	public List<Track> getDefaultTracks(int sequenceId) {
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "select t1.* from viewtracks as t1 " +
//				"inner join  viewTrackToAnnotation as t2 on t1.id = t2.viewtrack_id " +
//				"inner join auto_annotations as t3 on t2.annotation_id = t3.annotation_id " +
//				"where t3.seq_id = ? ;";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setInt(1, sequenceId);
//				ResultSet resultSet = this.executeQuery(statement);
//				List<Track> annots = getTracks(resultSet);
//				this.endQuery(true);
//				return annots;
//			} catch (SQLException e) {
//				logger.error(e);
//				this.endQuery(false);
//			}
//		}
//		return null;
//	}
//
//
//}

package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Session;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.ProjectDAO;
import ch.epfl.bbcf.gdv.access.database.dao.TrackDAO;
import ch.epfl.bbcf.gdv.access.database.dao.InputDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;

public class TrackControl extends Control{

	public static final String STATUS_FINISHED = "completed";
	public static final String STATUS_PARSING ="parsing";
	public static final String STATUS_PROCESSING ="processing";
	public static final String STATUS_UPLOADING ="uploading";
	public static final String STATUS_DECOMPRESS ="decompressing";
	public static final String STATUS_ERROR ="error";
	public static final String STATUS_SHA ="getting sha1 ";
	public static final String STATUS_FILETYPE ="getting filetype";
	public static final String STATUS_EXTENSION ="getting extensions";
	public static final String NOT_DETEMINED = "ND";
	public TrackControl(UserSession session) {
		super(session);
	}



	/**
	 * delete the connections of the track (and user input)
	 * with the user
	 * @param track id
	 */
	public void removeTrackFromUser(Track track) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		tdao.removeConnection(session.getUserId(),track.getId());
		String filename = tdao.getFileFromTrackId(track.getId());
		InputDAO udao = new InputDAO(Connect.getConnection(session));
		udao.removeConnection(session.getUserId(),filename);
		ProjectDAO vdao = new ProjectDAO(Connect.getConnection(session));
		vdao.removeConnection(session.getUserId(),track.getId());

	}
	/**
	 * remove a track from the database
	 * @param trackId
	 */
	public static void deleteTrack(int trackId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		tdao.deleteTrack(trackId);

	}

	/**
	 * create an admin track ~ createTrack but link to admin instead of user
	 * @param id
	 * @param assemblyId
	 * @param name
	 * @param string
	 * @param b
	 * @param i
	 * @return
	 */
	public static int createAdminTrack(int jobId,int userid, String assemblyId, String name,
			String filetype, boolean always, String status) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		int trackId = tdao.createNewTrack(jobId,assemblyId,name,filetype,always,status);
		if(trackId!=-1){
			tdao.linkToAdmin(trackId,assemblyId);
			return trackId;
		}
		return -1;
	}

	/** 
	 * get the if of the trackName by this
	 * md5 in the database
	 * @param md5
	 * @return
	 */
	public static Track getTrackWithInputName(String md5) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		Track t = tdao.getTrackWithInputName(md5);
		if(t!=null){
			return t;
		}
		return null;
	}
	public static int getTrackIdWithName(String md5) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		Track t = tdao.getTrackByUserInput(md5);
		if(t!=null){
			return t.getId();
		}
		return -1;
	}

	/**
	 * get the track with the specified job id
	 * @param jobId the job id
	 * @return the Track
	 */
	public static Track getTrackIdWithJobId(int jobId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		return tdao.getTrackIdWithJobId(jobId);
	}
	
	/**
	 * Update the status of a track in the database
	 * check if a project exist and add to it or create it
	 * @param trackId
	 * @param status
	 */
	public static void updateTrack(int trackId, String status) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		tdao.updateTrack(trackId,status);
	}
	/**
	 * Update the percentage finished of a track
	 * @param trackId
	 * @param status
	 */
	public static void updatePercentage(int trackId, int status) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		Track t = tdao.getTrackById(trackId);
		try {
			String strStatus = t.getStatus();
			if(strStatus.endsWith("%")){
				int stat = Integer.parseInt(strStatus.substring(0,strStatus.length()-2));
				int newStatus = stat+status;
				String newStat = newStatus+" %";
				tdao.updateTrack(trackId,newStat);
			} else {
				tdao.updateTrack(trackId, status+" %");
			}

		} catch(NumberFormatException e){
			tdao.updateTrack(trackId, status+" %");
		}



	}


	/**
	 * link the track to the input
	 * @param trackId
	 * @param inputId
	 */
	public static void linkToInput(int trackId, int inputId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		tdao.linkToInput(trackId,inputId);
	}




	/**
	 * get the tracks belonging to the user
	 * @return
	 */
	public List<Track> getTracksFromUser() {
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		return tdao.getTracksFromUserId(session.getUserId());
	}






	/**
	 * get the file belonging to a track
	 * @param id
	 * @return
	 */
	public static String getFileFromTrackId(int id) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		return tdao.getFileFromTrackId(id);
	}




	/**
	 * update the parameters of a track
	 * @param id
	 * @param params
	 */
	public void setParams(int id, String params) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		tdao.setParams(id,params);
	}




	/**
	 * get the track represented by this id
	 * @param trackId
	 * @return
	 */
	public static Track getTrackById(int trackId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		return tdao.getTrackById(trackId);
	}




	/**
	 * get the date when the user upload the user input
	 * @param id
	 * @return
	 */
	public Date getDate(int trackid) {
		InputDAO udao = new InputDAO(Connect.getConnection(session));
		return udao.getDateFromTrackId(trackid,session.getUserId());
	}




	/**
	 * get the admin track belonging to a sequence
	 * @param sequenceId
	 * @return
	 */
	public Set<Track> getAdminTracksFromSpeciesId(int sequenceId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		return tdao.getAdminTracksFromSequenceId(sequenceId);
	}




	/**
	 * link a track to a project
	 * @param trackId
	 * @param projectId
	 */
	public static boolean linkToProject(int trackId, int projectId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		return tdao.linkToProject(trackId, projectId);

	}









	/**
	 * Update the fields of a track
	 * @param trackId
	 * @param name
	 * @param filetype
	 * @param status
	 */
	public static void updateTrackFields(int trackId,
			String name, String filetype, String status) {
		Application.debug("UPDATE TRACK FIELD : "+trackId+" "+name+" "+filetype+" "+status);
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		tdao.updateTrackFields(trackId,name,filetype,status);
	}





	public List<Track> getTracksFromProjectId(int projectId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		return tdao.getTracksFromProjectId(projectId);
	}




	/**
	 * create a temporary track in GDV database
	 * @param status
	 * @return the track ID
	 */
	public static int createTmpTrack(int job_id,String status) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		return tdao.createTmpTrack(status,job_id);
	}




	/**
	 * create a track visible by all users
	 * @param sequenceId
	 * @param trackId
	 * @return
	 */
	public static boolean createAdminTrack(int sequenceId, int trackId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		return tdao.createAdminTrack(sequenceId,trackId);

	}





	public boolean renameTrack(int id, String input) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		tdao.resetParams(id);
		return tdao.renameTrack(id,input);
	}





	public Set<Track> getAllAdminTracks() {
		TrackDAO dao = new TrackDAO(Connect.getConnection(session));
		return dao.getAllAdminTracks();
	}




	/**
	 * Remove the admon track from the database and also the 
	 * flat files on the filesystem
	 * @param trackInstance
	 */
	public void removeAdminTrack(Track track) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		tdao.deleteTrack(track.getId());
		InputDAO idao = new InputDAO(Connect.getConnection(session));
		idao.remove(track.getInput());
		FileManagement.deleteDirectory(new File(Configuration.getFilesDir()+"/"+track.getInput()));
		FileManagement.deleteDirectory(new File(Configuration.getTracks_dir()+"/"+track.getInput()));
	}



	/**
	 * get the tracks from project id
	 * @param projectId
	 * @return
	 */
	public Set<Track> getCompletedTracksFromProjectId(int projectId) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		return tdao.getCompletedTracksFromProjectId(projectId);
	}


	public Set<Track> getCompletedTracksFromProjectIdAndTrackNames(int projectId,
			String[] tracksNames) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		return tdao.getCompletedTracksFromProjectIdAndTrackNames(projectId,Arrays.asList(tracksNames));
	}




	/**
	 * get the admin track for this assembly
	 * it assume that there is only one
	 * @param nr_assembly_id
	 */
	public static Track getAdminTrackByNrAssemblyID(int nr_assembly_id) {
		TrackDAO tdao = new TrackDAO(Connect.getConnection());
		Set<Track> tracks=  tdao.getAdminTracksFromSequenceId(nr_assembly_id);
		Track track = tracks.iterator().next();
		return track;
	}








}

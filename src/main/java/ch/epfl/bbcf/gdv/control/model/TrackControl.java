package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;


import ch.epfl.bbcf.bbcfutils.parsing.SQLiteExtension;
import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.ProjectDAO;
import ch.epfl.bbcf.gdv.access.database.dao.TrackDAO;
import ch.epfl.bbcf.gdv.access.database.dao.InputDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
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



	/**
	 * delete the connections of the track (and user input)
	 * with the user
	 * @param track id
	 */
	public static void removeTrackFromUser(Track track,int userId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		tdao.removeConnection(userId,track.getId());
		String filename = tdao.getFileFromTrackId(track.getId());
		InputDAO udao = new InputDAO(Conn.get());
		udao.removeConnection(userId,filename);
		ProjectDAO vdao = new ProjectDAO(Conn.get());
		vdao.removeConnection(userId,track.getId());

	}
	/**
	 * remove a track from the database
	 * @param trackId
	 */
	public static void deleteTrack(int trackId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
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
		TrackDAO tdao = new TrackDAO(Conn.get());
		int trackId = tdao.createNewTrack(jobId,assemblyId,name,filetype,always,status);
		if(trackId!=-1){
			tdao.linkToAdmin(trackId,assemblyId);
			return trackId;
		}
		return -1;
	}

	
	/**
	 * get the track with the specified job id
	 * @param jobId the job id
	 * @return the Track
	 */
	public static Track getTrackIdWithJobId(int jobId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.getTrackIdWithJobId(jobId);
	}
	
	/**
	 * Update the status of a track in the database
	 * check if a project exist and add to it or create it
	 * @param trackId
	 * @param status
	 */
	public static void updateTrack(int trackId, String status) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		tdao.updateTrack(trackId,status);
	}
	/**
	 * Update the percentage finished of a track
	 * @param trackId
	 * @param status
	 */
	public static void updatePercentage(int trackId, int status) {
		TrackDAO tdao = new TrackDAO(Conn.get());
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
		TrackDAO tdao = new TrackDAO(Conn.get());
		tdao.linkToInput(trackId,inputId);
	}




	/**
	 * get the tracks belonging to the user
	 * @return
	 */
	public static List<Track> getTracksFromUser(int userId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.getTracksFromUserId(userId);
	}






	/**
	 * get the file belonging to a track
	 * @param id
	 * @return
	 */
	public static String getFileFromTrackId(int id) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.getFileFromTrackId(id);
	}




	/**
	 * update the parameters of a track
	 * @param id
	 * @param params
	 */
	public static void setParams(int id, String params) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		tdao.setParams(id,params);
	}




	/**
	 * get the track represented by this id
	 * @param trackId
	 * @return
	 */
	public static Track getTrackById(int trackId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.getTrackById(trackId);
	}




	/**
	 * get the date when the user upload the user input
	 * @param id
	 * @return
	 */
	public static Date getDate(int trackid,int userId) {
		InputDAO udao = new InputDAO(Conn.get());
		return udao.getDateFromTrackId(trackid,userId);
	}




	/**
	 * get the admin track belonging to a sequence
	 * @param sequenceId
	 * @return
	 */
	public static Set<Track> getAdminTracksFromSpeciesId(int sequenceId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.getAdminTracksFromSequenceId(sequenceId);
	}




	/**
	 * link a track to a project
	 * @param trackId
	 * @param projectId
	 */
	public static boolean linkToProject(int trackId, int projectId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
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
			String name, SQLiteExtension filetype, String status) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		tdao.updateTrackFields(trackId,name,filetype,status);
	}





	public static List<Track> getTracksFromProjectId(int projectId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.getTracksFromProjectId(projectId);
	}




	/**
	 * create a temporary track in GDV database
	 * @param status
	 * @return the track ID
	 */
	public static int createTmpTrack(int job_id,String status) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.createTmpTrack(status,job_id);
	}




	/**
	 * create a track visible by all users
	 * @param sequenceId
	 * @param trackId
	 * @return
	 */
	public static boolean createAdminTrack(int sequenceId, int trackId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.createAdminTrack(sequenceId,trackId);

	}





	public static boolean renameTrack(int id, String input) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		tdao.resetParams(id);
		return tdao.renameTrack(id,input);
	}





	public static Set<Track> getAllAdminTracks() {
		TrackDAO dao = new TrackDAO(Conn.get());
		return dao.getAllAdminTracks();
	}




	/**
	 * Remove the admon track from the database and also the 
	 * flat files on the filesystem
	 * @param trackInstance
	 */
	public static void removeAdminTrack(Track track) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		tdao.deleteTrack(track.getId());
		InputDAO idao = new InputDAO(Conn.get());
		idao.remove(track.getInput());
		FileManagement.deleteDirectory(new File(Configuration.getFilesDir()+"/"+track.getInput()));
		FileManagement.deleteDirectory(new File(Configuration.getTracks_dir()+"/"+track.getInput()));
	}



	/**
	 * get the tracks from project id
	 * @param projectId
	 * @return
	 */
	public static Set<Track> getCompletedTracksFromProjectId(int projectId) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.getCompletedTracksFromProjectId(projectId);
	}


	public static Set<Track> getCompletedTracksFromProjectIdAndTrackNames(int projectId,
			String[] tracksNames) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		return tdao.getCompletedTracksFromProjectIdAndTrackNames(projectId,Arrays.asList(tracksNames));
	}




	/**
	 * get the admin track for this assembly
	 * it assume that there is only one
	 * @param nr_assembly_id
	 */
	public static Track getAdminTrackByNrAssemblyID(int nr_assembly_id) {
		TrackDAO tdao = new TrackDAO(Conn.get());
		Set<Track> tracks=  tdao.getAdminTracksFromSequenceId(nr_assembly_id);
		Track track = tracks.iterator().next();
		return track;
	}

	
	
	
	
	
	public static String buildTrackParams(Track track,String color){
		if(null==color){
			color = "red";
		}
		
		String params = "{\n";
		String directory = getFileFromTrackId(track.getId());

		String imageType = null;
		if(track.getType().toString().equalsIgnoreCase(SQLiteExtension.QUANTITATIVE.toString())){
			imageType="ImageTrack";
			params+="\"color\":\""+color+"\",";
		} else if(track.getType().toString().equalsIgnoreCase(SQLiteExtension.QUALITATIVE.toString())||
				track.getType().toString().equalsIgnoreCase(SQLiteExtension.QUALITATIVE_EXTENDED.toString())){
			imageType="FeatureTrack";
		} else {
			Application.error("datatype not recognized : "+track.getId());
		}
		
		params+="\"url\" : \"../"+directory+"/{refseq}.json\",\n" +
		"\"label\" : \""+protect(track.getName())+"\",\n"+
		"\"type\" : \""+imageType+"\",\n"+
		"\"key\" : \""+protect(track.getName())+"\"\n}";
		setParams(track.getId(),params);
		return params;
	}

	/**
	 * get a Track form the name of the file 
	 * @param dbName - the name of the file
	 * @param jobId - the jobId 
	 */
	public static void getTrackWithDBName(String dbName, int jobId) {
		// TODO Auto-generated method stub
		
	}



	/**
	 * protect char " with a backslash
	 * if there is one in the name
	 * @param name
	 * @return
	 */
	private static String protect(String name) {
		return name.replaceAll("\"", "\\\\\"");
	}




}

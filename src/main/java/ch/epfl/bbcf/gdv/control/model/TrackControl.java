package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


import ch.epfl.bbcf.bbcfutils.parsing.SQLiteExtension;
import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.ProjectDAO;
import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO;
import ch.epfl.bbcf.gdv.access.database.dao.TrackDAO;
import ch.epfl.bbcf.gdv.access.database.dao.InputDAO;
import ch.epfl.bbcf.gdv.access.database.dao.TypeDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Style;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Type;
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
		TrackDAO tdao = new TrackDAO();
		tdao.removeConnection(userId,track.getId());
		tdao.release();
		String filename = tdao.getFileFromTrackId(track.getId());
		InputDAO udao = new InputDAO();
		udao.removeConnection(userId,filename);
		udao.release();
		ProjectDAO vdao = new ProjectDAO();
		vdao.removeConnection(userId,track.getId());
		vdao.release();
	}

	/**
	 * remove a track from the database
	 * @param trackId
	 */
	public static void deleteTrack(int trackId) {
		TrackDAO tdao = new TrackDAO();
		tdao.deleteTrack(trackId);
		tdao.release();
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
		TrackDAO tdao = new TrackDAO();
		int trackId = tdao.createNewTrack(jobId,assemblyId,name,filetype,always,status);
		if(trackId!=-1){
			tdao.linkToAdmin(trackId,assemblyId);
			tdao.release();
			return trackId;
		}
		tdao.release();
		return -1;
	}


	/**
	 * get the track with the specified job id
	 * @param jobId the job id
	 * @return the Track
	 */
	public static Track getTrackIdWithJobId(int jobId) {
		TrackDAO tdao = new TrackDAO();
		Track t = tdao.getTrackIdWithJobId(jobId);
		tdao.release();
		return t;
	}

	/**
	 * Update the status of a track in the database
	 * check if a project exist and add to it or create it
	 * @param trackId
	 * @param status
	 */
	public static void updateTrack(int trackId, String status) {
		TrackDAO tdao = new TrackDAO();
		tdao.updateTrack(trackId,status);
		tdao.release();
	}
	/**
	 * Update the percentage finished of a track
	 * @param trackId
	 * @param status
	 */
	public static void updatePercentage(int trackId, int status) {
		TrackDAO tdao = new TrackDAO();
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
		} finally {
			tdao.release();
		}



	}


	/**
	 * link the track to the input
	 * @param trackId
	 * @param inputId
	 */
	public static void linkToInput(int trackId, int inputId) {
		TrackDAO tdao = new TrackDAO();
		tdao.linkToInput(trackId,inputId);
		tdao.release();
	}




	/**
	 * get the tracks belonging to the user
	 * @return
	 */
	public static List<Track> getTracksFromUser(int userId) {
		TrackDAO tdao = new TrackDAO();
		List<Track> ts = tdao.getTracksFromUserId(userId);
		tdao.release();
		return ts;
	}






	/**
	 * get the file belonging to a track
	 * @param id
	 * @return
	 */
	public static String getFileFromTrackId(int id) {
		TrackDAO tdao = new TrackDAO();
		String s = tdao.getFileFromTrackId(id);
		tdao.release();
		return s;
	}




	/**
	 * update the parameters of a track
	 * @param id
	 * @param params
	 */
	public static void setParams(int id, String params) {
		TrackDAO tdao = new TrackDAO();
		tdao.setParams(id,params);
		tdao.release();
	}




	/**
	 * get the track represented by this id
	 * @param trackId
	 * @return
	 */
	public static Track getTrackById(int trackId) {
		TrackDAO tdao = new TrackDAO();
		Track t = tdao.getTrackById(trackId);
		tdao.release();
		return t;
	}




	/**
	 * get the date when the user upload the user input
	 * @param id
	 * @return
	 */
	public static Date getDate(int trackid,int userId) {
		InputDAO udao = new InputDAO();
		Date d =  udao.getDateFromTrackId(trackid,userId);
		udao.release();
		return d;
	}




	/**
	 * get the admin track belonging to a sequence
	 * @param sequenceId
	 * @return
	 */
	public static Set<Track> getAdminTracksFromSpeciesId(int sequenceId) {
		TrackDAO tdao = new TrackDAO();
		Set<Track> ts = tdao.getAdminTracksFromSequenceId(sequenceId);
		tdao.release();
		return ts;
	}




	/**
	 * link a track to a project
	 * @param trackId
	 * @param projectId
	 */
	public static boolean linkToProject(int trackId, int projectId) {
		TrackDAO tdao = new TrackDAO();
		boolean b = tdao.linkToProject(trackId, projectId);
		tdao.release();
		return b;

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
		TrackDAO tdao = new TrackDAO();
		tdao.updateTrackFields(trackId,name,filetype,status);
		tdao.release();
	}





	public static List<Track> getTracksFromProjectId(int projectId) {
		TrackDAO tdao = new TrackDAO();
		List<Track> ts = tdao.getTracksFromProjectId(projectId);
		tdao.release();
		return ts;
	}




	/**
	 * create a temporary track in GDV database
	 * @param status
	 * @return the track ID
	 */
	public static int createTmpTrack(int job_id,String status) {
		TrackDAO tdao = new TrackDAO();
		int i = tdao.createTmpTrack(status,job_id);
		tdao.release();
		return i;
	}




	/**
	 * create a track visible by all users
	 * @param sequenceId
	 * @param trackId
	 * @return
	 */
	public static boolean createAdminTrack(int sequenceId, int trackId) {
		TrackDAO tdao = new TrackDAO();
		boolean b = tdao.createAdminTrack(sequenceId,trackId);
		tdao.release();
		return b;

	}





	public static boolean renameTrack(int id, String input) {
		TrackDAO tdao = new TrackDAO();
		tdao.resetParams(id);
		boolean b = tdao.renameTrack(id,input);
		tdao.release();
		return b;
	}





	public static Set<Track> getAllAdminTracks() {
		TrackDAO dao = new TrackDAO();
		Set<Track> tracks = dao.getAllAdminTracks();
		dao.release();
		return tracks;
	}




	/**
	 * Remove the admon track from the database and also the 
	 * flat files on the filesystem
	 * @param trackInstance
	 */
	public static void removeAdminTrack(Track track) {
		TrackDAO tdao = new TrackDAO();
		tdao.deleteTrack(track.getId());
		tdao.release();
		InputDAO idao = new InputDAO();
		idao.remove(track.getInput());
		idao.release();
		FileManagement.deleteDirectory(new File(Configuration.getFilesDir()+"/"+track.getInput()));
		FileManagement.deleteDirectory(new File(Configuration.getTracks_dir()+"/"+track.getInput()));
	}



	/**
	 * get the tracks from project id
	 * @param projectId
	 * @return
	 */
	public static Set<Track> getCompletedTracksFromProjectId(int projectId) {
		TrackDAO tdao = new TrackDAO();
		Set<Track> ts = tdao.getCompletedTracksFromProjectId(projectId);
		tdao.release();
		return ts;
	}


	public static Set<Track> getCompletedTracksFromProjectIdAndTrackNames(int projectId,
			String[] tracksNames) {
		TrackDAO tdao = new TrackDAO();
		Set<Track> ts = tdao.getCompletedTracksFromProjectIdAndTrackNames(projectId,Arrays.asList(tracksNames));
		tdao.release();
		return ts;
	}




	/**
	 * get the admin track for this assembly
	 * it assume that there is only one
	 * @param nr_assembly_id
	 */
	public static Track getAdminTrackByNrAssemblyID(int nr_assembly_id) {
		TrackDAO tdao = new TrackDAO();
		Set<Track> tracks=  tdao.getAdminTracksFromSequenceId(nr_assembly_id);
		tdao.release();
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
	 * protect char " with a backslash
	 * if there is one in the name
	 * @param name
	 * @return
	 */
	private static String protect(String name) {
		return name.replaceAll("\"", "\\\\\"");
	}






	public static boolean setTrackTypes(int trackId,List<Type> types){
		TypeDAO tdao = new TypeDAO();
		boolean b = tdao.setTrackTypes(trackId,types);
		tdao.release();
		return b;
	}

	/**
	 * add the style types to the track if it's have any
	 * @param track
	 */
	public static void addTypes(Track track) {
		if(track.getType().equals(SQLiteExtension.QUALITATIVE_EXTENDED)){
			try {
				SQLiteAccess access = SQLiteAccess.getConnectionWithDatabase(
						Configuration.getFilesDir()+File.separator+track.getInput());
				List<String> types_ = access.getTypes();
				access.close();
				List<Type> types = new ArrayList<Type>();
				if(null!=types_){
					for(String t : types_){
						Type type = StyleControl.createType(t);
						types.add(type);
					}
					setTrackTypes(track.getId(),types);
				} else {
					Application.warn("the track "+track.getId()+" : "+track.getInput()+" has no type ");
				}

			} catch (InstantiationException e) {
				Application.error(e);
			} catch (IllegalAccessException e) {
				Application.error(e);
			} catch (ClassNotFoundException e) {
				Application.error(e);
			} catch (SQLException e) {
				Application.error(e);
			}
		}

	}

	public static List<Type> getTrackTypes(int trackId){
		TypeDAO tdao = new TypeDAO();
		List<Type> ts = tdao.getTrackTypes(trackId);
		tdao.release();
		return ts;
	}

	public static Set<Type> getTracksTypes(Set<Track> tracks) {
		TypeDAO tdao = new TypeDAO();
		Set<Type> types = new HashSet<Type>();
		for(Track t : tracks){
			List<Type> tmp = tdao.getTrackTypes(t.getId());
			if(null!=tmp){
				types.addAll(tmp);
			}
		}
		tdao.release();
		return types;
	}





}

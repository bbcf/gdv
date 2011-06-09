package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.json.JSONException;
import org.json.JSONObject;


import ch.epfl.bbcf.bbcfutils.access.InternetConnection;
import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.JobDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Job;
import ch.epfl.bbcf.gdv.access.database.pojo.Job.JOB_OUTPUT;
import ch.epfl.bbcf.gdv.access.database.pojo.Job.JOB_TYPE;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.command.Command;
import ch.epfl.bbcf.gdv.exception.TrackCreationFailedException;
import ch.epfl.bbcf.gdv.mail.Sender;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;

public class JobControl extends Control{

	public JobControl(UserSession session) {
		super(session);
	}

	/**
	 * create a job
	 * @param projectId - the identifier of the project
	 * @return the job id
	 */
	public static int createJob(int projectId,Job.JOB_TYPE type,Job.JOB_OUTPUT output){
		JobDAO jdao = new JobDAO(Connect.getConnection());
		return jdao.createJob(projectId, type, output);
	}

	/**
	 * get the status of a job
	 * @param jobId - the job id
	 * @return the status of the job
	 */
	public static int getJobStatus(int jobId){
		JobDAO jdao = new JobDAO(Connect.getConnection());
		return jdao.getJobStatus(jobId);
	}

	/**
	 * get  a job
	 * @param jobId - the job id
	 * @return the job
	 */
	public static Job getJob(int jobId){
		JobDAO jdao = new JobDAO(Connect.getConnection());
		return jdao.getJob(jobId);
	}

	/**
	 * update a job with current status
	 * @param jobId the job id
	 * @param status the status
	 * @param data the data if one
	 * @return
	 */
	private static boolean updateJob(int jobId,Command.STATUS status,String data){
		int stat=0;
		switch(status){
		case error : stat = Status.ERROR;
		break;
		case running : stat = Status.RUNNING;
		break;
		case success : stat = Status.SUCCES;
		}
		JobDAO jdao = new JobDAO(Connect.getConnection());
		return jdao.updateJob(jobId, stat,data);
	}


	/**
	 * update a track & the job
	 * @param jobId the job id
	 * @param status the status
	 * @param data the data, message
	 */
	public static void updateTrackJob(int jobId, Command.STATUS status, String data) {
		Track track = TrackControl.getTrackIdWithJobId(jobId);
		TrackControl.updateTrack(track.getId(),data);
		updateJob(jobId, status, data);
	}

	/**
	 * update the job & delete the track & input,
	 * also on directory
	 * @param jobId the job id
	 * @param status the status
	 * @param data the data, message
	 */
	public static void updateTrackJobError(int jobId, String data) {
		Track track = TrackControl.getTrackIdWithJobId(jobId);
		TrackControl.updateTrack(track.getId(),data);
		updateJob(jobId, Command.STATUS.error, data);
		InputControl.removeInput(track.getInput());
		FileManagement.deleteDirectory(
				new File(
						Configuration.getFilesDir()+"/"+track.getInput()));

	}

	/**
	 * update track & job to success
	 * @param jobId
	 */
	public static void updateTrackJobSuccess(int jobId) {
		Application.debug("updateTrackJobSuccess "+jobId);
		Track track = TrackControl.getTrackIdWithJobId(jobId);
		updateJob(jobId, Command.STATUS.success,null);
		TrackControl.updateTrack(track.getId(),"completed");
	}



	/**
	 * create a new job for gFeatMiner
	 * & create a directory consistant with the job
	 * id created
	 * @param projectId - the project id
	 * @param output - the JOB_OUTPUT
	 * @return the job id
	 */
	public static int newGfeatMinerJob(int projectId,JOB_OUTPUT output){
		int jobId = createJob(projectId,JOB_TYPE.gfeatminer,output);
		File file = new File(Configuration.getgFeatMinerDirectory()+"/"+jobId);
		file.mkdir();
		return jobId;

	}




	/**
	 * create a new selection from the web interface
	 * (transform a selection to a track)
	 * @param selections - the selections
	 * @param projectId - the project id
	 * @param nr_assembly_id - the nr_assembly id
	 * @param selectionName - the name to give to the selection
	 * @return the job identifier
	 * @throws JSONException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static int newSelection(String selections, int projectId,int nr_assembly_id,String selectionName) throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException{
		int jobId = createJob(projectId,JOB_TYPE.new_selection,JOB_OUTPUT.reload);
		SelectionControl.createNewSelection(jobId,selections,projectId,nr_assembly_id,selectionName);
		return jobId;
	}

	/**
	 * create a new track from user upload
	 * One of the parameters url,fileUpload or systemPath must be filled
	 * @param userId - the user id
	 * @param project - the project
	 * @param url - the url where to fetch the track (if one)
	 * @param fileUpload - the fileupload  (web interface)
	 * @param systemPath - the path to the file (if one)
	 * @return the job identifier
	 */
	public static int newUserTrack(int userId,Project project,URL url,FileUpload fileUpload,String systemPath){
		int jobId = createJob(project.getId(),JOB_TYPE.new_track,JOB_OUTPUT.reload);
		InputControl.processUserInput(jobId,userId, project, url, fileUpload, systemPath);
		return jobId;
	}

	/**
	 * create a new track from user upload
	 * One of the parameters url,fileUpload or systemPath must be filled
	 * @param userId - the user id
	 * @param projectId - the project id
	 * @param url - the url where to fetch the track (if one)
	 * @param fileUpload - the fileupload  (web interface)
	 * @param systemPath - the path to the file (if one)
	 * @param trackName - the name to give to the track
	 * @return the job identifier
	 */
	public static int newUserTrack(int userId,int projectId,URL url,FileUpload fileUpload,String systemPath,String trackName){
		Application.debug("new user track : "+userId+"  pid "+projectId+"   tn "+trackName);
		int jobId = createJob(projectId,JOB_TYPE.new_track,JOB_OUTPUT.reload);
		InputControl.processUserInput(jobId,userId, projectId, url, fileUpload, systemPath,trackName);
		return jobId;
	}

	/**
	 * process your input - WARNING : it will be an admin one, & will be visible by each user
	 * you can either provide an URL, a file upload or a system path
	 * @param url
	 * @param fileUpload
	 * @param systemPath
	 * @throws TrackCreationFailedException 
	 */
	public static boolean newAdminTrack(int sequenceId,URL url,FileUpload fileUpload,String systemPath,String name){
		Application.debug("new admin track : "+sequenceId+"  "+name);
		int jobId = createJob(-1,JOB_TYPE.new_track,JOB_OUTPUT.reload);
		InputControl.processAdminInput(jobId,sequenceId, url, fileUpload, systemPath, name);
		return true;
	}

	/**
	 * Get the jobs (termitated or not) from GFeatMiner & get the jobs not terminated
	 * @param projectId - the project id
	 * @return
	 */
	public static List<Job> getGFeatMinerJobsAndNotTerminatedFromProjectId(int projectId) {
		JobDAO dao = new JobDAO(Connect.getConnection());
		return dao.getGFeatMinerJobsAndNotTerminatedFromProjectId(projectId);
	}


	/**
	 * write the output of a job,
	 * depending on his status
	 * @param job the job
	 * @return a String in JSON format that will be parser
	 * by the javascript
	 */
	public static String outputJob(Job job){
		String output = null;
		int status = job.getStatus();
		if(status==Status.RUNNING){
			output = "{job_id:"+job.getId()+",status:\"running\"}";
		} else if(status==Status.ERROR){
			output = "{job_id:"+job.getId()+",status:\"error\",data:\""+job.getData()+"\"}";
		} else if(status==Status.SUCCES){
			output = "{job_id:"+job.getId()+",status:\"success\",output:\""+job.getOutput()+"\",data:\""+job.getData()+"\"}";
		}
		return output;
	}
	/**
	 * remove the job corresponding to the track
	 * @param id the job id
	 */
	public static void removeJob(int id) {
		JobDAO dao = new JobDAO(Connect.getConnection());
		dao.removeJob(id);

	}

	/**
	 * Build & send the request to GFM server
	 * @param jobId - the job id
	 * @param data a JSONObject representing the data from the form
	 * @throws IOException 
	 */
	public static void sendToGFeatMiner(int jobId, JSONObject data) throws IOException {
		String body="";
		body+="callback_url="+URLEncoder.encode(Configuration.getGdv_appli_proxy()+"/post","UTF-8");
		body+="&job_id="+URLEncoder.encode(Integer.toString(jobId),"UTF-8");
		body+="&output_location="+URLEncoder.encode(Configuration.getgFeatMinerDirectory()+"/"+jobId,"UTF-8");
		body+="&data="+URLEncoder.encode(data.toString(),"UTF-8");
		InternetConnection.sendPOSTConnection(
				getGFMUrl(), body, InternetConnection.MIME_TYPE_FORM_APPLICATION);

	}

	private static String getGFMUrl() {
		return Configuration.getGdvTomcatServ()+"/gfeatminer";
	}


}

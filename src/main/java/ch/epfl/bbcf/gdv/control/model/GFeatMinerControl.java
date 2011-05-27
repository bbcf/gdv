package ch.epfl.bbcf.gdv.control.model;

import java.io.File;

import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.GFeatMinerJobDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;

public class GFeatMinerControl extends Control{

	public GFeatMinerControl(UserSession session) {
		super(session);
	}

	
	/**
	 * handle the result from the GFM server
	 * @param jobId - the job identifier
	 * @param result - the result
	 */
	public static void handleResultFromGFeatMiner(int jobId, JSONObject result) {
		int status = Status.SUCCES;
		updateJob(jobId,status,result.toString());
		
	}

	/**
	 * create a new GfeatMiner job
	 * @return
	 */
	public static int createNewJob(int projectId) {
		GFeatMinerJobDAO dao = new GFeatMinerJobDAO(Connect.getConnection());
		int jobId = dao.createNewJob(projectId,Status.RUNNING,"{}");
		File file = new File(Configuration.getgFeatMinerDirectory()+"/"+jobId);
		file.mkdir();
		return jobId;
	}

	
	public static void updateJob(int jobId,int status,String result) {
		GFeatMinerJobDAO dao = new GFeatMinerJobDAO(Connect.getConnection());
		dao.updateJob(jobId,status,result);
	}

	/**
	 * get the status of a job
	 * @param jobId - the job id
	 * @return the status (ch.epfl.bbcf.gdv.access.database.pojo.Status)
	 */
	public static int checkJob(int jobId) {
		GFeatMinerJobDAO dao = new GFeatMinerJobDAO(Connect.getConnection());
		return dao.getStatus(jobId);
	}
}

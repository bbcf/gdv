package ch.epfl.bbcf.gdv.control.model;

import java.io.File;

import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.GFeatMinerJobDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;
import ch.epfl.bbcf.gdv.config.Configuration;

public class GFeatMinerControl extends Control{


	
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
		GFeatMinerJobDAO dao = new GFeatMinerJobDAO();
		int jobId = dao.createNewJob(projectId,Status.RUNNING,"{}");
		File file = new File(Configuration.getgFeatMinerDirectory()+"/"+jobId);
		file.mkdir();
		dao.release();
		return jobId;
	}

	
	public static void updateJob(int jobId,int status,String result) {
		GFeatMinerJobDAO dao = new GFeatMinerJobDAO();
		dao.updateJob(jobId,status,result);
		dao.release();
	}

	/**
	 * get the status of a job
	 * @param jobId - the job id
	 * @return the status (ch.epfl.bbcf.gdv.access.database.pojo.Status)
	 */
	public static int checkJob(int jobId) {
		GFeatMinerJobDAO dao = new GFeatMinerJobDAO();
		int status = dao.getStatus(jobId);
		dao.release();
		return status;
	}
}

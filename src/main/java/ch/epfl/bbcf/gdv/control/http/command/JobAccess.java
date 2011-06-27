package ch.epfl.bbcf.gdv.control.http.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.pojo.Job;
import ch.epfl.bbcf.gdv.access.database.pojo.Job.JOB_OUTPUT;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.JobControl;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;

public class JobAccess extends Command{




	public JobAccess(RequestParameters params, PrintWriter out) {
		super(params, out);
	}

	@Override
	public void doRequest() {
		log.debug("job access : doRequest");
		if(null==params.getAction()){
			throw new AbortWithHttpStatusException(400,true);
		}
		switch(params.getAction()){


		case gfeatminer:
			checkParams(params.getData());
			checkParams(params.getProjectId());
			try {
				JSONObject data = checkData(params.getData());
				int jobId = JobControl.newGfeatMinerJob(params.getProjectId(), JOB_OUTPUT.image);
				Job job = JobControl.getJob(jobId);
				out.write(JobControl.outputJobForWebInterface(job));
				out.close();
				JobControl.sendToGFeatMiner(jobId,data);
			} catch (JSONException e1) {
				log.error(e1);
				out.write("{failed :\""+e1+"\"}");
				new AbortWithHttpStatusException(500,true);
			} catch (IOException e) {
				log.error(e);
				out.write("{failed :\""+e+"\"}");
				new AbortWithHttpStatusException(500,true);
			} finally {
				out.close();
			}
			break;

		case gfeatresponse:
			checkParams(params.getData());
			checkParams(params.getProjectId(),params.getJobId());
			try {
				JSONObject data = new JSONObject(params.getData());
				Application.debug("data : "+data.toString());
				String type = data.getString("type");
				Application.debug("type : "+type);
				/* handle error */
				if(type.equalsIgnoreCase("error")){
					JobControl.updateJob(params.getJobId(),Command.STATUS.error, data.getString("msg"));
				} 
			} catch (JSONException e1) {
				/* handle reponse */
				Application.debug("update job success");
				JobControl.updateJob(params.getJobId(),Command.STATUS.success, params.getData());
			} finally {
				out.close();
			}

			break;
		case new_selection:
			checkParams(params.getSelections());
			checkParams(params.getNrAssemblyId(),params.getProjectId());
			int jobId;
			try {
				jobId = JobControl.newSelection(params.getSelections(), params.getProjectId(), params.getNrAssemblyId(),params.getData());
				Job job = JobControl.getJob(jobId);
				if(null!=job){
					Application.debug(JobControl.outputJobForWebInterface(job));
					out.write(JobControl.outputJobForWebInterface(job));
				} else {
					throw new AbortWithHttpStatusException(500,true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new AbortWithHttpStatusException(500,true);
			} finally {
				out.close();
			}
			break;


		case status :
			checkParams(params.getJobId());
			Job job = JobControl.getJob(params.getJobId());
			out.write(JobControl.outputJobForWebInterface(job));
			out.close();
			break;
		default:throw new AbortWithHttpStatusException(400,true);
		}
	}












	/**
	 * check if the data is correct
	 * Dummy at the moment
	 * @param data the data
	 * @return a new JSONObject well formated if the check succed
	 * @throws JSONException
	 */
	private JSONObject checkData(String data) throws JSONException{
		JSONObject json = new JSONObject(data);
		JSONArray filters = json.getJSONArray("filter");
		JSONArray ntracks = json.getJSONArray("ntracks");
		if(filters!=null){
			filters = addCompletePath(filters);
			json.put("filter", filters);
		}
		if(ntracks!=null){
			ntracks = addCompletePath(ntracks);
			json.put("ntracks", ntracks);
		}
		return json;
	}	



	/**
	 * Change the path of the database, cauz 
	 * we just have the database name from the
	 * browser interface
	 * @param array - the array containing the paths
	 * @return a new JSONArray
	 * @throws JSONException
	 */
	private JSONArray addCompletePath(JSONArray array) throws JSONException{
		JSONArray newArray = new JSONArray();
		for(int i=0;i<array.length();i++){
			JSONObject j = array.getJSONObject(i);
			String dbName=j.getString("path");
			j.put("path",Configuration.getFilesDir()+"/"+dbName);
			newArray.put(i,j);
		}
		return newArray;
	}









}

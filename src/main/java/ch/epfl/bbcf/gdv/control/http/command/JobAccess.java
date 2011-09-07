package ch.epfl.bbcf.gdv.control.http.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.pojo.Job;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.access.database.pojo.Job.JOB_OUTPUT;
import ch.epfl.bbcf.gdv.access.database.pojo.Job.JOB_TYPE;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.JobControl;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;

public class JobAccess extends Command{



	private static Logger log = Logs.initLogger("post.log", JobAccess.class);
	
	public JobAccess(RequestParameters params, PrintWriter out) {
		super(params, out);
	}

	@Override
	public void doRequest() {
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
			checkParams(params.getJobId());
			
			Project project = ProjectControl.getProjectByJobId(params.getJobId());
			
			log.debug("gfeat response : "+params.getData()+" on project "+project.getId());
			try {
				JSONObject data = new JSONObject(params.getData());
				String type = data.getString("type");
				/* handle error */
				if(type.equalsIgnoreCase("error")){
					log.error(data.getString("msg"));
					JobControl.updateJob(params.getJobId(),Command.STATUS.error, data.getString("msg"));
				} 
			} catch (JSONException e1) {
				/* handle reponse */
				if("new_track".equalsIgnoreCase(params.getDatatype())){
					log.debug("new track");
					try {
						/* create a new track */
						
						Users user = UserControl.getUserByProjectId(project.getId());
						JSONObject data;
						data = new JSONObject(params.getData());
						JSONArray files = data.getJSONArray("files");
						for(int i = 0; i<files.length();i++){
							JSONObject f = files.getJSONObject(i);
							log.debug(f);
							String path = f.getString("path");
							log.debug(path);
							int jobId = JobControl.createJob(project.getId(),JOB_TYPE.new_track,JOB_OUTPUT.reload);
							log.debug(jobId);
							InputControl.processUserInput(
									jobId,user.getId(),project.getId(),null,null,path,"gFeatMiner output "+jobId);
						}
						log.debug("removing job "+params.getJobId());
						JobControl.removeJob(params.getJobId());
					} catch (JSONException e) {
						log.error("ERROR : "+e1.getLocalizedMessage());
					}
					
					
					
				} else {
					log.debug("update job success");
					JobControl.updateJob(params.getJobId(),Command.STATUS.success, params.getData());
				}
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
		if(json.has("filter")){
			JSONArray filters = json.getJSONArray("filter");
			filters = addCompletePath(filters);
			json.put("filter", filters);
		}
		if(json.has("ntracks")){
			JSONArray ntracks = json.getJSONArray("ntracks");
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

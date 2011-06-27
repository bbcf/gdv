package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Job;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.JobControl;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.SequenceControl;
import ch.epfl.bbcf.gdv.control.model.SpeciesControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;

public class PostAccess extends Command{

	public PostAccess(RequestParameters params, PrintWriter out) {
		super(params, out);
	}

	public enum COMMAND {new_project,new_track,status,assemblies}

	@Override
	public void doRequest() {
		/* sign in the use wo whant to modify or add project(s) */
		checkParams(params.getMail(),params.getKey());
		if(null==params.getCommand()){
			throw new AbortWithHttpStatusException(400,true);
		}
		Users user = UserControl.getuserByMailAndPass(params.getMail(),params.getKey());
		if(null==user){
			failed("problem with your key and password - check if they are valid");
		}
		
		
		/* dispatch */
		
		switch(params.getCommand()){
		case new_project:
			createNewProject(user);
		break;
		case new_track:
			addTrack(user);
			break;
		case status :
			checkParams(params.getJobId());
			Job job = JobControl.getJob(params.getJobId());
			out.write(JobControl.outputJobForTerminal(job));
			out.close();
			break;
		case assemblies:
			List<Sequence> seqs = SequenceControl.getAllSequences();
			out.write("[");
			for(Sequence seq : seqs){
				out.write("{id:"+seq.getId()+",name:\""+seq.getName()+"\"}");
			}
			out.write("]");
			break;
		default:throw new AbortWithHttpStatusException(400,true);
		}

	}




	/**
	 * Add a track to an already existing project
	 * 
	 * parameters needed :
	 *  - url of the file
	 *  - project_id 
	 * @param user 
	 */
	private void addTrack(Users user) {
		checkParams(params.getUrl());
		checkParams(params.getProjectId());
		int projectId = params.getProjectId();
		String name = params.getName();

		boolean ok = ProjectControl.userAuthorized(projectId,user);
		if(!ok){
			error("not authorized to modify this project");
		} else {
			URL url = null;
			try {
				url = new URL(params.getUrl());
			} catch (MalformedURLException e) {
				error(e);
			}
			int jobId = JobControl.newUserTrack(user.getId(), projectId, url, null,null,name);
			success("{job_id:"+jobId+"}");
		}
	}



	/**
	 * create a new project in GDV database
	 */
	private void createNewProject(Users user) {
		checkParams(params.getSequenceId(),params.getName());
		int seqId = -1;
		try{
			seqId = Integer.parseInt(params.getSequenceId());
		}catch(NumberFormatException e){
			throw new AbortWithHttpStatusException(400,true);
		}
		if(seqId==-1){
			throw new AbortWithHttpStatusException(400,true);
		}
		/* check if this seq id exist in GDV */
		Sequence seq = SequenceControl.getSequence(seqId);
		if(null==seq){
			error("sequence : "+seqId+" is not created on GDV. You can ask at webmaster.bbcf@epfl.ch to add it");
			throw new AbortWithHttpStatusException(400,true);
		}
		
		boolean pub = false;
		if(params.isPublic()!=null && (params.isPublic().equalsIgnoreCase("true") || params.isPublic().equalsIgnoreCase("1"))){
			pub=true;
		}

		JSONObject json = null;
		try {
			json = ProjectControl.createNewProject(user,seqId,params.getName(),user.getId(),pub);
			success(json);
		} catch (JSONException e) {
			Application.error(e);
			error(e);
			throw new AbortWithHttpStatusException(400,true);
		}

	}



}
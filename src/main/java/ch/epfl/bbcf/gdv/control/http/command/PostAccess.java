package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.JobControl;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;

public class PostAccess extends Command{

	public PostAccess(RequestParameters params, PrintWriter out) {
		super(params, out);
	}


	private static final String NEW_PROJECT = "new_project"; 
	private static final String ADD_TRACK = "add_track";
	private static final String ADD_SQLITE_FILE = "add_sqlite"; 
	private static final String REQUEST_LOGIN = "request_login"; 



	@Override
	public void doRequest() {
		Application.debug("do request ");
		//SIGN IN
		checkParams(params.getMail(),params.getKey(),params.getCommand());
		Users user = UserControl.getuserByMailAndPass(params.getMail(),params.getKey());
		if(null==user){
			failed("problem with your key and password - check if they are valid");
		}
		if(params.getCommand().equalsIgnoreCase(NEW_PROJECT)){
			createNewProject(user);
		}else if(params.getCommand().equalsIgnoreCase(ADD_TRACK)||params.getCommand().equalsIgnoreCase(ADD_SQLITE_FILE)){
			addTrack(user);
		}else if(params.getCommand().equalsIgnoreCase(REQUEST_LOGIN)){
			requestLogin();
		} else {
			throw new AbortWithHttpStatusException(400,true);
		}

	}

	/**
	 * request a login on GDV
	 */
	private void requestLogin() {
		checkParams(params.getObfuscated(),params.getUrl());
		if(UserControl.checkUserKey(params.getObfuscated(),params.getUrl())){
			success(true);
		} else {
			failed("bad key (you will find it by login on GDV on the web interface\n" +
			" under preferences)");
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
		Project p = ProjectControl.getProject(projectId);
		String name = params.getName();
		URL url = null;
		try {
			url = new URL(params.getUrl());
		} catch (MalformedURLException e) {
			error(e);
		}
		int jobId = JobControl.newUserTrack(user.getId(), p, url, null,null);
		success("{job_id:"+jobId+"}");
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

		boolean pub = false;
		if(params.isPublic()!=null && (params.isPublic().equalsIgnoreCase("true") || params.isPublic().equalsIgnoreCase("1"))){
			pub=true;
		}
		log.debug("pub : "+pub);

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
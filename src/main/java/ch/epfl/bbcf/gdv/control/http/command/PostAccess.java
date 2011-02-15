package ch.epfl.bbcf.gdv.control.http.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.authentication.TequilaAuthentication;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.InputControl.InputType;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;

public class PostAccess extends Command{

	private static final String NEW_PROJECT = "new_project"; 
	private static final String ADD_TRACK = "add_track";
	private static final String ADD_SQLITE_FILE = "add_sqlite"; 
	private static final String REQUEST_LOGIN = "request_login"; 


	public PostAccess(UserSession session, RequestParameters params,
			WebResponse webResponse) {
		super(session, params, webResponse);
	}

	@Override
	public void doRequest() {
		Application.debug("do request ");
		//SIGN IN
		checkParams(params.getMail(),params.getKey(),params.getCommand());
		UserControl uc = new UserControl(session);
		Users user = uc.getuserByMailAndPass(params.getMail(),params.getKey());
		if(null==user){
			failed("problem with your key and password - it can be a server error");
		}
		session.signIn(params.getMail(), "tequila");
		Application.debug("signed in");
		//PROCESSES
		if(params.getCommand().equalsIgnoreCase(NEW_PROJECT)){
			createNewProject(user);
		}else if(params.getCommand().equalsIgnoreCase(ADD_TRACK)){
			addTrack(user);
		}else if(params.getCommand().equalsIgnoreCase(ADD_SQLITE_FILE)){
			addSqliteTrack(user);
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
	 * Add a track already processed to sqlite
	 * to an existing project
	 * url : the url where to fetch the file
	 * project_id : the project the track belongs to
	 * mail : the user
	 * @param user 
	 */
	private void addSqliteTrack(Users user) {
		checkParams(params.getUrl(),params.getProjectId(),params.getDatatype());
		int projectId = Integer.parseInt(params.getProjectId());
		ProjectControl pc = new ProjectControl(session);
		Project p = pc.getProject(projectId);
		InputControl ic = new InputControl(session);
		String name = params.getName();
		boolean result = ic.processInputs(projectId, params.getUrl(), null,-1, p.getSequenceId(), false, false, 
				new ArrayList<Group>(),InputType.NEW_SQLITE,params.getDatatype(),name);
		success(result);
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
		checkParams(params.getUrl(),params.getProjectId());
		int projectId = Integer.parseInt(params.getProjectId());
		ProjectControl pc = new ProjectControl(session);
		Project p = pc.getProject(projectId);
		InputControl ic = new InputControl(session);
		String name = params.getName();
		boolean result = ic.processInputs(projectId,params.getUrl(),null,-1,p.getSpecies().getId(),false,false,
				new ArrayList<Group>(),InputType.NEW_FILE,null,name);
		success(result);
	}


	/**
	 * create a new project in GDV database
	 * can be a group project (from hts3cseq,....)
	 * or a project for the user logged in (said as "normal")
	 * 
	 * parameters needed : 
	 * 	- for normal project creation : type,seq_id,name
	 *  - for group project : type,seq_id,name,obfuscated
	 * @param user 
	 */
	private void createNewProject(Users user) {
		checkParams(params.getType(),params.getSequenceId(),params.getName());
		int seqId = -1;
		try{
			seqId = Integer.parseInt(params.getSequenceId());
		}catch(NumberFormatException e){
			throw new AbortWithHttpStatusException(400,true);
		}
		if(seqId==-1){
			throw new AbortWithHttpStatusException(400,true);
		}
		if(Configuration.getGdv_types_access().contains(params.getType())){
			ProjectControl pc = new ProjectControl(session);
			//normal project creation
			if(params.getType().equalsIgnoreCase("normal")){
				int projectId = pc.createNewProject(seqId,params.getName(),user.getId());
				success(projectId);
				//project creation for group
			} else {
				checkParams(params.getObfuscated());
				UserControl uc = new UserControl(session);
				if(!uc.sameMailExist(params.getObfuscated())){
					int userId = uc.createNewUser(params.getObfuscated(),"","","","","",params.getType());
					int projectId = pc.createNewProject(seqId,params.getName(),userId);
					success(projectId);
				} else {
					Users u = uc.getuserByMail(params.getObfuscated());
					session.signIn(u.getMail(), params.getType());
					int projectId = pc.createNewProject(seqId,params.getName(),u.getId());
					success(projectId);
				}
			}
		} else {
			throw new AbortWithHttpStatusException(400,true);
		}
	}
}
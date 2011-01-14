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
		checkParams(params.getCommand());
		if(params.getCommand().equalsIgnoreCase(NEW_PROJECT)){
			createNewProject();
		}else if(params.getCommand().equalsIgnoreCase(ADD_TRACK)){
			addTrack();
		}else if(params.getCommand().equalsIgnoreCase(ADD_SQLITE_FILE)){
			addSqliteTrack();
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
	 */
	private void addSqliteTrack() {
		checkParams(params.getUrl(),params.getProjectId(),params.getUsermail(),params.getDatatype());
		int projectId = Integer.parseInt(params.getProjectId());
		if(session.authenticate(params.getObfuscated(),"tequila")){
			ProjectControl pc = new ProjectControl(session);
			Project p = pc.getProject(projectId);
			InputControl ic = new InputControl(session);
			ic.processInputs(projectId, params.getUrl(), null,-1, p.getSequenceId(), false, false, 
					new ArrayList<Group>(),InputType.NEW_SQLITE,params.getDatatype(),null);
		}
	}

	/**
	 * Add a track to an already existing project
	 * url : the url where to fetch the file
	 * project_id : the project the track belongs to
	 * obfuscated : the user
	 */
	private void addTrack() {
		checkParams(params.getType());
		if(Configuration.getGdv_types_access().contains(params.getType())){
			checkParams(params.getUrl(),params.getProjectId(),params.getObfuscated());
			int projectId = Integer.parseInt(params.getProjectId());
			session.signIn(params.getObfuscated(),params.getType());
			ProjectControl pc = new ProjectControl(session);
			Project p = pc.getProject(projectId);
			InputControl ic = new InputControl(session);
			boolean result = ic.processInputs(projectId,params.getUrl(),null,-1,p.getSpecies().getId(),false,false,
					new ArrayList<Group>(),InputType.NEW_FILE,null,null);
			success(result);
		} else {
			throw new AbortWithHttpStatusException(400,true);
		}
	}


	/**
	 * create a new user in gdv database (if not already exist), 
	 * then create a project for him 
	 * needed : - type : from which project (e.g hts3cseq)
	 * 			- seq_id : the sequence id in Genrep
	 */
	private void createNewProject() {
		checkParams(params.getMail(),params.getPass(),params.getType(),params.getSequenceId());
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
			UserControl uc = new UserControl(session);
			ProjectControl pc = new ProjectControl(session);
			if(params.getType().equalsIgnoreCase("normal")){
				Users user = uc.getuserByMailAndPass(params.getMail(),params.getPass());
				if(null==user){
					failed("problem with your key and password - it can be a server error");
				}
				int projectId = pc.createNewProject(seqId,params.getName(),user.getId());
				success(projectId);
			} else {
				/**
				 * name : the name of the project
				 * obfuscated : the mail of the new user to create
				 */
				checkParams(params.getName(),params.getObfuscated());
				
				String mail = params.getObfuscated();
				
				if(!uc.sameMailExist(mail)){
					int userId = uc.createNewUser(mail,"","","","","",params.getType());
					int projectId = pc.createNewProject(seqId,params.getName(),userId);
					success(projectId);
				} else {
					Users user = uc.getuserByMail(mail);
					session.signIn(user.getMail(), params.getType());
					int projectId = pc.createNewProject(seqId,params.getName(),user.getId());
					success(projectId);
				}
			}
		} else {
			throw new AbortWithHttpStatusException(400,true);
		}
	}
}
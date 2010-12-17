package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.formats.json.JSONProcessor;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.mail.Sender;

public class FrontController {

	private static Logger log = Logs.initPOSTLogger();

	private RequestParameters params;
	private List<String> paramsNeeded;

	private UserSession session;

	private WebResponse webResponse;

	public FrontController(RequestParameters params,UserSession session, WebResponse webResponse){
		this.session = session;
		this.params = params;
		this.paramsNeeded = new ArrayList<String>();
		this.webResponse = webResponse;
	}

	public void doRequest() {
		if(params.getId()!=null){
			//CHANGE STATUS OF A TRACK
			if(params.getId().equalsIgnoreCase("track_status")){
				checkParams(params.getTrackId());
				int trackId = Integer.parseInt(params.getTrackId());
				String status = params.getMessage();
				try {
					TrackControl.updatePercentage(trackId, Integer.parseInt(status));
				}catch(NumberFormatException e){
					TrackControl.updateTrack(trackId,status);
				}
				//ERROR IN TRACK PROCESSING
			} else if(params.getId().equalsIgnoreCase("track_error")){
				checkParams(params.getTrackId(),params.getMessage(),params.getType());
				int trackId = Integer.parseInt(params.getTrackId());
				if(params.getType().equalsIgnoreCase("ext")) {
					TrackControl.updateTrack(trackId,params.getMessage());
				} else if(params.getType().equalsIgnoreCase("md5")){
					TrackControl.updateTrack(trackId,"server error (cannot get md5 of the file)");
				} else if(params.getType().equalsIgnoreCase("parsing")){
					TrackControl.updateTrack(trackId,"error when parsing the file : "+params.getMessage());
				} else if(params.getType().equalsIgnoreCase("chrList")){
					TrackControl.updateTrack(trackId,"server error ("+params.getMessage()+")");
				}



				//TRANSFORM TO SQLITE SUCCED
			} else if(params.getId().equalsIgnoreCase("track_parsing_success")){
				checkParams(params.getType(),params.getDb(),params.getUsermail());
				if(params.getType().equalsIgnoreCase("quantitatif")){
					Application.debug("writing new job to calcul score on "+params.getDb());
					SQLiteAccess.writeNewJobCalculScores(params.getTrackId(),params.getDb(),Configuration.getFilesDir(),params.getDb(),Configuration.getTracks_dir(),"0",params.getUsermail());
				} else if(params.getType().equalsIgnoreCase("qualitatif")){
					//UPDATE qualitatif

					Application.debug("db "+params.getDb()+" completed");
					TrackControl.updateTrack(Integer.parseInt(params.getTrackId()),"completed");
					if(!params.getUsermail().equalsIgnoreCase("nomail")){
						Sender.sendMessage(" File processed ","Your file has been processed by GDV you can now browse your projects at "+Configuration.getProjectUrl() , false, params.getUsermail());
					}
				} else {
					throw new AbortWithHttpStatusException(400,true);
				}


				//HTC
			} else if(params.getId().equalsIgnoreCase("hts3cseq")){
				//NEW HTC3CSEQ PROJECT
				/**
				 * id : must be 'htc3cseq'
				 * type : must be 'new_project'
				 * hts3cseq_number : the obfuscated string
				 * name : the name of the project to create
				 * seq_id : the sequence of the assembly (see genrep)
				 */
				if(params.getType()!=null && params.getType().equalsIgnoreCase("new_project")){
					checkParams(params.getHtc3cseq_number(),params.getName(),params.getSeq_id());
					int seq_id = -1;
					try{
						seq_id = Integer.parseInt(params.getSeq_id());
					}catch(NumberFormatException e){
						throw new AbortWithHttpStatusException(400,true);
					}
					if(seq_id==-1){
						throw new AbortWithHttpStatusException(400,true);
					}
					UserControl uc = new UserControl(session);
					String mail = params.getHtc3cseq_number()+"_htc3cseq";
					ProjectControl pc = new ProjectControl(session);
					if(!uc.sameMailExist(mail)){
						int userId = uc.createNewUser(mail,"","","","","", "hts3cseq");
						int projectId = pc.createNewProject(seq_id,params.getName(),userId);
						success(projectId);
					} else {
						Users user = uc.getuserByMail(mail);
						session.signIn(user.getMail(), "hts3cseq");
						int projectId = pc.createNewProject(seq_id,params.getName(),user.getId());
						success(projectId);
					}
					//ADD TRACK TO PROJECT
					/**
					 * id : must be 'hts3cseq'
					 * type : must be 'add_track'
					 * url : the url where to fetch the file
					 * project_id : the project the track belongs to
					 * seq_id : the sequence of the assembly (see genrep)
					 */
				} else if(params.getType()!=null && params.getType().equalsIgnoreCase("add_track")){
					checkParams(params.getUrl(),params.getProjectId(),params.getSeq_id());
					int projectId = Integer.parseInt(params.getProjectId());
					UserControl uc = new UserControl(session);
					Users u = uc.getUserByProjectId(projectId);
					session.signIn(u.getMail(), "hts3cseq");
					InputControl ic = new InputControl(session);
					boolean result = ic.processInputs(projectId,params.getUrl(),null,params.getSeq_id(),false,false,null);
					success();

					//id not recognized
				} else {
					throw new AbortWithHttpStatusException(400,true);
				}

			}




		} else {
			throw new AbortWithHttpStatusException(400,true);
		}

	}

	private void checkParams(String... params) {
		for(String p : params){
			if(null==p){
				log.error("params needed == null");
				throw new AbortWithHttpStatusException(400,true);
			}
		}

	}


	private void success(int id){
		webResponse.getHttpServletResponse().setContentType("text/plain");
		try {
			webResponse.getHttpServletResponse().getOutputStream().print(id);
		} catch (IOException e) {
			log.error(e);
		}
		success();
	}
	private void success(){
		webResponse.getHttpServletResponse().setStatus(HttpServletResponse.SC_OK) ;
	}




}

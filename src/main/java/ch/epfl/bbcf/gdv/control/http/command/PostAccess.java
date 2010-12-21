package ch.epfl.bbcf.gdv.control.http.command;

import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;

public class PostAccess extends Command{

	public PostAccess(UserSession session, RequestParameters params,
			WebResponse webResponse) {
		super(session, params, webResponse);
	}

	@Override
	protected void initLog() {
		log = Logs.initLogger(PostAccess.class.getName());
	}

	@Override
	public void doRequest() {
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

}

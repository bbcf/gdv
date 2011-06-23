package ch.epfl.bbcf.gdv.control.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Session;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.GroupDAO;
import ch.epfl.bbcf.gdv.access.database.dao.ProjectDAO;
import ch.epfl.bbcf.gdv.access.database.dao.SequenceDAO;
import ch.epfl.bbcf.gdv.access.database.dao.SpeciesDAO;
import ch.epfl.bbcf.gdv.access.database.dao.TrackDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.database.pojo.Species;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

public class ProjectControl extends Control implements Serializable{

	public ProjectControl(UserSession session) {
		super(session);
	}



	/**
	 * get the number of tracks belonging to 
	 * a project
	 * @param projectId
	 * @return
	 */
	public int tracksNumberUnderProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		return pdao.tracksNumberUnderProject(projectId);
	}

	public void updateProject(int id, int sequenceId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection(session));
		pdao.updateProject(id, sequenceId);
	}

	/**
	 * create a new project for an user
	 * @param species
	 * @param version
	 * @param projectName
	 */
	public boolean createNewProjectFromProjectPage(SelectOption species, SelectOption version,
			String projectName) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		int seq_id = version.getKey();
		int projectId = pdao.createNewProject(seq_id,projectName,false);
		if(projectId!=-1){
			boolean created = pdao.linkToUser(projectId,session.getUserId());
			if(created){
				Application.info("create new project :"+projectName,session.getUserId());
				return created;
			}

		}
		Application.error("project creation failed ", session.getUserId());
		return false;
	}
	/**
	 * create a new project for an user
	 * @param seq_id the seq id from genrep
	 * @param projectName the project name
	 * @param userId the user id
	 * @return
	 */
	public int createNewProject(int seq_id,String projectName,int userId){
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		int projectId = pdao.createNewProject(seq_id,projectName,false);
		if(projectId!=-1){
			boolean created = pdao.linkToUser(projectId,userId);
			if(created){
				Application.info("create new project :"+projectName,userId);
				return projectId;
			}

		}
		Application.error("project creation failed ", userId);
		return -1;
	}
	/**
	 * create a new project for an user
	 * Should be used just for the creation of projects by post request
	 * @param seq_id the seq id from genrep
	 * @param projectName the project name
	 * @param userId the user id
	 * @param isPublic - create a public project
	 * @return
	 * @throws JSONException 
	 */
	public static JSONObject createNewProject(Users user,int seq_id,String projectName,int userId,boolean isPublic) throws JSONException{
		JSONObject json = new JSONObject();
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		int projectId = pdao.createNewProject(seq_id,projectName,isPublic);
		if(projectId!=-1){
			boolean created = pdao.linkToUser(projectId,userId);
			if(created){
				json.put("project_id",projectId);
				if(isPublic){
					setProjectPublic(projectId, isPublic);
					String url = getPublicUrlFromProjectId(user,projectId);
					json.put("public_url",url);
				}
				return json;
			}

		}
		Application.error("project creation failed ", userId);
		return json;
	}

	/**
	 * get a project from its id and add the species to it
	 * @param projectId
	 * @return
	 */
	public static Project getProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		Project p = pdao.getProject(projectId);
		if(null==p){
			return null;
		}
		SpeciesDAO spDAO = new SpeciesDAO(Connect.getConnection());
		p.setSpecies(spDAO.getSpeciesFromProjectId(projectId));
		return p;
	}

	/**
	 * check if an user is auth to 
	 * see this project
	 * @return
	 */
	public static boolean userAuthorized(Project p,Users user) {
		ProjectDAO dao = new ProjectDAO(Connect.getConnection());
		return dao.userAuthorized(user,p.getId());
	}

	/**
	 * get the species belonging
	 * to this project id
	 * @param id
	 * @return
	 */
	public Species getSpeciesFromProjectId(int projectId) {
		SpeciesDAO spDAO = new SpeciesDAO(Connect.getConnection(session));
		return spDAO.getSpeciesFromProjectId(projectId);
	}

	/**
	 * get the sequences under this species id
	 * (Select Option)
	 * @param speciesId
	 * @return
	 */
	public List<SelectOption> getSequencesFromSpeciesIdSO(int speciesId) {
		SequenceDAO sDAO = new SequenceDAO(Connect.getConnection(session));
		List<Sequence> seqs = sDAO.getSequencesFromSpeciesId(speciesId);
		List<SelectOption> sos = new ArrayList<SelectOption>();
		for(Sequence seq : seqs){
			sos.add(new SelectOption(seq.getId(), getName()));
		}
		return sos;
	}

	/**
	 * import the project for this user
	 * form it's cookie value
	 * (create a copy of the project and
	 * link it to the tracks and user)
	 * @param projectId
	 * @return
	 */
	public boolean importProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection(session));
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		Project oldProject = pdao.getProject(projectId);
		List<Track> tracks = tdao.getTracksFromProjectId(oldProject.getId());
		List<Project> projects = pdao.getProjectsFromUser(session.getUserId());
		//		if(projects!=null){
		//			for (Project p : projects){
		//				if(p.getName().equalsIgnoreCase(oldProject.getName())){
		//					//return false;
		//				}
		//			}
		//		}
		int newProjectId = pdao.createNewProject(oldProject.getSequenceId(), oldProject.getName(),false);
		if(null!=tracks){
			for(Track t : tracks){
				tdao.linkToProject(t.getId(),newProjectId);
			}
		}
		return pdao.linkToUser(newProjectId,session.getUserId());
	}

	public void deleteProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection(session));
		pdao.deleteProject(projectId);
	}

	public void renameProject(int id, String input) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection(session));
		pdao.renameProject(id,input);
	}

	public List<Group> getGroupNameFromProjectId(int id,String mail) {
		GroupDAO dao = new GroupDAO(Connect.getConnection(session));
		return dao.getGroupNameFromProjectIdandUserMail(id,mail);
	}

	/**
	 * get the projects belonging to an user
	 * plus all projects the user belongs to
	 * @return
	 */
	public List<Project> getAllProjectFromUser() {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		return pdao.getAllProjectsFromUser(session.getUser());
	}
	/**
	 * get the projects belonging to an user
	 * @return
	 */
	public List<Project> getProjectsFromUser() {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		return pdao.getProjectsFromUser(session.getUserId());
	}



	public static String getPublicUrlFromProjectId(Users user,int id) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		String key = pdao.getPublicKeyFromProjectId(id);
		String userKey = user.getKey();
		String url = Configuration.getBrowserUrl()+"?id="+id+"&ukey="+userKey+"&pkey="+key;
		return url;
	}



	public static String setProjectPublic(int id, boolean b) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		pdao.setProjectPublic(id,b);
		String key = pdao.getPublicKeyFromProjectId(id);
		if(null==key){
			key = pdao.generatePublicKey(id);
		}
		return key;
	}



	public static boolean isProjectPublic(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		return pdao.isProjectPublic(projectId);
	}



	public static String getPublicKeyFromProjectId(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		return pdao.getPublicKeyFromProjectId(projectId);
	}



	public static String getUserKeyFromProjectId(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		return pdao.getUserKeyFromProjectId(projectId);
	}



	public boolean hasProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection(session));
		return pdao.userHasProject(session.getUserId(),projectId);
	}


}

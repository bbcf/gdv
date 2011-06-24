package ch.epfl.bbcf.gdv.control.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Session;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.Conn;
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




	/**
	 * get the number of tracks belonging to 
	 * a project
	 * @param projectId
	 * @return
	 */
	public static int tracksNumberUnderProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		return pdao.tracksNumberUnderProject(projectId);
	}

	public static void updateProject(int id, int sequenceId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		pdao.updateProject(id, sequenceId);
	}

	/**
	 * create a new project for an user
	 * @param species
	 * @param version
	 * @param projectName
	 */
	public static boolean createNewProjectFromProjectPage(SelectOption species, SelectOption version,
			String projectName,int userId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		int seq_id = version.getKey();
		int projectId = pdao.createNewProject(seq_id,projectName,false);
		if(projectId!=-1){
			boolean created = pdao.linkToUser(projectId,userId);
			if(created){
				Application.info("create new project :"+projectName,userId);
				return created;
			}

		}
		Application.error("project creation failed ", userId);
		return false;
	}
	/**
	 * create a new project for an user
	 * @param seq_id the seq id from genrep
	 * @param projectName the project name
	 * @param userId the user id
	 * @return
	 */
	public static int createNewProject(int seq_id,String projectName,int userId){
		ProjectDAO pdao = new ProjectDAO(Conn.get());
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
		ProjectDAO pdao = new ProjectDAO(Conn.get());
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
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		Project p = pdao.getProject(projectId);
		if(null==p){
			return null;
		}
		SpeciesDAO spDAO = new SpeciesDAO(Conn.get());
		p.setSpecies(spDAO.getSpeciesFromProjectId(projectId));
		return p;
	}

	/**
	 * check if an user is auth to 
	 * see this project
	 * @return
	 */
	public static boolean userAuthorized(Project p,Users user) {
		ProjectDAO dao = new ProjectDAO(Conn.get());
		return dao.userAuthorized(user,p.getId());
	}

	/**
	 * get the species belonging
	 * to this project id
	 * @param id
	 * @return
	 */
	public static Species getSpeciesFromProjectId(int projectId) {
		SpeciesDAO spDAO = new SpeciesDAO(Conn.get());
		return spDAO.getSpeciesFromProjectId(projectId);
	}

	/**
	 * get the sequences under this species id
	 * (Select Option)
	 * @param speciesId
	 * @return
	 */
	public static List<SelectOption> getSequencesFromSpeciesIdSO(int speciesId) {
		SequenceDAO sDAO = new SequenceDAO(Conn.get());
		List<Sequence> seqs = sDAO.getSequencesFromSpeciesId(speciesId);
		List<SelectOption> sos = new ArrayList<SelectOption>();
		for(Sequence seq : seqs){
			sos.add(new SelectOption(seq.getId(), seq.getName()));
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
	public static boolean importProject(int projectId,int userId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		TrackDAO tdao = new TrackDAO(Conn.get());
		Project oldProject = pdao.getProject(projectId);
		List<Track> tracks = tdao.getTracksFromProjectId(oldProject.getId());
		List<Project> projects = pdao.getProjectsFromUser(userId);
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
		return pdao.linkToUser(newProjectId,userId);
	}

	public static void deleteProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		pdao.deleteProject(projectId);
	}

	public static void renameProject(int id, String input) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		pdao.renameProject(id,input);
	}

	public static List<Group> getGroupNameFromProjectId(int id,String mail) {
		GroupDAO dao = new GroupDAO(Conn.get());
		return dao.getGroupNameFromProjectIdandUserMail(id,mail);
	}

	/**
	 * get the projects belonging to an user
	 * plus all projects the user belongs to
	 * @return
	 */
	public static List<Project> getAllProjectFromUser(Users user) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		return pdao.getAllProjectsFromUser(user);
	}
	/**
	 * get the projects belonging to an user
	 * @return
	 */
	public static List<Project> getProjectsFromUser(int userId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		return pdao.getProjectsFromUser(userId);
	}



	public static String getPublicUrlFromProjectId(Users user,int id) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		String key = pdao.getPublicKeyFromProjectId(id);
		String userKey = user.getKey();
		String url = Configuration.getBrowserUrl()+"?id="+id+"&ukey="+userKey+"&pkey="+key;
		return url;
	}



	public static String setProjectPublic(int id, boolean b) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		pdao.setProjectPublic(id,b);
		String key = pdao.getPublicKeyFromProjectId(id);
		if(null==key){
			key = pdao.generatePublicKey(id);
		}
		return key;
	}



	public static boolean isProjectPublic(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		return pdao.isProjectPublic(projectId);
	}



	public static String getPublicKeyFromProjectId(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		return pdao.getPublicKeyFromProjectId(projectId);
	}



	public static String getUserKeyFromProjectId(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		return pdao.getUserKeyFromProjectId(projectId);
	}



	public static boolean hasProject(int projectId,int userId) {
		ProjectDAO pdao = new ProjectDAO(Conn.get());
		return pdao.userHasProject(userId,projectId);
	}


}

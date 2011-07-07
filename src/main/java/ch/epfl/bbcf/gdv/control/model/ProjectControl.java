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
		ProjectDAO pdao = new ProjectDAO();
		int i = pdao.tracksNumberUnderProject(projectId);
		pdao.release();
		return i;
	}

	public static void updateProject(int id, int sequenceId) {
		ProjectDAO pdao = new ProjectDAO();
		pdao.updateProject(id, sequenceId);
		pdao.release();
	}

	/**
	 * create a new project for an user
	 * @param species
	 * @param version
	 * @param projectName
	 */
	public static boolean createNewProjectFromProjectPage(SelectOption species, SelectOption version,
			String projectName,int userId) {
		ProjectDAO pdao = new ProjectDAO();
		int seq_id = version.getKey();
		int projectId = pdao.createNewProject(seq_id,projectName,false);
		if(projectId!=-1){
			boolean created = pdao.linkToUser(projectId,userId);
			if(created){
				Application.info("create new project :"+projectName,userId);
				pdao.release();
				return created;
			}

		}
		Application.error("project creation failed ", userId);
		pdao.release();
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
		ProjectDAO pdao = new ProjectDAO();
		int projectId = pdao.createNewProject(seq_id,projectName,false);
		if(projectId!=-1){
			boolean created = pdao.linkToUser(projectId,userId);
			if(created){
				Application.info("create new project :"+projectName,userId);
				pdao.release();
				return projectId;
			}

		}
		Application.error("project creation failed ", userId);
		pdao.release();
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
		ProjectDAO pdao = new ProjectDAO();
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
				pdao.release();
				return json;
			}

		}
		Application.error("project creation failed ", userId);
		pdao.release();
		return json;
	}

	/**
	 * get a project from its id and add the species to it
	 * @param projectId
	 * @return
	 */
	public static Project getProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO();
		Project p = pdao.getProject(projectId);
		if(null==p){
			pdao.release();
			return null;
		}
		SpeciesDAO spDAO = new SpeciesDAO();
		p.setSpecies(spDAO.getSpeciesFromProjectId(projectId));
		spDAO.release();
		return p;
	}

	/**
	 * check if an user is auth to 
	 * see or modify this project
	 * @return
	 */
	public static boolean userAuthorized(Project p,Users user) {
		return userAuthorized(p.getId(), user);
	}
	/**
	 * check if an user is auth to 
	 * see or modify this project
	 * @return
	 */
	public static boolean userAuthorized(int projectId,int userId) {
		Users u = UserControl.getUserById(userId);
		return userAuthorized(projectId, u);
	}
	/**
	 * check if an user is auth to 
	 * see or modify this project
	 * @return
	 */
	public static boolean userAuthorized(int projectId,Users user) {
		ProjectDAO dao = new ProjectDAO();
		boolean i = dao.userAuthorized(user,projectId);
		dao.release();
		return i;
	}

	/**
	 * get the species belonging
	 * to this project id
	 * @param id
	 * @return
	 */
	public static Species getSpeciesFromProjectId(int projectId) {
		SpeciesDAO spDAO = new SpeciesDAO();
		Species i = spDAO.getSpeciesFromProjectId(projectId);
		spDAO.release();
		return i;
	}

	/**
	 * get the sequences under this species id
	 * (Select Option)
	 * @param speciesId
	 * @return
	 */
	public static List<SelectOption> getSequencesFromSpeciesIdSO(int speciesId) {
		SequenceDAO sDAO = new SequenceDAO();
		List<Sequence> seqs = sDAO.getSequencesFromSpeciesId(speciesId);
		List<SelectOption> sos = new ArrayList<SelectOption>();
		for(Sequence seq : seqs){
			sos.add(new SelectOption(seq.getId(), seq.getName()));
		}
		sDAO.release();
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
		ProjectDAO pdao = new ProjectDAO();
		TrackDAO tdao = new TrackDAO();
		Project oldProject = pdao.getProject(projectId);
		List<Track> tracks = tdao.getTracksFromProjectId(oldProject.getId());
		int newProjectId = pdao.createNewProject(oldProject.getSequenceId(), oldProject.getName(),false);
		if(null!=tracks){
			for(Track t : tracks){
				tdao.linkToProject(t.getId(),newProjectId);
			}
		}
		boolean b = pdao.linkToUser(newProjectId,userId);
		pdao.release();
		tdao.release();
		return b;
	}

	public static void deleteProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO();
		pdao.deleteProject(projectId);
		pdao.release();
	}

	public static void renameProject(int id, String input) {
		ProjectDAO pdao = new ProjectDAO();
		pdao.renameProject(id,input);
		pdao.release();
	}

	public static List<Group> getGroupNameFromProjectId(int id,String mail) {
		GroupDAO dao = new GroupDAO();
		List<Group> i = dao.getGroupNameFromProjectIdandUserMail(id,mail);
		dao.release();
		return i;
	}

	/**
	 * get the projects belonging to an user
	 * plus all projects the user belongs to
	 * @return
	 */
	public static List<Project> getAllProjectFromUser(Users user) {
		ProjectDAO pdao = new ProjectDAO();
		List<Project> i = pdao.getAllProjectsFromUser(user);
		pdao.release();
		return i;
	}
	/**
	 * get the projects belonging to an user
	 * @return
	 */
	public static List<Project> getProjectsFromUser(int userId) {
		ProjectDAO pdao = new ProjectDAO();
		List<Project> i = pdao.getProjectsFromUser(userId);
		pdao.release();
		return i;
	}



	public static String getPublicUrlFromProjectId(Users user,int id) {
		ProjectDAO pdao = new ProjectDAO();
		String key = pdao.getPublicKeyFromProjectId(id);
		String userKey = user.getKey();
		String url = Configuration.getBrowserUrl()+"?id="+id+"&ukey="+userKey+"&pkey="+key;
		pdao.release();
		return url;
	}



	public static String setProjectPublic(int id, boolean b) {
		ProjectDAO pdao = new ProjectDAO();
		pdao.setProjectPublic(id,b);
		String key = pdao.getPublicKeyFromProjectId(id);
		if(null==key){
			key = pdao.generatePublicKey(id);
		}
		pdao.release();
		return key;
	}



	public static boolean isProjectPublic(int projectId) {
		ProjectDAO pdao = new ProjectDAO();
		boolean i = pdao.isProjectPublic(projectId);
		pdao.release();
		return i;
	}



	public static String getPublicKeyFromProjectId(int projectId) {
		ProjectDAO pdao = new ProjectDAO();
		String i = pdao.getPublicKeyFromProjectId(projectId);
		pdao.release();
		return i;
	}



	public static String getUserKeyFromProjectId(int projectId) {
		ProjectDAO pdao = new ProjectDAO();
		String i = pdao.getUserKeyFromProjectId(projectId);
		pdao.release();
		return i;
	}



	public static boolean hasProject(int projectId,int userId) {
		ProjectDAO pdao = new ProjectDAO();
		boolean i = pdao.userHasProject(userId,projectId);
		pdao.release();
		return i;
	}




}

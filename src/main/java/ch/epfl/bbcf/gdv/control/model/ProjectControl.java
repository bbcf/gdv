package ch.epfl.bbcf.gdv.control.model;

import java.util.ArrayList;
import java.util.List;

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
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

public class ProjectControl extends Control{

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
	public boolean createNewProject(SelectOption species, SelectOption version,
			String projectName) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		int seq_id = version.getKey();
		int projectId = pdao.createNewProject(seq_id,projectName);
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
		int projectId = pdao.createNewProject(seq_id,projectName);
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
	 * get a project from its id and add the species to it
	 * @param projectId
	 * @return
	 */
	public Project getProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection(session));
		Project p = pdao.getProject(projectId);
		if(null==p){
			return null;
		}
		SpeciesDAO spDAO = new SpeciesDAO(Connect.getConnection(session));
		p.setSpecies(spDAO.getSpeciesFromProjectId(projectId));
		return p;
	}

	/**
	 * check if an user is auth to 
	 * see this project
	 * @return
	 */
	public boolean userAuthorized(Project p) {
		ProjectDAO dao = new ProjectDAO(Connect.getConnection(session));
		return dao.userAuthorized(session.getUser(),p.getId());
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
	 * @param value
	 * @return
	 */
	public boolean importProject(String value) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection(session));
		TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
		Project oldProject = pdao.getProject(Integer.parseInt(value));
		List<Track> tracks = tdao.getTracksFromProjectId(oldProject.getId());
		List<Project> projects = pdao.getProjectsFromUser(session.getUserId());
		if(projects!=null){
			for (Project p : projects){
				if(p.getName().equalsIgnoreCase(oldProject.getName())){
					//return false;
				}
			}
		}
		int newProjectId = pdao.createNewProject(oldProject.getSequenceId(), oldProject.getName());
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


}

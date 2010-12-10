package ch.epfl.bbcf.gdv.control.model;

import java.util.List;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.dao.ProjectDAO;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Project;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

public class ProjectControl extends Control{

	public ProjectControl(UserSession session) {
		super(session);
	}

	/**
	 * get the projects belonging to an user
	 * @return
	 */
	public List<Project> getProjectsFromUser() {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		return pdao.getProjectsFromUser(session.getUserId());
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

	/**
	 * create a new project for an user
	 * @param species
	 * @param version
	 * @param projectName
	 */
	public boolean createNewProject(SelectOption species, SelectOption version,
			String projectName) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		int seq_id = Integer.parseInt(version.getKey());
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
	 * get a project from its id
	 * @param projectId
	 * @return
	 */
	public Project getProject(int projectId) {
		ProjectDAO pdao = new ProjectDAO(Connect.getConnection());
		return pdao.getProject(projectId);
	}

	/**
	 * check if an user is auth to 
	 * see this project
	 * @return
	 */
	public boolean userAuthorized(Project p) {
		ProjectDAO dao = new ProjectDAO(Connect.getConnection(session));
		return dao.userAuthorized(session.getUserId(),p.getId());
	}

}

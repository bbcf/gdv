package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.utility.RandomKey;

public class ProjectDAO extends DAO<Project>{

	private final static String tableName ="projects";
	private final static String[] fields = {"id","cur_seq_id","name","isPublic"};

	public ProjectDAO(Connection connection) {
		super(connection);
	}


	/**
	 * create a new project in the database
	 * @param seq_id
	 * @param projectName
	 * @param isPublic 
	 * @param userId
	 * @return
	 */
	public int createNewProject(int seq_id, String projectName, boolean isPublic) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into projects values (" +
				"default , ? , ? ,?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,seq_id);
				statement.setString(2,projectName);
				statement.setBoolean(3, isPublic);
				this.executeUpdate(statement);
				query = "select currval('projects_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                               
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					int projectId = resultSet.getInt(1);
					this.endQuery(true);
					return projectId;
				}
			} catch (SQLException e) {
				logger.error("createNewProject "+e);
			}
		}
		this.endQuery(false);
		return -1;
	}

	public boolean createDefault(int projectId, int genomeId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into projects values (" +
				" ? , ? , ? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				statement.setString(2, "default");
				statement.setInt(3, genomeId);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return false;
	}


	/**
	 * get a project from its id
	 * @param id
	 * @return
	 */
	public Project getProject(int id) {
		Project project = null;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from "+tableName+
				" where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					project = getProject(resultSet);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return project;
	}

	public boolean projectWithSameSequenceExistForUser(int trackId, int userId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select 1 from projects as t1 " +
				"inner join usertoProject as t2 on t2.project_id = t1.id " +
				"where t1.seq_id = ? and t2.user_id= ? limit 1  ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, trackId);
				statement.setInt(2, userId);
				ResultSet resultSet = this.executeQuery(statement);
				if (resultSet.first()) {
					this.endQuery(true);
					return true;
				}
			} catch (SQLException e) {
				logger.error("projectWithSameSequenceExistForUser "+e);
			}
			this.endQuery(false);
		}
		return false;
	}


	public Project getProjectWithSameSequenceExistForUser(int trackId, int userId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from projects as t1 " +
				"inner join usertoProject as t2 on t2.project_id = t1.id " +
				"where t1.seq_id = ? and t2.user_id= ? limit 1  ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, trackId);
				statement.setInt(2, userId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					return 	getProject(resultSet);
				}
			} catch (SQLException e) {
				logger.error("getProjectWithSameSequenceExistForUser " +e);
			}
			this.endQuery(false);
		}
		return null;
	}


	private List<Project> getProjects(ResultSet resultSet) {
		List<Project> projects = new ArrayList<Project>();
		try {
			while (resultSet.next()) {
				projects.add(getProject(resultSet));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return projects;
	}






	private Project getProject(ResultSet resultSet) {
		Project project = new Project();
		try {
			project.setId(resultSet.getInt(fields[0]));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			project.setName(resultSet.getString(fields[2]));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			project.setCurrentSequenceId(resultSet.getInt(fields[1]));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			project.setPublic(resultSet.getBoolean(fields[3]));
		} catch (SQLException e) {
			logger.error(e);
		}
		return project;
	}


	/**
	 * link a project to an user
	 */
	public boolean linkToUser(int projectId, int userId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into usertoproject values ( ? , ? );";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(2, projectId);
				statement.setInt(1, userId);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return false;
	}

	public void updateProject(int id, int sequenceId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update projects set cur_seq_id = ? " +
				"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(2, id);
				statement.setInt(1, sequenceId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
	}


	public boolean userAuthorized(Users user, int viewId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select 1 from usertoproject as t1 " +
				"where t1.project_id =  ? and t1.user_id = ? " +
				"union " +
				"select 1 from grouptoproject as t2 " +
				"inner join userToGroup as t3 on t2.group_id = t3.group_id " +
				"where t3.user_mail = ? " +
				"limit 1;";

				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, viewId);
				statement.setInt(2, user.getId());
				statement.setString(3, user.getMail());
				ResultSet resultSet = this.executeQuery(statement);
				if (resultSet.first()) {
					this.endQuery(true);
					return true;
				}
			} catch (SQLException e) {
				logger.error(e);
			}
			this.endQuery(false);
		}
		return false;
	}

	/**
	 * get the projects from user id
	 * @param userId
	 * @return
	 */
	public List<Project> getProjectsFromUser(int userId) {
		List<Project> views = new ArrayList<Project>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from projects as t1 " +
				"inner join usertoProject as t2 on t1.id = t2.project_id " +
				"where t2.user_id = ? ;";	
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, userId);
				ResultSet resultSet = this.executeQuery(statement);
				views = getProjects(resultSet);
				this.endQuery(true);
				return views;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return views;
	}
	public List<Project> getAllProjectsFromUser(Users user) {
		List<Project> projects = new ArrayList<Project>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from projects as t1 " +
				"inner join usertoProject as t2 on t1.id = t2.project_id " +
				"where t2.user_id = ? " +
				"union " +
				"select t3.* from projects as t3 " +
				"inner join grouptoproject as t4 on t3.id=t4.project_id " +
				"inner join groups as t5 on t5.id = t4.group_id " +
				"where t5.owner = ? "+
				"union "+
				"select t6.* from projects as t6 " +
				"inner join grouptoproject as t7 on t6.id = t7.project_id " +
				"inner join userToGroup as t8 on t7.group_id = t8.group_id " +
				"where t8.user_mail = ?  ;";	
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, user.getId());
				statement.setInt(2, user.getId());
				statement.setString(3, user.getMail());
				ResultSet resultSet = this.executeQuery(statement);
				projects = getProjects(resultSet);
				this.endQuery(true);
				return projects;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return projects;
	}

	/**
	 * true if the user has a view with this sequenceId
	 * @param userid
	 * @param sequenceId
	 * @return
	 */
	public boolean projectExistForUserAndSequenceId(int userid, String sequenceId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select 1 from projects as t1 " +
				"inner join usertoproject as t2 on t1.id = t2.project_id " +
				"where t2.user_id = ? and t1.seq_id = ? limit 1; ";
				PreparedStatement statement;
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,userid);
				statement.setInt(2, Integer.parseInt(sequenceId));
				ResultSet r = statement.executeQuery();
				if(r.first()){
					return true;
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return false;
	}





	public Project getProjectFromUserAndSequenceId(int userid, String sequenceId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from projects as t1 " +
				"inner join usertoproject as t2 on t1.id = t2.project_id " +
				"where t2.user_id = ? and t1.seq_id = ? limit 1; ";
				PreparedStatement statement;
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,userid);
				statement.setInt(2, Integer.parseInt(sequenceId));
				ResultSet r = statement.executeQuery();
				if(r.first()){
					return getProject(r);
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return null;
	}





	public int createDefaultNewProject(String sequenceId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into projects values (default,?,?) ;";
				PreparedStatement statement;
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,"default");
				statement.setInt(1,Integer.parseInt(sequenceId));
				statement.execute();
				query = "select currval('views_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                               
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					return resultSet.getInt(1);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return -1;
	}




	/**
	 * remove the connection between the track and the project,
	 * so remove the track from the view
	 * @param userId
	 * @param id
	 */
	public void removeConnection(int userId, int trackid) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from projectToTrack as t1 " +
				"where t1.track_id = ? and t1.project_id in " +
				"(select project_id from usertoproject " +
				"where user_id = ? );";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(2,userId);
				statement.setInt(1,trackid);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("removeConnection : "+e);
				this.endQuery(false);
			}
		}		
	}




	/**
	 * get the number of tracks belonging to 
	 * a project
	 * @param projectId
	 * @return
	 */
	public int tracksNumberUnderProject(int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select count(track_id) from projectToTrack where project_id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				ResultSet r = this.executeQuery(statement);
				if(r.first()){
					this.endQuery(true);
					return r.getInt(1);
				}

			} catch (SQLException e) {
				logger.error("tracksNumberUnderProject : "+e);
				this.endQuery(false);
			}
		}
		return 0;		
	}


	public void deleteProject(int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from projects where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("tracksNumberUnderProject : "+e);
				this.endQuery(false);
			}
		}
	}


	public void renameProject(int id, String input) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update projects set name = ? " +
				"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,input);
				statement.setInt(2,id);
				this.executeUpdate(statement);
				this.endQuery(true);
				Application.debug("renaming project ok");
			} catch (SQLException e) {
				logger.error("renameProject : "+e);
				this.endQuery(false);
			}
		}
	}


	public String getPublicKeyFromProjectId(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.public_key from publicProjects as t1 " +
				"where t1.project_id = ?; ";
				PreparedStatement statement;
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				ResultSet r = statement.executeQuery();
				if(r.first()){
					return r.getString(1);
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return null;
	}


	public void setProjectPublic(int id, boolean b) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update projects set isPublic = ? " +
				"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setBoolean(1,b);
				statement.setInt(2,id);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("setProjectPublic : "+e);
				this.endQuery(false);
			}
		}
	}


	public String generatePublicKey(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into publicProjects values (" +
				"? ,? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,id);
				RandomKey r = new RandomKey();
				String key = r.getRandom();
				statement.setString(2,key);
				this.executeUpdate(statement);
				return key;
			} catch (SQLException e) {
				logger.error("genratePublicKey "+e);
			}
		}
		this.endQuery(false);
		return null;
	}


	public boolean isProjectPublic(int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.isPublic from projects as t1 " +
				"where t1.id = ? limit 1; ";
				PreparedStatement statement;
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				ResultSet r = statement.executeQuery();
				if(r.first()){
					return r.getBoolean(1);
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return false;
	}


	public String getUserKeyFromProjectId(int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.key from users as t1 " +
						"inner join usertoproject as t2 on t1.id = t2.user_id " +
						"where t2.project_id = ? limit 1;";
				PreparedStatement statement;
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,projectId);
				ResultSet r = statement.executeQuery();
				if(r.first()){
					return r.getString(1);
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return null;
	}


	public boolean userHasProject(int userId, int projectId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select 1 from usertoproject as t1 " +
				"where t1.user_id = ? and t1.project_id = ? limit 1 ;";	
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, userId);
				statement.setInt(2, projectId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					return true;
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		this.endQuery(true);
		return false;
	}
}


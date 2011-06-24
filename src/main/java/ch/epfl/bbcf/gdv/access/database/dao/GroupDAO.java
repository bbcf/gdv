package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;

public class GroupDAO extends DAO<Group>{

	public GroupDAO(Connection connection) {
		super(connection);
	}


	private Group getGroup(ResultSet r){
		Group g = new Group();
		if(this.databaseConnected()){
			try {
				g.setId(r.getInt("id"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				g.setOwner(r.getInt("owner"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				g.setName(r.getString("name"));
			} catch (SQLException e) {
				logger.error(e);
			}
			return g;
		}
		return null;
	}

	private List<Group> getGroups(ResultSet r){
		List<Group> gs = new ArrayList<Group>();
		try {
			while(r.next()){
				gs.add(getGroup(r));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return gs;
	}




	/**
	 * get the list of group created by the user
	 * @param userId
	 * @return
	 */
	public List<Group> getGroupOwnedByUser(int userId) {
		List<Group> groups = new ArrayList<Group>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from groups where owner = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,userId);
				ResultSet resultSet = this.executeQuery(statement);
				groups = getGroups(resultSet);
				this.endQuery(true);
				return groups;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return groups;
	}
	/**
	 * get the list of groups the user is in
	 * @param userMail
	 * @return
	 */
	public List<Group> getGroupBelongingToUser(String userMail) {
		List<Group> groups = new ArrayList<Group>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from groups as t1 " +
						"inner join userToGroup as t2 on t1.id = t2.group_id " +
						"where t2.user_mail = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,userMail);
				ResultSet resultSet = this.executeQuery(statement);
				groups = getGroups(resultSet);
				this.endQuery(true);
				return groups;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return groups;
	}

	/**
	 * create a new group
	 * @param userId - the owner
	 * @param gn - the group name
	 */
	public int createNewGroup(int userId, String gn) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into groups values (default,?,?); ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,userId);
				statement.setString(2, gn);
				this.executeUpdate(statement);
				query = "select currval('groups_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                               
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					int groupId = resultSet.getInt(1);
					this.endQuery(true);
					return groupId;
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return -1;
	}

	/**
	 * add an user to a group
	 * @param groupId
	 * @param userMail
	 */
	public void addUserToGroup(int groupId, String userMail) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into userTogroup values (?,?); ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,userMail);
				statement.setInt(2, groupId);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
	}

	/**
	 * remove the user from the group
	 * @param id - the user id
	 * @param mail - the user mail
	 */
	public void removeUserFromGroup(int id, String mail) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from userTogroup " +
						"where user_mail = ? " +
						"and group_id = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,mail);
				statement.setInt(2, id);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
	}

	/**
	 * remove the group
	 * @param id
	 */
	public void removeGroup(int id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "delete from groups " +
						"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, id);
				this.executeUpdate(statement);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
	}


	/**
	 * share a project to a group
	 * @param projectId
	 * @param groupId
	 * @return
	 */
	public boolean shareProject(int projectId, int groupId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into groupToProject values (?,?);";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, groupId);
				statement.setInt(2, projectId);
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


	public List<Group> getGroupNameFromProjectIdandUserMail(int id,String mail) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from groups as t1 " +
						"inner join grouptoproject as t2 on t2.group_id = t1.id " +
						"inner join usertogroup as t3 on t3.group_id = t1.id " +
						"where t2.project_id = ? " +
						"and t3.user_mail = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, id);
				statement.setString(2, mail);
				ResultSet resultSet = this.executeQuery(statement);
				List<Group> groups = getGroups(resultSet);
				this.endQuery(true);
				return groups;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return new ArrayList<Group>();
	}

}

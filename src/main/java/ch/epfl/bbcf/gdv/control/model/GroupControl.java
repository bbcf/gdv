package ch.epfl.bbcf.gdv.control.model;

import java.util.List;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.GroupDAO;
import ch.epfl.bbcf.gdv.access.database.dao.UsersDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.UserSession;

public class GroupControl extends Control{

	public GroupControl(UserSession session) {
		super(session);
	}

	/**
	 * get the list of group created by the user
	 * @param userId
	 * @return
	 */
	public List<Group> getGroupOwnedByUser(int userId) {
		GroupDAO gdao = new GroupDAO(Connect.getConnection(session));
		return gdao.getGroupOwnedByUser(userId);
	}

	/**
	 * get the list of groups the user is in
	 * @param userMail
	 * @return
	 */
	public List<Group> getGroupBelongingToUser(String userMail) {
		GroupDAO gdao = new GroupDAO(Connect.getConnection(session));
		return gdao.getGroupBelongingToUser(userMail);
	}

	/**
	 * retrieve the list of users belonging to a group
	 * @param groupId
	 * @return
	 */
	public List<Users> getUserListFromGroupId(int groupId) {
		UsersDAO udao = new UsersDAO(Connect.getConnection(session));
		return udao.getUserListFromGroupId(groupId);
	}

	/**
	 * get the owner mail for a group
	 * @param id
	 * @return
	 */
	public String getUserMailOwnerFromGroupId(int groupId) {
		UsersDAO udao = new UsersDAO(Connect.getConnection(session));
		Users u = udao .getUserOwnerFromGroupId(groupId);
		return u.getMail();
	}

	/**
	 * create a new group 
	 * @param gn - the group name
	 */
	public void createNewGroup(String gn) {
		GroupDAO gdao = new GroupDAO(Connect.getConnection(session));
		gdao.createNewGroup(session.getUserId(),gn);
		
	}
	/**
	 * add a mail to this group
	 * @param groupId - the group
	 * @param userMail - the user mail
	 */
	public void addUserToGroup(int groupId, String userMail) {
		GroupDAO gdao = new GroupDAO(Connect.getConnection(session));
		gdao.addUserToGroup(groupId,userMail);
	}

	/**
	 * remove the user from the group
	 * @param id
	 * @param currentUser
	 */
	public void removeUserFromGroup(int id, String currentUser) {
		GroupDAO gdao = new GroupDAO(Connect.getConnection(session));
		gdao.removeUserFromGroup(id,currentUser);
	}

	/**
	 * remove the group
	 * @param id
	 */
	public void removeGroup(int id) {
		GroupDAO gdao = new GroupDAO(Connect.getConnection(session));
		gdao.removeGroup(id);
	}

}
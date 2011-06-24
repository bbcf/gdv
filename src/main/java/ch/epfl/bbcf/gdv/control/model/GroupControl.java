package ch.epfl.bbcf.gdv.control.model;

import java.sql.Connection;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.GroupDAO;
import ch.epfl.bbcf.gdv.access.database.dao.UsersDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;

public class GroupControl extends Control{


	/**
	 * get the list of group created by the user
	 * @param userId
	 * @return
	 */
	public static List<Group> getGroupOwnedByUser(int userId) {
		Connection conn = Conn.get();
		GroupDAO gdao = new GroupDAO(conn);
		List<Group> r = gdao.getGroupOwnedByUser(userId);
		return r;
	}

	/**
	 * get the list of groups the user is in
	 * @param userMail
	 * @return
	 */
	public static List<Group> getGroupBelongingToUser(String userMail) {
		GroupDAO gdao = new GroupDAO(Conn.get());
		return gdao.getGroupBelongingToUser(userMail);
	}

	/**
	 * retrieve the list of users belonging to a group
	 * @param groupId
	 * @return
	 */
	public static List<Users> getUserListFromGroupId(int groupId) {
		UsersDAO udao = new UsersDAO(Conn.get());
		return udao.getUserListFromGroupId(groupId);
	}
	/**
	 * retrieve the list of mails belonging to a group
	 * @param id
	 * @return
	 */
	public static List<String> getUserMailFromGroupId(int groupId) {
		UsersDAO udao = new UsersDAO(Conn.get());
		return udao.getUserMailFromGroupId(groupId);
	}
	/**
	 * get the owner mail for a group
	 * @param id
	 * @return
	 */
	public static String getUserMailOwnerFromGroupId(int groupId) {
		UsersDAO udao = new UsersDAO(Conn.get());
		Users u = udao .getUserOwnerFromGroupId(groupId);
		if(null!=u){
			return u.getMail();
		}
		return null;
	}



	/**
	 * create a new group 
	 * @param gn - the group name
	 */
	public static int createNewGroup(String gn,int userId) {
		GroupDAO gdao = new GroupDAO(Conn.get());
		return gdao.createNewGroup(userId,gn);

	}
	/**
	 * add a mail to this group
	 * @param groupId - the group
	 * @param userMail - the user mail
	 */
	public static void addUserToGroup(int groupId, String userMail) {
		GroupDAO gdao = new GroupDAO(Conn.get());
		gdao.addUserToGroup(groupId,userMail);
	}

	/**
	 * remove the user from the group
	 * @param id
	 * @param currentUser
	 */
	public static void removeUserFromGroup(int id, String mail) {
		GroupDAO gdao = new GroupDAO(Conn.get());
		gdao.removeUserFromGroup(id,mail);
	}

	/**
	 * remove the group
	 * @param id
	 */
	public static void removeGroup(int id) {
		GroupDAO gdao = new GroupDAO(Conn.get());
		gdao.removeGroup(id);
	}
	/**
	 * check if the user have group or is in group
	 * @return
	 */
	public static  boolean checkIfGroupsForUser(Users user) {
		GroupDAO gdao = new GroupDAO(Conn.get());
		if( (getGroupBelongingToUser(user.getMail()).isEmpty()) &&
				(getGroupOwnedByUser(user.getId()).isEmpty()) ){
			return false;
		}
		return true;
	}

	/**
	 * share a project to a group
	 * @param projectId
	 * @param groupId
	 * @return
	 */
	public static boolean shareProject(int projectId, int groupId) {
		GroupDAO gdao = new GroupDAO(Conn.get());
		return gdao.shareProject(projectId,groupId);
	}


}

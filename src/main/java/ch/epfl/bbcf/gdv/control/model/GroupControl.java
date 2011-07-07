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
		GroupDAO gdao = new GroupDAO();
		List<Group> r = gdao.getGroupOwnedByUser(userId);
		gdao.release();
		return r;
	}

	/**
	 * get the list of groups the user is in
	 * @param userMail
	 * @return
	 */
	public static List<Group> getGroupBelongingToUser(String userMail) {
		GroupDAO gdao = new GroupDAO();
		List<Group> gs = gdao.getGroupBelongingToUser(userMail);
		gdao.release();
		return gs;
	}

	/**
	 * retrieve the list of users belonging to a group
	 * @param groupId
	 * @return
	 */
	public static List<Users> getUserListFromGroupId(int groupId) {
		UsersDAO udao = new UsersDAO();
		List<Users> us = udao.getUserListFromGroupId(groupId);
		udao.release();
		return us;
	}
	/**
	 * retrieve the list of mails belonging to a group
	 * @param id
	 * @return
	 */
	public static List<String> getUserMailFromGroupId(int groupId) {
		UsersDAO udao = new UsersDAO();
		List<String> us = udao.getUserMailFromGroupId(groupId);
		udao.release();
		return us;
	}
	/**
	 * get the owner mail for a group
	 * @param id
	 * @return
	 */
	public static String getUserMailOwnerFromGroupId(int groupId) {
		UsersDAO udao = new UsersDAO();
		Users u = udao .getUserOwnerFromGroupId(groupId);
		if(null!=u){
			udao.release();
			return u.getMail();
		}
		udao.release();
		return null;
	}



	/**
	 * create a new group 
	 * @param gn - the group name
	 */
	public static int createNewGroup(String gn,int userId) {
		GroupDAO gdao = new GroupDAO();
		int i = gdao.createNewGroup(userId,gn);
		gdao.release();
		return i;

	}
	/**
	 * add a mail to this group
	 * @param groupId - the group
	 * @param userMail - the user mail
	 */
	public static void addUserToGroup(int groupId, String userMail) {
		GroupDAO gdao = new GroupDAO();
		gdao.addUserToGroup(groupId,userMail);
		gdao.release();
	}

	/**
	 * remove the user from the group
	 * @param id
	 * @param currentUser
	 */
	public static void removeUserFromGroup(int id, String mail) {
		GroupDAO gdao = new GroupDAO();
		gdao.removeUserFromGroup(id,mail);
		gdao.release();
	}

	/**
	 * remove the group
	 * @param id
	 */
	public static void removeGroup(int id) {
		GroupDAO gdao = new GroupDAO();
		gdao.removeGroup(id);
		gdao.release();
	}
	/**
	 * check if the user have group or is in group
	 * @return
	 */
	public static  boolean checkIfGroupsForUser(Users user) {
		GroupDAO gdao = new GroupDAO();
		if( (getGroupBelongingToUser(user.getMail()).isEmpty()) &&
				(getGroupOwnedByUser(user.getId()).isEmpty()) ){
			gdao.release();
			return false;
		}
		gdao.release();
		return true;
	}

	/**
	 * share a project to a group
	 * @param projectId
	 * @param groupId
	 * @return
	 */
	public static boolean shareProject(int projectId, int groupId) {
		GroupDAO gdao = new GroupDAO();
		boolean is = gdao.shareProject(projectId,groupId);
		gdao.release();
		return is;
	}


}

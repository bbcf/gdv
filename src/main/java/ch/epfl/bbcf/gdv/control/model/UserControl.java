package ch.epfl.bbcf.gdv.control.model;


import java.io.File;
import java.io.IOException;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.UsersDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;

/**
 * Contains methods controlling user management
 * @author Yohan Jarosz
 *
 */
public class UserControl extends Control{

	public UserControl(UserSession session) {
		super(session);
	}

	public int createNewUser(Users user) {
		Application.info("create new user :\n"+user.toString());
		UsersDAO dao = new UsersDAO(Connect.getConnection(session));
		int userId = dao.createUser(user);
		if(userId!=-1){
			File logfile = new File(Configuration.getGdvWorkingDir()+"/log/"+userId+".log");
			try {
				if(logfile.createNewFile()){
					Application.debug("create user and log for "+user.getMail());
					return userId;
				}
			} catch (IOException e) {
				Application.error(e);
			}
		}
		return userId;
	}
	public int createNewUser(String email, String name, String firstname,
			String title, String phone, String office,String type) {
		Users user = new Users(email,name,firstname,title,phone,office,type);
		return createNewUser(user);
	}

	public boolean sameMailExist(String mail) {
		UsersDAO dao = new UsersDAO(Connect.getConnection(session));
		return dao.emailExist(mail);
	}

	public Users getUserByProjectId(int projectId) {
		UsersDAO dao = new UsersDAO(Connect.getConnection(session));
		return dao.getUserByProjectId(projectId);
	}

	public Users getuserByMail(String mail) {
		UsersDAO dao = new UsersDAO(Connect.getConnection(session));
		return dao.getUserByEmail(mail);
	}
}

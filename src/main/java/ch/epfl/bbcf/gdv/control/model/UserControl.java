package ch.epfl.bbcf.gdv.control.model;


import java.io.File;
import java.io.IOException;
import java.util.List;


import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.UsersDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

/**
 * Contains methods controlling user management
 * @author Yohan Jarosz
 *
 */
public class UserControl extends Control{


	public static int createNewUser(Users user) {
		Application.info("create new user :\n"+user.toString());
		UsersDAO dao = new UsersDAO(Conn.get());
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
	public static int createNewUser(String email, String name, String firstname,
			String title, String phone, String office,String type) {
		Users user = new Users(email,name,firstname,title,phone,office,type);
		return createNewUser(user);
	}

	public static boolean sameMailExist(String mail) {
		UsersDAO dao = new UsersDAO(Conn.get());
		return dao.emailExist(mail);
	}

	public static Users getUserByProjectId(int projectId) {
		UsersDAO dao = new UsersDAO(Conn.get());
		return dao.getUserByProjectId(projectId);
	}

	public static Users getUserById(int userId) {
		UsersDAO dao = new UsersDAO(Conn.get());
		return dao.getUserById(userId);
	}
	
	public static Users getuserByMail(String mail) {
		UsersDAO dao = new UsersDAO(Conn.get());
		return dao.getUserByEmail(mail);
	}

	public static boolean checkUserKey(String key, String mail) {
		UsersDAO dao = new UsersDAO(Conn.get());
		Users u = dao.getUserByEmail(mail);
		return u.getKey().equalsIgnoreCase(key);
	}

	public static Users getuserByMailAndPass(String mail, String pass) {
		UsersDAO dao = new UsersDAO(Conn.get());
		return dao.getUserByEmailAndPass(mail,pass);
	}

	public static List<Users> getUserFromTrackId(int trackId) {
		UsersDAO dao = new UsersDAO(Conn.get());
		return dao.getUserFromTrackId(trackId);
	}

	public static boolean checkUserAuthorizedToConfigureTrack(Integer trackId) {
		// TODO Auto-generated method stub
		return true;
	}

	public static boolean checkUserAuthorizedToViewImage(Integer imageId) {
		// TODO Auto-generated method stub
		return true;
	}
	
}

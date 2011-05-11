package ch.epfl.bbcf.gdv.config;


import java.util.Date;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.WebResponse;
import org.json.JSONException;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.UsersDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.control.model.UserControl;


/**
 * Represent the session in the application
 * @author Yohan Jarosz
 *
 */
public class UserSession extends AuthenticatedWebSession{


	private static final long serialVersionUID = 8871190803192332L;
	/** Tracks the status of the lengthy process of uploading. 
	 * These are declared volatile to make sure the JVM writes the value
	 * of the flag from the ImportThread to the Wicket thread.
	 */
	//private volatile ArrayList<Track> dasTracks;
	//private volatile ArrayList<Boolean> featureUpdated;




	private int user = 0;

	public UserSession(Request request) {
		super(request);
	}	  
	
	

	/**
	 * basic method for authentification
	 * this can be a normal identification :
	 * email from user + tequila type
	 * or obfuscatedkey + Configuration.getGdv_types_access() type
	 */
	
	@Override
	public final boolean authenticate(final String email, final String type){
		Session.set(this);
		if (user == 0){
			if(type.equalsIgnoreCase("tequila")){
				UsersDAO dao = new UsersDAO(Connect.getConnection(this));
				if(dao.emailExist(email)){
					Users person = dao.getUserByEmail(email);
					user = person.getId();
					this.signIn(email, type);
					Application.info("sign in",user);
				}
			} 
		}
		return user != 0;
	}




	public Users getUser(){
		UsersDAO dao = new UsersDAO(Connect.getConnection(this));
		return dao.getUserById(user);

	}
	public int getUserId(){
		return user;
	}
	public void setUser(Users user){
		this.user = user.getId();
	}

	public void logOut(){
		this.user = 0;
		this.signOut();
	}




	//	@Override
	//	public Roles getRoles() {
	//		if(isSignedIn()){
	//			return this.getUser().getRole();
	//		}
	//		return new Roles("NOT_LOGGED");
	//	}





	public void finalize() {
		Application.warn("FINALIZE "+this);
		Connect.removeConnection(this);
		Application.removeLogger(this.getUserId());
	}

	public boolean hasAnyRole(Roles roles) {
		return roles.equals(getRoles());
	}
	@Override
	public Roles getRoles() {
		//Application.debug("session.getRoles()");
		if(this.user==4){
			return new Roles(Roles.ADMIN);
		}
		else {
			return new Roles(Roles.USER);
		}
	}


	public boolean isAdmin() {
		UsersDAO dao = new UsersDAO(Connect.getConnection(this));
		return dao.isAdmin(user);
	}









}
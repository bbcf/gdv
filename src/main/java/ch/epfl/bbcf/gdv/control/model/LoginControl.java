package ch.epfl.bbcf.gdv.control.model;

import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;

public class LoginControl extends Control{

	public LoginControl(UserSession session) {
		super(session);
	}

	public boolean logAlternativeUser(String type, String key) {
		if(Configuration.getGdv_types_access().contains(type)){
			return session.authenticate(key,type);
		}
		return false;
	}
}

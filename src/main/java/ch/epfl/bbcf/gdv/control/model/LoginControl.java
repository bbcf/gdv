package ch.epfl.bbcf.gdv.control.model;

import javax.servlet.http.Cookie;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;

public class LoginControl extends Control{

	public LoginControl(UserSession session) {
		super(session);
	}

//	public boolean logAlternativeUser(String type, String key) {
//		if(Configuration.getGdv_types_access().contains(type)){
//			return session.authenticate(key,type);
//		}
//		return false;
//	}

	public void logOut(WebPage page, boolean finishSession) {
		removeCookie(page);
		session.signOut();
		session.logOut();
		if(finishSession){
			session.invalidateNow();
		}

	}

	private void removeCookie(WebPage page) {
		Cookie cook = ((WebRequest) page.getRequestCycle().getRequest()).getCookie("TEQUILA_KEY");
		if(null!=cook){
			cook.setMaxAge(0);
			((WebResponse) page.getRequestCycle().getResponse()).addCookie(cook);
		}

	}
}

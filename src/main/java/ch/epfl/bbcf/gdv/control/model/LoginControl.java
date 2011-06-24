package ch.epfl.bbcf.gdv.control.model;

import javax.servlet.http.Cookie;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;

public class LoginControl extends Control{


	public static void logOut(WebPage page, boolean finishSession,UserSession session) {
		removeCookie(page);
		session.signOut();
		session.logOut();
		if(finishSession){
			session.invalidateNow();
		}

	}

	private static void removeCookie(WebPage page) {
		Cookie cook = ((WebRequest) page.getRequestCycle().getRequest()).getCookie("TEQUILA_KEY");
		if(null!=cook){
			cook.setMaxAge(0);
			((WebResponse) page.getRequestCycle().getResponse()).addCookie(cook);
		}

	}
}

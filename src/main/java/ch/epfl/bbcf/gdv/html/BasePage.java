package ch.epfl.bbcf.gdv.html;

import javax.servlet.http.Cookie;

import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.WebRequest;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;

public class BasePage extends WebPage{
	
	public BasePage(final PageParameters p){
		for(String cp : Configuration.getGDVCSSFiles()){
			add(CSSPackageResource.getHeaderContribution(cp));
		}
		Users user = ((UserSession)getSession()).getUser();
		if(null!=user && Configuration.getGdv_types_access().contains(user.getType())){
			redirectToInterceptPage(new AlternativeProjectPage(new PageParameters()));
		}
		add(new MenuPage("menu",Configuration.getNavigationLinks()));
		
	}
}

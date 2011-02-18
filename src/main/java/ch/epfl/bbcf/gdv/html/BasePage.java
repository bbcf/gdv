package ch.epfl.bbcf.gdv.html;

import javax.servlet.http.Cookie;

import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

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

		Image gdv = new Image("gdv_logo");
		gdv.add(new SimpleAttributeModifier("src",Configuration.getGdv_Images_url()+"/logo_gdv.jpg"));
		add(gdv);
		Image epfl = new Image("epfl_logo");
		epfl.add(new SimpleAttributeModifier("src",Configuration.getGdv_Images_url()+"/epfl-logo.jpeg"));
		add(epfl);
		Image bbcf = new Image("bbcf_logo");
		bbcf.add(new SimpleAttributeModifier("src",Configuration.getGdv_Images_url()+"/logo-bbcf.png"));
		add(bbcf);
		Image sybit = new Image("sybit_logo");
		sybit.add(new SimpleAttributeModifier("src",Configuration.getGdv_Images_url()+"/logo_sybit.jpg"));
		add(sybit);

		add(new MenuPage("menu",Configuration.getNavigationLinks()));

	}

	@Override
	protected void configureResponse() {
		super.configureResponse();
		WebResponse response = getWebRequestCycle().getWebResponse();
		response.setHeader("Cache-Control",
		"no-cache, max-age=0,must-revalidate, no-store");
	}
}

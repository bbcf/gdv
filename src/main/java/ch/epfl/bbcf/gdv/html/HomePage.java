package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.LoginControl;

public class HomePage extends BasePage{

	public HomePage(PageParameters p) {
		super(p);
		Users user = ((UserSession)getSession()).getUser();
		if(null!=user && Configuration.getGdv_types_access().contains(user.getType())){
			LoginControl lc = new LoginControl((UserSession)getSession());
			redirectToInterceptPage(new AlternativeProjectPage(new PageParameters()));
		}
		Application.debug("Home page", ((UserSession)getSession()).getUserId());
	}

}

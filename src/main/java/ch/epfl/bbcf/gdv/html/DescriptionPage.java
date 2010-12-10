package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.PageParameters;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;

public class DescriptionPage extends BasePage{

	public DescriptionPage(PageParameters p) {
		super(p);
		Application.debug("Home page", ((UserSession)getSession()).getUserId());
	}

}

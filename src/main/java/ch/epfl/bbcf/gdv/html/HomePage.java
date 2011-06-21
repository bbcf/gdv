package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
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
//		if(null!=user && Configuration.getGdv_types_access().contains(user.getType())){
//			LoginControl lc = new LoginControl((UserSession)getSession());
//			redirectToInterceptPage(new AlternativeProjectPage(new PageParameters()));
//		}
			add(JavascriptPackageResource.getHeaderContribution(Configuration.getJavascriptUrl()+"/js/gdv.js"));
			add(JavascriptPackageResource.getHeaderContribution(Configuration.getJavascriptUrl()+"/jslib/dojo/dojo.js"));
			add(JavascriptPackageResource.getHeaderContribution(Configuration.getJavascriptUrl()+"/jslib/dojo/jbrowse_dojo.js"));
			add(CSSPackageResource.getHeaderContribution(Configuration.getJavascriptUrl()+"/jslib/dojox/image/resources/image.css"));
				
		//adding final javascript
		final String jsControl = "initGDV_prez();";

		add(new AbstractBehavior() {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.renderJavascript(jsControl,"js_control");
			}
		}); 
		
		
		
		
		
		
		
	}

}

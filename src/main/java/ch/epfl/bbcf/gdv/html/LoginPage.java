package ch.epfl.bbcf.gdv.html;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.Cookie;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.authentication.TequilaAuthentication;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.tequila.client.model.TequilaPrincipal;

public class LoginPage extends BasePage{

	private TequilaPrincipal principal;

	public LoginPage(PageParameters p) throws IOException {
		super(p);
		if(((UserSession)getSession()).getUserId()==0){
			String uri = getRequest().getQueryString();
		// 	System.out.println("TEQUILA  : "+uri);
			boolean getnewkey=false;
			if(null!=uri && uri.equalsIgnoreCase("new")){
				getnewkey = true;
			}
			
			//Application.debug("start logging process");
			String key = null;
			String query = this.getRequest().getQueryString();
			if(query!=null){
				String[] params = query.split("=");
				if(params.length>0){
					for(int i=0;i<params.length;i+=2){
						if(params[i].equalsIgnoreCase("key")){
							key = params[i+1];
						}
					}
				}
			}
			
			//Application.debug("key="+key);
			//zero key or not valid one
			if(null==key || getnewkey){
				
				String requestKey = TequilaAuthentication._tequilaService.createRequest(
						TequilaAuthentication._clientConfig, 
						Configuration.getGdv_appli_proxy()+"/login");//+Configuration.GDV_MOUNT_PATH+"/login");
				Cookie cook = new Cookie("TEQUILA_KEY",requestKey);
				cook.setMaxAge(160);
				((WebResponse)getRequestCycle().getResponse()).addCookie(cook);
				setResponsePage(new RedirectPage(
						"https://" +TequilaAuthentication._clientConfig.getHost() +
						"/cgi-bin/tequila/requestauth?requestkey=" +requestKey));
//				Application.debug("redirect on "+Configuration.PROXY_URL+"/gdvprotected");
//				setResponsePage(new RedirectPage(Configuration.PROXY_URL+"/gdvprotected"));
			}
			else {
				try {
					//Application.debug("authentifying");
					principal = TequilaAuthentication.validateKey(key);
				} catch (Exception e) {
					Application.error("tequila error (principal): "+e);
				}
				if(null==principal){
					//Application.debug("wrong key->redirect");
					setResponsePage(new RedirectPage(Configuration.getGdv_appli_proxy()+"/login?new"));
				}

				else {
					String email=null;
					String name=null;
					String firstname=null;
					String title=null;
					String phone =null;
					String office=null;
					Iterator<String> it = principal.getAttributes().keySet().iterator();
					while(it.hasNext()){
						String str = it.next();
						if(str.equalsIgnoreCase("name")){
							name = principal.getAttribute(str);
						}
						else if(str.equalsIgnoreCase("email")){
							email = principal.getAttribute(str);
						}
						else if(str.equalsIgnoreCase("firstname")){
							firstname = principal.getAttribute(str);
						}
						else if(str.equalsIgnoreCase("title")){
							title = principal.getAttribute(str);
						}
						else if(str.equalsIgnoreCase("phone")){
							phone = principal.getAttribute(str);
						}
						else if(str.equalsIgnoreCase("office")){
							office = principal.getAttribute(str);
						}
					}
					if(null!=email){
						UserSession session = (UserSession)getSession();
						//Application.debug("authentify in gdv");
						if(session.authenticate(email,"tequila")){
							Application.info("login : "+email);
							setResponsePage(new HomePage(new PageParameters()));
						}
						else{
							//CREATE USER
							UserControl controller = new UserControl((UserSession)getSession());
							if(controller.createNewUser(email,name,firstname,title,phone,office,"tequila")!=-1){
								setResponsePage(new HomePage(new PageParameters()));
							}
							//TODO ERROR PAGE
							//setResponsePage(new RedirectPage(Configuration.PROXY_URL+"/protected"));
						}

					}

				}
			}
		} else {
			setResponsePage(new HomePage(new PageParameters()));
		}
	}


}

package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.LoginControl;
import ch.epfl.bbcf.gdv.html.utility.FormChecker;



/**
 * login page for project form htc3cseq
 * url must be formed like : http://GDV_URL/hts3cseq/'obfuscatedString'
 *
 */
public class MagicPasswordPage extends WebPage{

	
	public MagicPasswordPage(PageParameters p){
		
		
		String type = p.getString("0");
		String key = p.getString("1");
		Application.debug("pass page "+type+" "+key);
		LoginControl lc = new LoginControl((UserSession)getSession());
		boolean b = lc.logAlternativeUser(type,key);
		if(b){
			setResponsePage(AlternativeProjectPage.class);
		} else {
			throw new AbortWithHttpStatusException(403,true);
		}
		
	}
	
}

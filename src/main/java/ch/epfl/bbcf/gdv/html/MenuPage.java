package ch.epfl.bbcf.gdv.html;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebResponse;

import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.html.utility.MenuElement;

public class MenuPage extends Panel{


	public MenuPage(String id,List<MenuElement>links) {
		super(id);

		Label nav = new Label("nav","Navigation");
		add(nav);

		ListView<MenuElement> entries = new ListView<MenuElement>("nav_items",links){
			protected void populateItem(ListItem<MenuElement> item) {
				final MenuElement el = item.getModelObject();
				Link link = new Link("link"){
					@Override
					public void onClick() {
						if(el.isImportProject()){
							String pid = Integer.toString(el.getProjectId());
							Cookie cook = new Cookie("PROJECT_ID",pid);
							cook.setMaxAge(160);
							((WebResponse)getRequestCycle().getResponse()).addCookie(cook);
							((UserSession)getSession()).logOut();
							((UserSession)getSession()).signOut();
							((UserSession)getSession()).invalidateNow();
						} 
						setResponsePage(el.getPage());
					}
				};
				link.add(new Label("label",el.getName()));
				item.add(link);
			}
		};
		add(entries);
	}

}

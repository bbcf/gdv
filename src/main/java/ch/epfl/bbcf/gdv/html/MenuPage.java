package ch.epfl.bbcf.gdv.html;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.border.BoxBorder;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

public class MenuPage extends Panel{

	
	public MenuPage(String id,List<Class>links) {
		super(id);
		
		Label nav = new Label("nav","Navigation");
		add(nav);
		
		ListView<Class> entries = new ListView<Class>("nav_items",links){
			protected void populateItem(ListItem<Class> item) {
				Class page = item.getModelObject();
				item.add(new PageLink("link",page));
			}
		};
		add(entries);
	}

}

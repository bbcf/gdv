package ch.epfl.bbcf.gdv.html;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

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

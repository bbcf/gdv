package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.border.BoxBorder;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.panel.Panel;

public class MenuPage extends Panel{

	
	public MenuPage(String id) {
		super(id);
		WebMarkupContainer navigation_menu = new WebMarkupContainer("navigation_menu");
		add(navigation_menu);
	}

}

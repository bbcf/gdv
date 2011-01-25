package ch.epfl.bbcf.gdv.html.utility;


import org.apache.wicket.PageMap;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;

import ch.epfl.bbcf.gdv.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.html.PDFPopupPage;
import ch.epfl.bbcf.gdv.html.PNGPopupPage;

public class Sidebar extends Panel{

	public Sidebar(String id) {
		super(id);
		//add(new MenuElement("testelem",new Model(),new Label("label","test"),new Image("image")));



		PopupSettings popupSettings = new PopupSettings(PageMap.forName("popuppagemap")).setHeight(
				700).setWidth(700);
		// Popup example
		add(new BookmarkablePageLink("gFeatTest", PDFPopupPage.class).setPopupSettings(popupSettings));
		//add(new BookmarkablePageLink("gFeatTest", PNGPopupPage.class).setPopupSettings(popupSettings));
	}
}
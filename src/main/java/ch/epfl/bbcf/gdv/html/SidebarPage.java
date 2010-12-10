package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;

import ch.epfl.bbcf.gdv.html.utility.Sidebar;

public class SidebarPage extends WebPage{

	public SidebarPage(final PageParameters p){
		add(new Sidebar("sidebar"));
		
	}
}

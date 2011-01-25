package ch.epfl.bbcf.gdv.html.utility;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class MenuElement implements Serializable{
	
	private Class<WebPage> page;
	private String name;

	public MenuElement(Class page,String name) {
		this.setPage(page);
		this.setName(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPage(Class<WebPage> page) {
		this.page = page;
	}

	public Class<WebPage> getPage() {
		return page;
	}
	
}

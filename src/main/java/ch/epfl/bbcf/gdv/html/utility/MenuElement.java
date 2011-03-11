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

	//small hack on the menu element 
	//if the use click on import in my profile when he is 
	//on a public view
	private boolean importProject;
	private int projectId;
	
	
	public MenuElement(Class page,String name,boolean importProject,int id) {
		this.setPage(page);
		this.setName(name);
		this.importProject=importProject;
		this.projectId = id;
	}
	
	public MenuElement(Class page,String name) {
		this.setPage(page);
		this.setName(name);
		this.importProject=false;
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

	public void setImportProject(boolean importProject) {
		this.importProject = importProject;
	}

	public boolean isImportProject() {
		return importProject;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public int getProjectId() {
		return projectId;
	}
	
}

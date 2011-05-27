package ch.epfl.bbcf.gdv.html.utility;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;

import ch.epfl.bbcf.gdv.html.wrapper.ProjectWrapper;

public class GroupModalWindow  extends ModalWindow{

	private ProjectWrapper project;
	
	public GroupModalWindow(String id, IModel<?> model) {
		super(id, model);
	}

	public GroupModalWindow(String id) {
		super(id);
	}

	public void setProject(ProjectWrapper project) {
		this.project = project;
	}

	public ProjectWrapper getProject() {
		return project;
	}

	

}

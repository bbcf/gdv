package ch.epfl.bbcf.gdv.html.utility;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

public class CustModalWindow extends ModalWindow{

	private String speciesId;
	private int userId;
	private int projectId;
	private Label label;
	private boolean admin;


	public CustModalWindow(String id) {
		super(id);
	}
	public CustModalWindow(String id, IModel<?> model) {
		super(id, model);
	}

	public void setParams(int speciesId,int userId,int projectId,boolean admin){
		this.setSpeciesID(Integer.toString(speciesId));
		this.setUserId(userId);
		this.setProjectId(projectId);
		this.admin = admin;
	}
	/**
	 * @param version the version to set
	 */
	public void setSpeciesID(String version) {
		this.speciesId = version;
	}
	/**
	 * @return the version
	 */
	public String getSpeciesId() {
		return speciesId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}
	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	/**
	 * @return the projectId
	 */
	public int getProjectId() {
		return projectId;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(Label label) {
		this.label = label;
	}
	/**
	 * @return the label
	 */
	public Label getLabel() {
		return label;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public boolean isAdmin() {
		return admin;
	}

}

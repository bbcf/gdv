package ch.epfl.bbcf.gdv.access.gdv_prod.pojo;

import java.io.Serializable;

public class Project implements Serializable	{

	private int id;
	private int sequenceId;
	private String description;

	
	public Project(int id) {
		this.id = id;
	}
	public Project() {
	}
	/**
	 * @param viewId the viewId to set
	 */
	public void setId(int viewId) {
		this.id = viewId;
	}
	/**
	 * @return the viewId
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param genomeId the genomeId to set
	 */
	public void setSequenceId(int genomeId) {
		this.sequenceId = genomeId;
	}
	/**
	 * @return the genomeId
	 */
	public int getSequenceId() {
		return sequenceId;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
}

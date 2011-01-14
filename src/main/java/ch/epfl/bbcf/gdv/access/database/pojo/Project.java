package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class Project implements Serializable	{

	private int id;
	private int curSequenceId;
	private Species species;
	private String name;

	
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
	public void setCurrentSequenceId(int genomeId) {
		this.curSequenceId = genomeId;
	}
	/**
	 * @return the genomeId
	 */
	public int getSequenceId() {
		return curSequenceId;
	}
	/**
	 * @param description the description to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getName() {
		return name;
	}
	
	public void setSpecies(Species species) {
		this.species = species;
	}
	public Species getSpecies() {
		return species;
	}
}

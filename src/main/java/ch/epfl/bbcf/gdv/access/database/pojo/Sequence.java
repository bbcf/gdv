package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class Sequence implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private int jbrowsoRId;
	private String type;
	private String name;
	private int speciesId;
	/**
	 * @param geneRepId the geneRepId to set
	 */
	public void setId(final int id) {
		this.id = id;
	}
	/**
	 * @return the geneRepId
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param jbrowsoRId the jbrowsoRId to set
	 */
	public void setJbrowsoRId(final int jbrowsoRId) {
		this.jbrowsoRId = jbrowsoRId;
	}
	/**
	 * @return the jbrowsoRId
	 */
	public int getJbrowsoRId() {
		return jbrowsoRId;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setSpeciesId(int speciesId) {
		this.speciesId = speciesId;
	}
	public int getSpeciesId() {
		return speciesId;
	}
}

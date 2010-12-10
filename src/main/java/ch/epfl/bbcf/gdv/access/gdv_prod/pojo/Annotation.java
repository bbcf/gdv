package ch.epfl.bbcf.gdv.access.gdv_prod.pojo;

import java.io.Serializable;
import java.sql.Date;

public class Annotation implements Serializable{
	private int id;
	private String name;
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}

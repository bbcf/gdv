package ch.epfl.bbcf.gdv.access.gdv_prod.pojo;

import java.io.Serializable;

public class UserInput implements Serializable{
	
	private int id;
	private String file;
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
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}
	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

}

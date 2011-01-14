package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class Input implements Serializable{
	
	private int id;
	private String md5;
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
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	/**
	 * @return the file
	 */
	public String getMd5() {
		return md5;
	}

}

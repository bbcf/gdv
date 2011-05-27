package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class Status implements Serializable{

	public static final int SUCCES = 1;
	public static final int ERROR = 0;
	public static final int RUNNING = 2;
	
	private int id;
	private String status;
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
}

package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class Type implements Serializable{

	private int id;
	private String name;

	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}

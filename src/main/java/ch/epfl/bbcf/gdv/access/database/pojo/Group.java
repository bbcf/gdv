package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class Group implements Serializable{

	private int id;
	private int owner;
	private String name;
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setOwner(int owner) {
		this.owner = owner;
	}
	public int getOwner() {
		return owner;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}

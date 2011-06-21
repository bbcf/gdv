package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class Style implements Serializable{

	private int id;
	private String style;
	
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getStyle() {
		return style;
	}
}

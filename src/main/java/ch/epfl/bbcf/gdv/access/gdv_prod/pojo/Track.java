package ch.epfl.bbcf.gdv.access.gdv_prod.pojo;

import java.io.Serializable;

public class Track implements Serializable{

	private int id;
	private String name;
	private String parameters;
	private String filetype;
	private boolean always;
	private String status;
	
	public Track(){
		id=-1;
		name="";
		parameters="";
		filetype="";
		always=false;
		status="";
	}
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	/**
	 * @return the parameters
	 */
	public String getParameters() {
		return parameters;
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
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
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
	@Override
	public boolean equals(Object o){
		if(o instanceof Track){
			Track t = (Track)o;
			return this.id==t.id;
		}
		return false;
		
	}
	
	/**
	 * @param filetype the filetype to set
	 */
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	/**
	 * @return the filetype
	 */
	public String getFiletype() {
		return filetype;
	}
	/**
	 * @param always the always to set
	 */
	public void setAlways(boolean always) {
		this.always = always;
	}
	/**
	 * @return the always
	 */
	public boolean isAlways() {
		return always;
	}
}

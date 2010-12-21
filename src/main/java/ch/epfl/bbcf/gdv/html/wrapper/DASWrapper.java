package ch.epfl.bbcf.gdv.html.wrapper;

import java.io.Serializable;

public class DASWrapper implements Serializable{

	
	private String mapmaster;
	private String name;
	public DASWrapper(String mapmaster, String desc) {
		this.mapmaster = mapmaster;
		this.name = desc;
	}
	/**
	 * @param mapmaster the mapmaster to set
	 */
	public void setMapmaster(String mapmaster) {
		this.mapmaster = mapmaster;
	}
	/**
	 * @return the mapmaster
	 */
	public String getMapmaster() {
		return mapmaster;
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

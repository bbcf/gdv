package ch.epfl.bbcf.gdv.html.utility;

import java.io.Serializable;

public class SelectOption implements Serializable{

	private int key;
	private String value;

	public SelectOption(int key, String value) {
		this.setKey(key);
		this.setValue(value);
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(int key) {
		this.key = key;
	}

	/**
	 * @return the key
	 */
	public int getKey() {
		return key;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	public String toString(){
		return key+" "+value;
	}
}

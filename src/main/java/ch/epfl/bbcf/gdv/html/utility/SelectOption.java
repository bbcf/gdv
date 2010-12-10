package ch.epfl.bbcf.gdv.html.utility;

import java.io.Serializable;

public class SelectOption implements Serializable{

	private String key;
	private String value;

	public SelectOption(String key, String value) {
		this.setKey(key);
		this.setValue(value);
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
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
}

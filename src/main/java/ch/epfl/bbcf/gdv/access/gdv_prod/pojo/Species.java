package ch.epfl.bbcf.gdv.access.gdv_prod.pojo;

import java.io.Serializable;
import java.util.List;

public class Species implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String taxId;
	private List<String> names;
	
	/**
	 * @param taxId the taxId to set
	 */
	public void setTaxId(final String taxId) {
		this.taxId = taxId;
	}
	/**
	 * @return the taxId
	 */
	public String getTaxId() {
		return taxId;
	}
	/**
	 * @param names the names to set
	 */
	public void setNames(final List<String> names) {
		this.names = names;
	}
	/**
	 * @return the names
	 */
	public List<String> getNames() {
		return names;
	}
}

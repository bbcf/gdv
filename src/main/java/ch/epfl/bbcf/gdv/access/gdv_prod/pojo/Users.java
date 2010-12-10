package ch.epfl.bbcf.gdv.access.gdv_prod.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.Roles;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

/**
 * Represent an User in the database
 * @author Yohan Jarosz
 *
 */
public class Users implements Serializable{
	
	
	private static final long serialVersionUID = 4231877656093703622L;
	private int id;
	protected String mail;
	
	private String userDirectory;
	private String filesDirectory;
	private String name;
	private String firstname;
	private String title;
	private String phone;
	private String office;
	private String type;//tequila,other,...
	
	

	public Users(String email, String name, String firstname, String title,
			String phone, String office,String type) {
		this.mail = email;
		this.setName(name);
		this.setFirstname(firstname);
		this.setTitle(title);
		this.setPhone(phone);
		this.setOffice(office);
		this.type = type;
	}

	public Users() {
	}

	/**
	 * @param userDirectory the userDirectory to set
	 */
	public void setUserDirectory(String userDirectory) {
		this.userDirectory = userDirectory;
	}
	/**
	 * @return the userDirectory
	 */
	public String getUserDirectory() {
		return userDirectory;
	}
	/**
	 * @param mail the mail to set
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}
	/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
//		this.setUserDirectory(Configuration.WORKING_DIRECTORY+"/users/"+id);
//		this.setFilesDirectory(Configuration.WORKING_DIRECTORY+"/users/"+id+"/files");
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param filesDirectory the filesDirectory to set
	 */
	public void setFilesDirectory(String filesDirectory) {
		this.filesDirectory = filesDirectory;
	}
	/**
	 * @return the filesDirectory
	 */
	public String getFilesDirectory() {
		return filesDirectory;
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
	 * @param firstname the firstname to set
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	/**
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * @param office the office to set
	 */
	public void setOffice(String office) {
		this.office = office;
	}
	/**
	 * @return the office
	 */
	public String getOffice() {
		return office;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}

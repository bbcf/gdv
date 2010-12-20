package ch.epfl.bbcf.gdv.html.utility;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.UsersDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;

public class FormChecker {

	private Form form;
	private boolean submitable;
	private UserSession session;
	
	public FormChecker(Form form,UserSession session){
		this.form = form;
		this.submitable = true;
		this.session = session;
	}

	public void checkPasswords(String pwd, String rpwd) {
		if(null==pwd || rpwd==null){
			this.submitable = false;
			form.info("password field empty");
		}
		else if (!pwd.equalsIgnoreCase(rpwd)){
			this.submitable = false;
			form.info("password fields not correct");
		}
	}

//	public void checkUsername(String username) {
//		UsersDAO dao = new UsersDAO(Connect.getConnection(session));
//		boolean userAlreadyExist = dao.usernameExists(username);
//		//TMPUsersDAO tmpdao = new TMPUsersDAO(Connect.getConnection(session));
//		boolean tmpUserExist = true;//tmpdao.exists(username);
//		if(userAlreadyExist || tmpUserExist){
//			this.submitable = false;
//			form.info("username already exist - choose another one");
//		}
//	}
	/**
	 * check if the string given is like a mail
	 */
	public void checkMail(String mail) {
		Pattern p = Pattern.compile(".+@.+\\.[a-z]{2,4}");
		Matcher m = p.matcher(mail);
		if(!m.matches()){
			this.submitable = false;
			form.info("mail are not correct");
		}
	}

//	public void checkUsernameAndMail(String username,String mail){
//		UsersDAO dao = new UsersDAO(Connect.getConnection(session));
//		boolean userExist = dao.usernameExists(username);
//		if(userExist){
//			Users person = dao.getUserByName(username);
//			if(mail.equalsIgnoreCase(
//					person.getMail())){
//				this.submitable = true;
//			}
//			else{
//				this.submitable = false;
//				form.info("mail don't exist for this user");
//			}
//		}
//		else {
//			this.submitable = false;
//			form.info("user don't exist");
//		}
//
//	}

	public boolean isFormSubmitable() {
		return this.submitable;
	}

	

	/**
	 * check if the user can create a new project with the
	 * values he inputed
	 * @param species
	 * @param version
	 * @param projectName
	 */
	public void checkCreateNewProject(SelectOption species,
			SelectOption version, String projectName) {
		if(null==species){
			this.submitable=false;
			form.error("you must select a species");
		}
		if(null==version){
			this.submitable = false;
			form.error("you must select an assembly");
		}
		if(null==projectName){
			this.submitable = false;
			form.error("you must give a name to your project");
		}
		
	}

	/**
	 * check if an user can import a file
	 * @param url
	 * @param uploadField
	 * @param species
	 * @param version
	 */
	public void checkImportFilePageForm(String url,
			FileUploadField uploadField, SelectOption species, SelectOption version) {
		if((null==url || url.equalsIgnoreCase("")) && null==uploadField.getFileUpload()){
			this.submitable = false;
			form.error("you must give an input");
		}
		if(null==species || null==version){
			this.submitable = false;
			form.error("select the assembly your input belongs to");
		}
		if(url!=null){
			try {
				URL u = new URL(url);
			} catch (MalformedURLException e) {
				this.submitable = false;
				form.error("your URL is not a valid one");
			}
		}
		
	}
	/**
	 * check if the user can import
	 * a file from a project
	 * @param species
	 * @param version
	 * @param url
	 * @param uploadField
	 */
	public void checkImportFile(String species,
			String url, FileUploadField uploadField) {
		Application.debug("subbmit");
		if((null==url || url.equalsIgnoreCase("")) && null==uploadField.getFileUpload()){
			this.submitable = false;
			form.error("you must give an input");
		}
		if(null==species){
			this.submitable = false;
			form.error("you must select an assembly");
		}
		if(url!=null){
			try {
				URL u = new URL(url);
			} catch (MalformedURLException e) {
				this.submitable = false;
				form.error("your URL is not a valid one");
			}
		}
	}

	public void checkMagicPassword(String magic) {
		if(null==magic){
			this.submitable=false;
		}
		
	}
	
	
}

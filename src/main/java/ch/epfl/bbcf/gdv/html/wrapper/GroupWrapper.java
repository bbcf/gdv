package ch.epfl.bbcf.gdv.html.wrapper;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

public class GroupWrapper {

	private Group group;

	private List<String> users;
	private String userowner;
	
	private String currentUser;


	public GroupWrapper(Group g) {
		this.group = g;
	}
	public int getId() {
		return group.getId();
	}
	public void setOwner(int owner) {
		this.group.setOwner(owner);
	}
	public int getOwner() {
		return group.getOwner();
	}
	public void setName(String name) {
		this.group.setName(name);
	}
	public String getName() {
		return group.getName();
	}
	public List<String> getUsersMails() {
		if(null!=users){
			return users;
		}
		return new ArrayList<String>();
	}
	public void setUsersMails(List<String> so) {
		this.users = so;
	}
	public void setUserowner(String mail){
		this.userowner = mail;
	}
	public String getUserOwner() {
		return userowner;
	}
	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}
	public String getCurrentUser() {
		return currentUser;
	}
	
}

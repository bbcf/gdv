package ch.epfl.bbcf.gdv.html.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.GroupControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;
import ch.epfl.bbcf.gdv.html.wrapper.GroupWrapper;
import ch.epfl.bbcf.gdv.html.wrapper.ProjectWrapper;

public class DataGroupProvider extends SortableDataProvider<GroupWrapper>{

	public final static int OWNER = 1;
	public final static int BELONG = 2;

	private List<GroupWrapper> groups;
	private int type;
	private Users user;

	public DataGroupProvider(UserSession session, int tag) {
		type = tag;
		user = session.getUser();
		List<Group> g = new ArrayList<Group>();
		switch(type){
		case OWNER : g = GroupControl.getGroupOwnedByUser(user.getId());
		break;
		case BELONG : g = GroupControl.getGroupBelongingToUser(user.getMail());
		break;
		default : g = new ArrayList<Group>();
		}
		groups = getGroupWrappers(g,type);

	}

	private List<GroupWrapper> getGroupWrappers(List<Group> groups,int tag) {
		List<GroupWrapper> wrappers = new ArrayList<GroupWrapper>();
		for (Group g : groups){
			GroupWrapper gw = new GroupWrapper(g);
			switch(tag){
			case OWNER:
				List<String> users = GroupControl.getUserMailFromGroupId(g.getId());
				gw.setUsersMails(users);
				break;
			case BELONG :
				String userMail = GroupControl.getUserMailOwnerFromGroupId(g.getId());
				gw.setUserowner(userMail);
				break;

			}
			wrappers.add(gw);
		}
		return wrappers;
	}



	public Iterator<? extends GroupWrapper> iterator(int arg0, int arg1) {
		return groups.iterator();
	}

	public IModel<GroupWrapper> model(final GroupWrapper object) {
		return new LoadableDetachableModel<GroupWrapper>(){
			@Override
			protected GroupWrapper load() {
				return object;
			}
		};
	}

	public int size() {
		return groups.size();
	}
	public void detach() {
		List<Group> g = new ArrayList<Group>();
		switch(type){
		case OWNER : g = GroupControl.getGroupOwnedByUser(user.getId());
		break;
		case BELONG : g = GroupControl.getGroupBelongingToUser(user.getMail());
		break;
		default : g = new ArrayList<Group>();
		}
		groups = getGroupWrappers(g,type);
	}
}

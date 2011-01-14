package ch.epfl.bbcf.gdv.html.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import ch.epfl.bbcf.gdv.html.wrapper.GroupWrapper;

public class DataUserGroupProvider  extends SortableDataProvider<String>{

	private List<String> users;
	
	public DataUserGroupProvider(List<String> mails){
		users = mails;
		if(null==users){
			users=new ArrayList<String>();
		}
	}
	
	
	public Iterator iterator(int arg0, int arg1) {
		return users.iterator();
	}

	public IModel model(final String str) {
		return new LoadableDetachableModel<String>(){
			@Override
			protected String load() {
				return str;
			}
		};
	}

	public int size() {
		return users.size();
	}

}

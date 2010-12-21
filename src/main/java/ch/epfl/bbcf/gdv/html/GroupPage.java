package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.GroupControl;
import ch.epfl.bbcf.gdv.html.database.DataGroupProvider;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;
import ch.epfl.bbcf.gdv.html.wrapper.GroupWrapper;

public class GroupPage extends BasePage{

	protected final ValueMap properties = new ValueMap();
	private ch.epfl.bbcf.gdv.html.GroupPage.CustModalWindow addUserModal;

	public GroupPage(PageParameters p) {
		super(p);


		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////CREATE NEW GROUP/////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////


		Label header = new Label("create_header","create new group");
		add(header);
		final Form create_form = new Form("form");

		TextField<String> group_name = new TextField<String>(
				"group_name",new PropertyModel<String>(properties,"group_name"));
		create_form.add(group_name);
	
		create_form.add(new Button("create_but"){
			public void onSubmit(){
				String gn = properties.getString("group_name");
				if(gn!=null){
					GroupControl gc = new GroupControl((UserSession)getSession());
					gc.createNewGroup(gn);
					setResponsePage(GroupPage.class);
				}
			}
		});
		add(create_form);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////EXISTING GROUPS//////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		Label owner_label = new Label("y_g_header","your groups");
//		owner_label.setOutputMarkupPlaceholderTag(true);
//		add(owner_label);
		
		final Form y_g_form = new Form("y_g_form");
		
		DataGroupProvider dgp = new DataGroupProvider((UserSession)getSession(),DataGroupProvider.OWNER);
		
		DataView<GroupWrapper> owner = new DataView<GroupWrapper>("owner_g",dgp) {
			private AjaxButton removeUser;
			private DropDownChoice<String> ddcUsers;

			@Override
			protected void populateItem(Item<GroupWrapper> item) {
				final GroupWrapper gw = item.getModelObject();
				item.add(new Label("own_group_name",gw.getName()));
				item.add(new Label("group_status","owner"));
				
				ddcUsers = new DropDownChoice<String>(
						"versions",new Model<String>(),gw.getUsersMails()){
					protected boolean wantOnSelectionChangedNotifications() {
						return false;
					}
					protected void onSelectionChanged(final String newSelection){
						gw.setCurrentUser(newSelection);
						removeUser.setVisible(true);
					}
				};
				ddcUsers.setOutputMarkupPlaceholderTag(true);
				if(gw.getUsersMails().isEmpty()){
					ddcUsers.setVisible(false);
				}
				item.add(ddcUsers);
				AjaxButton addUser = new AjaxButton("add_user"){
					public void onSubmit(AjaxRequestTarget target, Form<?> form){
						addUserModal.setGroupId(gw.getId());
						addUserModal.show(target);
					}
				};
				item.add(addUser);
				removeUser = new AjaxButton("remove_user"){
					public void onSubmit(AjaxRequestTarget target, Form<?> form){
						Application.debug("removing user : "+gw.getCurrentUser());
						GroupControl gc = new GroupControl((UserSession)getSession());
						gc.removeUserFromGroup(gw.getId(),gw.getCurrentUser());
					}
				};
				removeUser.setOutputMarkupPlaceholderTag(true);
				removeUser.setVisible(false);
				item.add(removeUser);
				
				AjaxButton abdg = new AjaxButton("delete_group"){
					public void onSubmit(AjaxRequestTarget target, Form<?> form){
						GroupControl gc = new GroupControl((UserSession)getSession());
						gc.removeGroup(gw.getId());
					}
				};
				item.add(abdg);
			}
		};
		y_g_form.add(owner);
//		if (dgp.size()==0){
//			owner_label.setVisible(false);
//		}
		add(y_g_form);
		
		
		
		Label belong_label = new Label("b_g_header","your part of ");
		belong_label.setOutputMarkupPlaceholderTag(true);
		add(belong_label);
		final Form b_g_form = new Form("b_g_form");
		DataGroupProvider dgp2 = new DataGroupProvider((UserSession)getSession(),DataGroupProvider.BELONG);
		DataView<GroupWrapper> bg = new DataView<GroupWrapper>("bg",dgp2) {
			@Override
			protected void populateItem(Item<GroupWrapper> item) {
				GroupWrapper gw = item.getModelObject();
				item.add(new Label("belong_group_name",gw.getName()));
				item.add(new Label("belong_owner","owner is "+gw.getUserOwner()));
			}
		};
		b_g_form.add(bg);
		if(dgp2.size()==0){
			belong_label.setVisible(false);
		}
		add(b_g_form);
		
		
		
		addUserModal = new CustModalWindow("modal_add_user"); 
		add(addUserModal);
		addUserModal.setPageCreator(new ModalWindow.PageCreator(){
			public Page createPage() {
				return new AddUserToGroupPage(GroupPage.this,addUserModal);
			}
		});
		addUserModal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			public void onClose(AjaxRequestTarget target) {
				setResponsePage(GroupPage.class);
			}
		});
		
		
		
		
		
		
		
		
		
		
	}

	
	public class CustModalWindow extends ModalWindow{

		private int groupId;

		public CustModalWindow(String id) {
			super(id);
		}
		
		public int getGroupId(){
			return this.groupId;
		}
		public void setGroupId(int groupId){
			this.groupId = groupId;
		}
		
		
		
	}
}

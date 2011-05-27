package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.GroupControl;
import ch.epfl.bbcf.gdv.control.model.LoginControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.database.DataGroupProvider;
import ch.epfl.bbcf.gdv.html.database.DataUserGroupProvider;
import ch.epfl.bbcf.gdv.html.utility.GroupModalWindow;
import ch.epfl.bbcf.gdv.html.wrapper.GroupWrapper;

public class PreferencesPage extends BasePage{

	protected final ValueMap properties = new ValueMap();
	private ch.epfl.bbcf.gdv.html.PreferencesPage.CustModalWindow addUserModal;



	public PreferencesPage(PageParameters p) {
		super(p);
		//////////////////////////////////
		////LOGOUT///////////////////////
		////////////////////////////////
		final WebPage thisPage = this;
//		final Form logout_form = new Form("logout_form");
//		Button logoutBut = new Button("logout"){
//			public void onSubmit(){
//				LoginControl lc = new LoginControl((UserSession)getSession());
//				lc.logOut(thisPage,true);
//				setResponsePage(HomePage.class);
//			}
//		};
//		logoutBut.add(new SimpleAttributeModifier("title","logout from GDV"));
//		logout_form.add(logoutBut);
//		add(logout_form);





		///////////////////////////////////////
		/////////////USER KEY/////////////////
		/////////////////////////////////////
		final int projectId = p.getInt("project_id",-1);
		Users user = ((UserSession)getSession()).getUser();
		Application.debug(user.getType());
		Label userKeyLabel = new Label("user_key_label","user key");
		userKeyLabel.setOutputMarkupPlaceholderTag(true);
		add(userKeyLabel);

		String key = "";
		if(user.getType().equalsIgnoreCase("tequila")){
			key = user.getKey();
		}
		Label userKey = new Label("user_key",key);
		userKey.setOutputMarkupPlaceholderTag(true);
		add(userKey);
		if(!user.getType().equalsIgnoreCase("tequila")){
			userKeyLabel.setVisible(false);
			userKey.setVisible(false);
		}
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

		Button createBut = new Button("create_but"){
			public void onSubmit(){
				String gn = properties.getString("group_name");
				if(gn!=null){
					GroupControl gc = new GroupControl((UserSession)getSession());
					int groupId = gc.createNewGroup(gn);
					Application.debug("project id :"+projectId);
					if(projectId!=-1 && groupId!=-1){
						gc.shareProject(projectId,groupId);
					}
					setResponsePage(PreferencesPage.class);
				}
			}
		};
		createBut.add(new SimpleAttributeModifier("title","create a new group"));
		create_form.add(createBut);
		add(create_form);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////// GROUPS DATA ////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////

		Label group_header = new Label("group_header","manage groups");
		add(group_header);
		final Form groupForm = new Form("group_form");
		/////////////////////////OWNER
		final DataGroupProvider dgp = new DataGroupProvider((UserSession)getSession(),DataGroupProvider.OWNER);
		DataView<GroupWrapper> group_data = new DataView<GroupWrapper>("group_data",dgp) {
			private WebMarkupContainer userContainer;
			@Override
			protected void populateItem(Item<GroupWrapper> item) {
				final GroupWrapper gw = item.getModelObject();
				Label groupName = new Label("group_name",gw.getName());
				groupName.add(new SimpleAttributeModifier("title","group name"));
				item.add(groupName);
				Label groupStatus = new Label("group_status","owner");
				groupStatus.add(new SimpleAttributeModifier("title","you own this group"));
				item.add(groupStatus);
				AjaxButton abdg = new AjaxButton("delete_group"){
					public void onSubmit(AjaxRequestTarget target, Form<?> form){
						GroupControl gc = new GroupControl((UserSession)getSession());
						gc.removeGroup(gw.getId());
						dgp.detach();
					}
				};
				abdg.add(new SimpleAttributeModifier("title","delete the group"));
				item.add(abdg);
				AjaxButton addUser = new AjaxButton("add_user"){
					public void onSubmit(AjaxRequestTarget target, Form<?> form){
						addUserModal.setGroupId(gw.getId());
						addUserModal.show(target);
						target.addComponent(userContainer);
					}
				};
				addUser.add(new SimpleAttributeModifier("title","add an user with his email"));
				item.add(addUser);

				//users data
				userContainer = new WebMarkupContainer("users_container");
				userContainer.setOutputMarkupPlaceholderTag(true);
				userContainer.setVisible(true);
				final DataUserGroupProvider dugp = new DataUserGroupProvider(gw.getUsersMails());
				DataView<String> userData = new DataView<String>("user_data",dugp) {
					@Override
					protected void populateItem(final Item<String> item) {
						final String mail = item.getModelObject();
						Label userLab = new Label("user_mail",mail);
						userLab.add(new SimpleAttributeModifier("title","user"));
						item.add(userLab);
						//### delete
						final AjaxLink rmUser = new AjaxLink("delete_user"){
							@Override
							public void onClick(AjaxRequestTarget target) {
								GroupControl gc = new GroupControl((UserSession)getSession());
								gc.removeUserFromGroup(gw.getId(),mail);
								dugp.removeUserMail(mail);
								dugp.detach();
							}
						};
						rmUser.add(new SimpleAttributeModifier("title","delete user"));
						item.add(rmUser);
						item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel(){
							@Override
							public String getObject(){
								return (item.getIndex() % 2 == 1) ? "list_n" : "list_alt";
							}
						}));
					}
				}; 
				userContainer.add(userData);
				item.add(userContainer);

			}
		};
		
		///////////////////MEMBER
		DataGroupProvider dgp2 = new DataGroupProvider((UserSession)getSession(),DataGroupProvider.BELONG);
		DataView<GroupWrapper> group_data2 = new DataView<GroupWrapper>("group_data2",dgp2) {
			@Override
			protected void populateItem(Item<GroupWrapper> item) {
				final GroupWrapper gw = item.getModelObject();
				Label uName = new Label("group_name2",gw.getName());
				uName.add(new SimpleAttributeModifier("title","group name"));
				item.add(uName);
				Label gStat = new Label("group_status2","member");
				gStat.add(new SimpleAttributeModifier("title","you are member of this group"));
				item.add(gStat);
			}

		};
		groupForm.add(group_data);
		groupForm.add(group_data2);
		add(groupForm);

		//modal page to add user to a group
		addUserModal = new CustModalWindow("modal_add_user"); 
		add(addUserModal);
		addUserModal.setPageCreator(new ModalWindow.PageCreator(){
			public Page createPage() {
				return new AddUserToGroupPage(PreferencesPage.this,addUserModal);
			}
		});
		addUserModal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			public void onClose(AjaxRequestTarget target) {
				setResponsePage(PreferencesPage.class);
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

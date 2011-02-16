package ch.epfl.bbcf.gdv.html;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.GroupControl;
import ch.epfl.bbcf.gdv.html.database.DataGroupProvider;
import ch.epfl.bbcf.gdv.html.utility.GroupModalWindow;
import ch.epfl.bbcf.gdv.html.wrapper.GroupWrapper;
import ch.epfl.bbcf.gdv.html.wrapper.ProjectWrapper;

public class GroupListPage extends WebPage{

	private CheckGroup<GroupWrapper> group;
	
	public GroupListPage(ProjectPage projectPage, final GroupModalWindow groupModal) {
		for(String cp : Configuration.getGDVCSSFiles()){
			add(CSSPackageResource.getHeaderContribution(cp));
		}
		
		final ProjectWrapper project = groupModal.getProject();

		Label projectInfo = new Label("project_info","sharing project");
		Label projectInfo2 = new Label("project_info2",project.getDescription());
		Label projectInfo3 = new Label("project_info3",	" with group(s) :");
		add(projectInfo);
		add(projectInfo2);
		add(projectInfo3);
		
		Form form = new Form("form");
		form.add(new AjaxButton("submit"){
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> arg1) {
				List<GroupWrapper> list = (List<GroupWrapper>) group.getDefaultModelObject();
				GroupControl gc = new GroupControl((UserSession)getSession());
				for(GroupWrapper gw : list){
					gc.shareProject(project.getId(),gw.getId());
				}
				groupModal.close(target);
			}
			
		});
		group = new CheckGroup<GroupWrapper>("group", new ArrayList<GroupWrapper>());
		form.add(group);
		Label title1 = new Label("title1","owner");
		Label title2 = new Label("title2","member");
		group.add(title1);
		group.add(title2);
		group.add(new CheckGroupSelector("groupselector"));

		DataGroupProvider dgp = new DataGroupProvider((UserSession)getSession(),DataGroupProvider.OWNER);
		DataView<GroupWrapper> group_data = new DataView<GroupWrapper>("group_data",dgp) {
			@Override
			protected void populateItem(final Item<GroupWrapper> item) {
				final GroupWrapper gw = item.getModelObject();
				item.add(new Label("group_name",gw.getName()));
				item.add(new Check<GroupWrapper>("checkbox",item.getModel()));
				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel(){
					@Override
					public String getObject(){
						return (item.getIndex() % 2 == 1) ? "list_n" : "list_alt";
					}
				}));
			}

		};
		
		DataGroupProvider dgp2 = new DataGroupProvider((UserSession)getSession(),DataGroupProvider.BELONG);
		DataView<GroupWrapper> group_data2 = new DataView<GroupWrapper>("group_data2",dgp2) {
			@Override
			protected void populateItem(final Item<GroupWrapper> item) {
				final GroupWrapper gw = item.getModelObject();
				item.add(new Label("group_name2",gw.getName()));
				item.add(new Check<GroupWrapper>("checkbox2",item.getModel()));
				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel(){
					@Override
					public String getObject(){
						return (item.getIndex() % 2 == 1) ? "list_n" : "list_alt";
					}
				}));
			}

		};
		
		group.add(group_data);
		group.add(group_data2);
		add(form);
	}

}

package ch.epfl.bbcf.gdv.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.GroupControl;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.SequenceControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.database.DataProjectProvider;
import ch.epfl.bbcf.gdv.html.database.DataTrackProvider;
import ch.epfl.bbcf.gdv.html.utility.CustModalWindow;
import ch.epfl.bbcf.gdv.html.utility.FormChecker;
import ch.epfl.bbcf.gdv.html.utility.GroupModalWindow;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;
import ch.epfl.bbcf.gdv.html.wrapper.ProjectWrapper;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;

public class ProjectPage extends BasePage{

	protected final ValueMap properties = new ValueMap();
	protected final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
	protected SelectOption so;
	private DropDownChoice<SelectOption> ddcVersion;
	private DropDownChoice<SelectOption> ddcSpecies;
	private DataView<ProjectWrapper> projectData;
	private CustModalWindow importModal;
	private GroupModalWindow groupModal;

	private final static IChoiceRenderer<SelectOption> choiceRenderer = new ChoiceRenderer<SelectOption>("value", "key");

	public ProjectPage(PageParameters p) {
		super(p);
		Cookie cook = ((WebRequest)getRequestCycle().getRequest()).getCookie("PROJECT_ID");
		if(null!=cook){
			ProjectControl pc = new ProjectControl((UserSession)getSession());
			if(pc.importProject(cook.getValue())){
				cook.setMaxAge(0);
				((WebResponse)getRequestCycle().getResponse()).addCookie(cook);
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////CREATE NEW PROJECT///////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		final Form create_form = new Form("form");
		Label header = new Label("create_header","Create new project");
		add(header);

		TextField<String> project_name = new TextField<String>(
				"project_name",new PropertyModel<String>(properties,"project_name"));
		create_form.add(project_name);

		List<SelectOption> spOptions = SequenceControl.getSpeciesSO();
		//IChoiceRenderer<SelectOption> choiceRenderer = new ChoiceRenderer<SelectOption>("value", "key");
		ddcSpecies = new DropDownChoice<SelectOption>(
				"species",new Model<SelectOption>(),spOptions,choiceRenderer){
			protected boolean wantOnSelectionChangedNotifications() {
				return true;
			}
			protected void onSelectionChanged(final SelectOption newSelection){
				ddcVersion.updateModel();
			}
		};

		ddcVersion = new DropDownChoice<SelectOption>("version",new Model<SelectOption>(),
				new LoadableDetachableModel<List<SelectOption>>() {
			@Override
			protected List<SelectOption> load() {
				SelectOption so = (SelectOption) ddcSpecies.getDefaultModelObject();
				if(null== so){
					return new ArrayList<SelectOption>();
				}
				else {
					return SequenceControl.getSequencesFromSpeciesIdSO(so.getKey());
				}
			}
		},
		choiceRenderer);

		create_form.add(ddcSpecies);
		create_form.add(ddcVersion);
		create_form.add(new Button("create_but"){
			public void onSubmit(){
				//	Application.debug("create but");
				SelectOption species = (SelectOption) ddcSpecies.getDefaultModelObject();
				SelectOption version = (SelectOption) ddcVersion.getDefaultModelObject();
				String project_name = properties.getString("project_name");
				FormChecker checker = new FormChecker(create_form,(UserSession)getSession());
				checker.checkCreateNewProject(species,version,project_name);
				if(checker.isFormSubmitable()){
					ProjectControl pc = new ProjectControl((UserSession)getSession());
					if(pc.createNewProject(species,version,project_name)){
						setResponsePage(new ProjectPage(new PageParameters()));
					}
					else {
						create_form.error("project creation failed ... an admin is contacted");
					}
				}
			}
		});
		create_form.add(new FeedbackPanel("feedback"));
		add(create_form);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////EXISTING PROJECTS////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		final Form existing_form = new Form("exist_form");
		Label project_header = new Label("project_header","Existing projects");
		add(project_header);
		final WebMarkupContainer projectContainer = new WebMarkupContainer("projects_container");
		projectContainer.setOutputMarkupPlaceholderTag(true);
		DataProjectProvider dpp = new DataProjectProvider((UserSession)getSession());
		//PROJECTS
		projectData = new DataView<ProjectWrapper>("project_data",dpp) {
			@Override
			protected void populateItem(Item<ProjectWrapper> item) {
				final ProjectWrapper projectWrapper = item.getModelObject();
				//### label
				final AjaxEditableLabel<String> editableProjectName = new AjaxEditableLabel<String>("description",
						new Model<String>(projectWrapper.getDescription())){

					@Override
					protected void onSubmit(AjaxRequestTarget target){
						ProjectControl pc = new ProjectControl((UserSession)getSession());
						pc.renameProject(projectWrapper.getId(),getEditor().getInput());

					}
				};
				item.add(editableProjectName);

				//### from group
				item.add(new Label("group_names",projectWrapper.getGroupName()));
				//### species name
				item.add(new Label("project_species",projectWrapper.getSpeciesName()));

				//###assembly
				SequenceControl secC = new SequenceControl((UserSession)getSession());
				Sequence seq = secC.getSequenceFromId(projectWrapper.getSequenceId());
				Label assemblyName = new Label("project_version",seq.getName());
				item.add(assemblyName);
				//### track number
				final Label track_number = new Label("tracks_number",Integer.toString(projectWrapper.getTracksNumber()));
				track_number.setOutputMarkupId(true);
				item.add(track_number);
				item.add(new AjaxButton("import_but"){
					public void onSubmit(AjaxRequestTarget target, Form<?> form){
						importModal.setParams(
								-1,projectWrapper.getSpeciesId(),((UserSession)getSession()).getUserId(),projectWrapper.getId(),false);
						importModal.show(target);
					}
				});
				//### view it
				item.add(new Button("view_but"){
					public void onSubmit(){
						PageParameters params 	= new PageParameters();
						params.put("id", Integer.toString(projectWrapper.getId()));	
						setResponsePage(BrowserPage.class,params);
					}
				});
				//### share it
				item.add(new AjaxButton("share_but"){
					public void onSubmit(AjaxRequestTarget target, Form<?> form){
						GroupControl gc = new GroupControl((UserSession)getSession());
						if(!gc.checkIfGroupsForUser()){
							PageParameters params 	= new PageParameters();
							params.put("project_id", projectWrapper.getId());	
							setResponsePage(PreferencesPage.class,params);
						} else {
							groupModal.setProject(projectWrapper);
							groupModal.show(target);
						}
					}
				});

				//### delete it
				AjaxButton del;

				if(projectWrapper.isAdmin()){
					del = new AjaxButton("delete_but"){
						public void onSubmit(AjaxRequestTarget target, Form<?> form){
							ProjectControl pc = new ProjectControl((UserSession)getSession());
							pc.deleteProject(projectWrapper.getId());
							projectData.detach();
						}
					};
				} else {
					del = new AjaxButton("delete_but"){
						public void onSubmit(AjaxRequestTarget target, Form<?> form){
						}
					};
					del.setOutputMarkupPlaceholderTag(true);
					del.setVisible(false);
				}
				item.add(del);












				//////////////////////////////////////////////////TRACKS//////////////////////////////////////////////////////
				//### container
				final WebMarkupContainer trackContainer = new WebMarkupContainer("tracks_container");
				trackContainer.setOutputMarkupPlaceholderTag(true);
				trackContainer.setVisible(false);
				//### arrow
				final Image image = new Image("arrow");
				image.add(new SimpleAttributeModifier("src",Configuration.getGdv_Images_url()+"/right_arrow.jpeg"));
				image.setOutputMarkupId(true);
				AjaxLink link = new AjaxLink("arrow_link"){
					@Override
					public void onClick(AjaxRequestTarget target) {
						if(trackContainer.isVisible()){
							projectWrapper.setListOpen(false);
							trackContainer.setVisible(false);
							image.add(new SimpleAttributeModifier("src",Configuration.getGdv_Images_url()+"/right_arrow.jpeg"));
							target.addComponent(image);
						} else {
							trackContainer.setVisible(true);
							projectWrapper.setListOpen(true);
							image.add(new SimpleAttributeModifier("src",Configuration.getGdv_Images_url()+"/down_arrow.jpeg"));
							target.addComponent(image);
						}
						target.addComponent(trackContainer);
					}
				};
				link.add(image);
				item.add(link);


				//### tracks
				final DataTrackProvider dtp = new DataTrackProvider((UserSession)getSession(),projectWrapper);
				int alt=0;
				final DataView<TrackWrapper> trackData;
				trackData = new DataView<TrackWrapper>("track_data",dtp){
					@Override
					protected void populateItem(final Item<TrackWrapper> item) {
						final TrackWrapper track = item.getModelObject();
						//### name
						final AjaxEditableLabel<String> editableTrackName = new AjaxEditableLabel<String>("track_label",
								new Model<String>(track.getName())){
							@Override
							protected void onSubmit(AjaxRequestTarget target){
								TrackControl tc = new TrackControl((UserSession)getSession());
								Track newTrack = tc.getTrackById(track.getId());
								if(null!=newTrack && null!=track.getName() && !track.getName().equalsIgnoreCase("in process")){
									tc.renameTrack(track.getId(),getEditor().getInput());
								}
							}
						};
						if(track.getName().equalsIgnoreCase("in process")){
							editableTrackName.setOutputMarkupId(true);
							final AbstractAjaxTimerBehavior behaviour = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
								@Override 
								protected void onPostProcessTarget(AjaxRequestTarget target) { 
									editableTrackName.setDefaultModelObject(getLabel(track.getId(),editableTrackName,this));
									target.addComponent(editableTrackName);
								}
								private String getLabel(int id, AjaxEditableLabel label, AbstractAjaxTimerBehavior behaviour) {
									TrackControl tc = new TrackControl((UserSession)getSession());
									Track newTrack = tc.getTrackById(track.getId());
									if(null==newTrack || null==track.getName()){
										return "in process";
									}
									behaviour.stop();
									return newTrack.getName();
								} 
							};
							editableProjectName.add(behaviour);
						}

						item.add(editableTrackName);


						//### date
						String date ="";
						if(null!=track.getDate()){
							date = dateFormat.format(track.getDate());
						}
						Label theDate = new Label("track_date",date);
						item.add(theDate);
						//### status
						final Image imgLoader = new Image("status_loader");
						imgLoader.add(new SimpleAttributeModifier("src",Configuration.getGdv_Images_url()+"/blue-loader.gif"));
						imgLoader.setOutputMarkupPlaceholderTag(true);
						final Label statusLabel = new Label("status",getStatus(track,imgLoader,new AjaxRequestTarget(getPage()),null));
						statusLabel.setOutputMarkupId(true);
						if(!track.getStatus().equalsIgnoreCase("completed") && !track.getStatus().equalsIgnoreCase("error")){
							final AbstractAjaxTimerBehavior behaviour = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
								@Override 
								protected void onPostProcessTarget(AjaxRequestTarget target) { 
									statusLabel.setDefaultModelObject(getStatus(track,imgLoader,target,this));
									target.addComponent(statusLabel);
								} 
							};
							statusLabel.add(behaviour);
						}
						item.add(imgLoader);
						item.add(statusLabel);
						item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel(){
							@Override
							public String getObject(){
								return (item.getIndex() % 2 == 1) ? "list_n" : "list_alt";
							}
						}));

						//### delete
						final AjaxLink link = new AjaxLink("delete"){
							@Override
							public void onClick(AjaxRequestTarget target) {
								TrackControl tc = new TrackControl((UserSession)getSession());
								tc.removeTrackFromUser(track.getTrackInstance());
								dtp.detach();
								target.addComponent(trackContainer);
							}
						};
						//						link.add(new SimpleAttributeModifier("onclick", "return confirm('are you sure you want to delete this track ?');"));
						//						link.setOutputMarkupPlaceholderTag(true);
						item.add(link);
					}

					private String getStatus(TrackWrapper track, Image image,
							AjaxRequestTarget ajaxRequestTarget, AbstractAjaxTimerBehavior behaviour) {
						TrackControl tc = new TrackControl((UserSession)getSession());
						Track newTrack = tc.getTrackById(track.getId());
						String status="";
						if(newTrack!=null){
							status = newTrack.getStatus();
							if(status==null){
								status ="";
							}
						}
						if(status.equalsIgnoreCase("completed") || status.equalsIgnoreCase(TrackControl.STATUS_ERROR)){
							image.setVisible(false);
							if(null!=behaviour){
								behaviour.stop();
							}
							ajaxRequestTarget.addComponent(image);
						} else {
							image.setVisible(true);
							ajaxRequestTarget.addComponent(image);
						}
						return status;
					}
				};
				alt++;
				trackContainer.add(trackData);
				item.add(trackContainer);
			}
		};
		projectContainer.add(projectData);
		existing_form.add(projectContainer);
		add(existing_form);










		//MODAL PANEL IMPORT FILE
		importModal = new CustModalWindow("modal_import"); 
		add(importModal);
		importModal.setPageCreator(new ModalWindow.PageCreator(){
			public Page createPage() {
				return new ImportFilePage(ProjectPage.this,importModal);
			}
		});
		importModal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			public void onClose(AjaxRequestTarget target) {
				setResponsePage(ProjectPage.class);
			}
		});

		//MODAL PANEL SHARE
		groupModal = new GroupModalWindow("modal_group");
		add(groupModal);
		groupModal.setPageCreator(new ModalWindow.PageCreator() {
			public Page createPage() {
				return new GroupListPage(ProjectPage.this,groupModal);
			}
		});
		importModal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			public void onClose(AjaxRequestTarget target) {
				setResponsePage(ProjectPage.class);
			}
		});
	}

}

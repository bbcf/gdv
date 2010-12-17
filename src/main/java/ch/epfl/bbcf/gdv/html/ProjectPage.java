package ch.epfl.bbcf.gdv.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.generep.AssembliesAccess;
import ch.epfl.bbcf.gdv.access.generep.SpeciesAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.SequenceControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.database.DataProjectProvider;
import ch.epfl.bbcf.gdv.html.database.DataTrackProvider;
import ch.epfl.bbcf.gdv.html.utility.FormChecker;
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
	private final static IChoiceRenderer<SelectOption> choiceRenderer = new ChoiceRenderer<SelectOption>("value", "key");

	public ProjectPage(PageParameters p) {
		super(p);
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
				Application.debug("create but");
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
		DataProjectProvider dpp = new DataProjectProvider((UserSession)getSession());
		//PROJECTS
		projectData = new DataView<ProjectWrapper>("project_data",dpp) {
			@Override
			protected void populateItem(Item<ProjectWrapper> item) {
				final ProjectWrapper pw = item.getModelObject();
				item.add(new Label("description",pw.getDescription()));
				item.add(new Label("project_species",pw.getSpeciesName()));
				item.add( new DropDownChoice<SelectOption>(
						"project_versions",new Model<SelectOption>(),pw.getSequences(),choiceRenderer){
					protected boolean wantOnSelectionChangedNotifications() {
						return true;
					}
					protected void onSelectionChanged(final SelectOption newSelection){
						pw.setSequenceId(newSelection.getKey());
					}
				});
				final Label track_number = new Label("tracks_number",Integer.toString(pw.getTracksNumber()));
				track_number.setOutputMarkupId(true);
				item.add(track_number);
				item.add(new AjaxButton("import_but"){
					public void onSubmit(AjaxRequestTarget target, Form<?> form){
						importModal.setParams(pw.getSpeciesId(),pw.getId(),((UserSession)getSession()).getUserId());
						importModal.show(target);
					}
				});
				item.add(new Button("view_but"){
					public void onSubmit(){
						PageParameters params 	= new PageParameters();
						params.put("id", Integer.toString(pw.getId()));	
						setResponsePage(BrowserPage.class,params);
					}
				});
				
				
				//TODO ADD SHARE BUTTON

				
				final WebMarkupContainer trackContainer = new WebMarkupContainer("tracks_container");
				trackContainer.setOutputMarkupPlaceholderTag(true);
				trackContainer.setVisible(false);
				final Image image = new Image("arrow","../public/images/right_arrow.jpeg");
				image.setOutputMarkupId(true);
				AjaxLink link = new AjaxLink("arrow_link"){
					@Override
					public void onClick(AjaxRequestTarget target) {
						if(trackContainer.isVisible()){
							trackContainer.setVisible(false);
							image.add(new SimpleAttributeModifier("src","../public/images/right_arrow.jpeg"));
							target.addComponent(image);
						} else {
							trackContainer.setVisible(true);
							image.add(new SimpleAttributeModifier("src","../public/images/down_arrow.jpeg"));
							target.addComponent(image);
						}
						target.addComponent(trackContainer);
					}
				};
				link.add(image);
				item.add(link);
				DataTrackProvider dtp = new DataTrackProvider((UserSession)getSession(),pw);
				int alt=0;
				//TRACKS
				DataView<TrackWrapper> trackData = new DataView<TrackWrapper>("track_data",dtp){
					@Override
					protected void populateItem(final Item<TrackWrapper> item) {
						final TrackWrapper track = item.getModelObject();
						//->name
						final Label label = new Label("track_label",track.getName());
						if(track.getName().equalsIgnoreCase("in process")){
							label.setOutputMarkupId(true);
							label.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
								@Override 
								protected void onPostProcessTarget(AjaxRequestTarget target) { 
									label.setDefaultModelObject(getLabel(track.getId()));
									target.addComponent(label);
								}
								private String getLabel(int id) {
									TrackControl tc = new TrackControl((UserSession)getSession());
									Track newTrack = tc.getTrackById(track.getId());
									if(null==newTrack || null==track.getName()){
										return "in process";
									}
									return newTrack.getName();

								} 
							});
						}
						item.add(label);
						//->date
						String date ="";
						if(null!=track.getDate()){
							date = dateFormat.format(track.getDate());
						}
						Label theDate = new Label("track_date",date);
						item.add(theDate);
						//->status
						final Image imgLoader = new Image("status_loader","/blue-loader.gif");
						imgLoader.setOutputMarkupPlaceholderTag(true);
						final Label statusLabel = new Label("status",getStatus(track,imgLoader,new AjaxRequestTarget(getPage())));
						statusLabel.setOutputMarkupId(true);
						if(!track.getStatus().equalsIgnoreCase("completed") || !track.getStatus().equalsIgnoreCase(TrackControl.STATUS_ERROR)){
							statusLabel.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
								@Override 
								protected void onPostProcessTarget(AjaxRequestTarget target) { 
									statusLabel.setDefaultModelObject(getStatus(track,imgLoader,target));
									target.addComponent(statusLabel);
								} 
							});
						}
						item.add(imgLoader);
						item.add(statusLabel);
						item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel(){
							@Override
							public String getObject(){
								return (item.getIndex() % 2 == 1) ? "pt_n" : "pt_alt";
							}
						}));

						//-> delete link
						final Link link = new Link("delete"){
							@Override
							public void onClick() {
								TrackControl tc = new TrackControl((UserSession)getSession());
								tc.removeTrackFromUser(track.getTrackInstance());
								setResponsePage(ProjectPage.class);
							}
						};
						link.add(new SimpleAttributeModifier("onclick", "return confirm('are you sure you want to delete this track ?');"));
						link.setOutputMarkupPlaceholderTag(true);
						item.add(link);
					}

					private String getStatus(TrackWrapper track, Image image,
							AjaxRequestTarget ajaxRequestTarget) {
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
		existing_form.add(projectData);
		add(existing_form);

		//MODAL PANEL
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


	}
	
	
	
	
	
	
	
	
	
	
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////OTHER CLASS//////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	
	
	
	
	public class CustModalWindow extends ModalWindow{

		private String speciesId;
		private int userId;
		private int projectId;
		private Label label;


		public CustModalWindow(String id) {
			super(id);
		}
		public CustModalWindow(String id, IModel<?> model) {
			super(id, model);
		}

		public void setParams(int speciesId,int userId,int projectId){
			this.setSpeciesID(Integer.toString(speciesId));
			this.setUserId(userId);
			this.setProjectId(projectId);
		}
		/**
		 * @param version the version to set
		 */
		public void setSpeciesID(String version) {
			this.speciesId = version;
		}
		/**
		 * @return the version
		 */
		public String getSpeciesId() {
			return speciesId;
		}
		/**
		 * @param userId the userId to set
		 */
		public void setUserId(int userId) {
			this.userId = userId;
		}
		/**
		 * @return the userId
		 */
		public int getUserId() {
			return userId;
		}
		/**
		 * @param projectId the projectId to set
		 */
		public void setProjectId(int projectId) {
			this.projectId = projectId;
		}
		/**
		 * @return the projectId
		 */
		public int getProjectId() {
			return projectId;
		}
		/**
		 * @param label the label to set
		 */
		public void setLabel(Label label) {
			this.label = label;
		}
		/**
		 * @return the label
		 */
		public Label getLabel() {
			return label;
		}

	}
	public void refresh() {
		setResponsePage(ProjectPage.class, new PageParameters());

	}
}

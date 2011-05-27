package ch.epfl.bbcf.gdv.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.genrep.GenrepWrapper;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.SequenceControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.database.DataAdminTrackProvider;
import ch.epfl.bbcf.gdv.html.utility.CustModalWindow;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;


public class AdminPage extends BasePage{

	private CustModalWindow importModal;
	private int import_speciesId = -1;
	private int import_assemblyId;
	
	private DropDownChoice  ddcVersion;
	private int speciesId = -1;
	private int assemblyId = -1;
	private FeedbackPanel feedback;
	SelectOption[] spOptions = GenrepWrapper.getOrganismsSO();
	IChoiceRenderer<SelectOption> choiceRenderer = new ChoiceRenderer<SelectOption>("value", "key");
	private Form form;

	public AdminPage(PageParameters p) {
		super(p);
		
		
		
		//ADD A GENOME
		Label genome_header = new Label("genome_header","add a new sequence");
		
		 form = new Form("form"){
			public void onSubmit(){
				if(-1!=speciesId && -1!=assemblyId){
					SequenceControl sc = new SequenceControl((UserSession)getSession());
					boolean created = sc.createGenome(assemblyId,speciesId,feedback);
					if(created){
						feedback.info("genome created");
					} else {
						feedback.error("genome not created");
					}
				}
			}
		};

		//## species
		DropDownChoice ddcSpecies = new DropDownChoice<SelectOption>("species",new Model(),Arrays.asList(spOptions),choiceRenderer){
				protected boolean wantOnSelectionChangedNotifications() {
					return true;
				}
				protected void onSelectionChanged(final SelectOption newSelection){
					speciesId = newSelection.getKey();
					ddcVersion.updateModel();
				}
		};
			
		//## version
		ddcVersion = new DropDownChoice("version",new Model(),
				new LoadableDetachableModel<List<SelectOption>>() {
			@Override
			protected List<SelectOption> load() {
				if(-1==speciesId){
					return new ArrayList<SelectOption>();
				}
				else {
					SequenceControl sc = new SequenceControl((UserSession)getSession());
					List<SelectOption> ass = sc.getNRAssembliesNotCreated(speciesId);
					return ass;
				}
			}
		},
		choiceRenderer){
			protected boolean wantOnSelectionChangedNotifications() {
				return true;
			}
			protected void onSelectionChanged(final Object newSelection){
				assemblyId = ((SelectOption)newSelection).getKey();
				
			}
		};
		
		if(-1!=speciesId){
			for (SelectOption so : spOptions){
				if(speciesId==so.getKey()){
					ddcSpecies.setDefaultModelObject(so);
				}
			}
		}
		if(-1!=assemblyId){
			for (SelectOption so : spOptions){
				if(assemblyId==so.getKey()){
					ddcVersion.setDefaultModelObject(so);
				}
			}
		}
		
		ddcVersion.setOutputMarkupId(true);
		form.add(genome_header);
		form.add(ddcSpecies);
		form.add(ddcVersion);
		feedback = new FeedbackPanel("feedback");
		form.add(feedback);

		
		//IMPORT A FILE FOR EVERY USER 
		Label import_header = new Label("import_header","import a file for species selected");
		form.add(import_header);
		
		List<SelectOption> spOptions = SequenceControl.getSpeciesSO();
		//## species
		final DropDownChoice<SelectOption> ddcImportForSpecies = new DropDownChoice<SelectOption>(
				"import_species",new Model<SelectOption>(),spOptions,choiceRenderer){
			protected boolean wantOnSelectionChangedNotifications() {
				return true;
			}
			protected void onSelectionChanged(final SelectOption newSelection){
				import_speciesId = newSelection.getKey();
			}
		};
		form.add(ddcImportForSpecies);
		
		//## version
		final DropDownChoice<SelectOption> ddcImportForVersion  = new DropDownChoice<SelectOption>("import_version",new Model<SelectOption>(),
				new LoadableDetachableModel<List<SelectOption>>() {
			@Override
			protected List<SelectOption> load() {
				SelectOption so = (SelectOption) ddcImportForSpecies.getDefaultModelObject();
				if(null== so){
					return new ArrayList<SelectOption>();
				}
				else {
					return SequenceControl.getSequencesFromSpeciesIdSO(so.getKey());
				}
			}
		},
		choiceRenderer){
				protected boolean wantOnSelectionChangedNotifications() {
					return true;
				}
				protected void onSelectionChanged(final SelectOption newSelection){
					import_assemblyId = newSelection.getKey();
				}
		};
		form.add(ddcImportForVersion);
		form.add(new AjaxButton("import_but"){
			public void onSubmit(AjaxRequestTarget target, Form<?> form){
				importModal.setParams(import_assemblyId,import_speciesId,((UserSession)getSession()).getUserId(),-1,true);
				importModal.show(target);
			}
		});
		
		//LIST OF ADMIN TRACKS
		//## header
		Label tracks = new Label("list_header","Track list");
		form.add(tracks);
		//## track list
		final DataAdminTrackProvider dtp = new DataAdminTrackProvider((UserSession)getSession());
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
				item.add(editableTrackName);
				//## delete
				final AjaxLink link = new AjaxLink("delete"){
					@Override
					public void onClick(AjaxRequestTarget target) {
						TrackControl tc = new TrackControl((UserSession)getSession());
						tc.removeAdminTrack(track.getTrackInstance());
						dtp.detach();
					}
				};
				link.add(new SimpleAttributeModifier("title","delete the track in the database and flat file associated"));
				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel(){
					@Override
					public String getObject(){
						return (item.getIndex() % 2 == 1) ? "list_n" : "list_alt";
					}
				}));
				item.add(link);
			}
		};
		form.add(trackData);
		
		
		
		
		
		
		
		add(form);
		
		
		
		
		
		
		
		
		
		
		
		//MODAL WINDOW
		importModal = new CustModalWindow("modal_import"); 
		form.add(importModal);
		importModal.setPageCreator(new ModalWindow.PageCreator(){
			public Page createPage() {
				return new ImportFilePage(AdminPage.this,importModal);
			}
		});
		importModal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			public void onClose(AjaxRequestTarget target) {
				setResponsePage(ProjectPage.class);
			}
		});
	}

}

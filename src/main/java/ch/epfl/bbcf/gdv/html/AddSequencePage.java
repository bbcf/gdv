package ch.epfl.bbcf.gdv.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestContext;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.IOnChangeListener;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;

import ch.epfl.bbcf.gdv.access.genrep.GenrepWrapper;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.SequenceControl;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;


public class AddSequencePage extends BasePage{

	private Form form;
	private DropDownChoice ddcSpecies;
	private DropDownChoice  ddcVersion;
	private int speciesId = -1;
	private int assemblyId = -1;
	private FeedbackPanel feedback;
	SelectOption[] spOptions = GenrepWrapper.getOrganismsSO();

	public AddSequencePage(final PageParameters p) {
		super(p);
		feedback = new FeedbackPanel("feedback");
		add(feedback);
		Application.debug("Add sequence page", ((UserSession)getSession()).getUserId());

		form = new Form("form"){
			public void onSubmit(){
				if(-1!=speciesId && -1!=assemblyId){
					boolean created = SequenceControl.createGenome(assemblyId,speciesId,feedback);
					if(created){
						feedback.info("genome created");
					} else {
						feedback.info("genome not created");
					}
				}
			}
		};





		//-->SPECIES
		IChoiceRenderer spchoiceRenderer = new ChoiceRenderer("value", "key");
		
		ddcSpecies = new DropDownChoice<SelectOption>("species",new Model(),Arrays.asList(spOptions),spchoiceRenderer){
				protected boolean wantOnSelectionChangedNotifications() {
					return true;
				}
				protected void onSelectionChanged(final SelectOption newSelection){
					speciesId = newSelection.getKey();
					ddcVersion.updateModel();
				}
			
		};
		IChoiceRenderer vchoiceRenderer = new ChoiceRenderer("value", "key");
			
		//-->VERSION
		ddcVersion = new DropDownChoice("version",new Model(),
				new LoadableDetachableModel<List<SelectOption>>() {
			@Override
			protected List<SelectOption> load() {
				if(-1==speciesId){
					return new ArrayList<SelectOption>();
				}
				else {
					List<SelectOption> allAssemblies = Arrays.asList(GenrepWrapper.getNRAssembliesByOrganismIdSO(speciesId));
					List<SelectOption> nonAddedAssemblies = new ArrayList<SelectOption>();
					Application.debug("seq control");
					for (SelectOption so : allAssemblies){
						if(!SequenceControl.isCreatedOnJBrowsoR(so.getKey(),so.getValue())){
							nonAddedAssemblies.add(so);
						}
					}
					Application.debug("end of seqcontrol");
					return nonAddedAssemblies;
				}
			}
		},
		vchoiceRenderer){
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
		form.add(ddcSpecies);
		form.add(ddcVersion);
		add(form);



	}
}

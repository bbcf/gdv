package ch.epfl.bbcf.gdv.html;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.SequenceControl;
import ch.epfl.bbcf.gdv.html.utility.CustModalWindow;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;


public class AdminPage extends BasePage{

	private CustModalWindow importModal;
	private DropDownChoice<SelectOption> ddcVersion;
	private int speciesId;
	

	public AdminPage(PageParameters p) {
		super(p);
		
		
		
		
		List<SelectOption> spOptions = SequenceControl.getSpeciesSO();
		IChoiceRenderer<SelectOption> choiceRenderer = new ChoiceRenderer<SelectOption>("value", "key");
		final DropDownChoice<SelectOption> ddcSpecies = new DropDownChoice<SelectOption>(
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
		choiceRenderer){
			protected boolean wantOnSelectionChangedNotifications() {
				return true;
			}
			protected void onSelectionChanged(final SelectOption newSelection){
				speciesId = Integer.parseInt(newSelection.getKey());
			}
		};
		
		
		
		
		add(new AjaxButton("import_but"){
			public void onSubmit(AjaxRequestTarget target, Form<?> form){
				importModal.setParams(speciesId,-1,((UserSession)getSession()).getUserId(),true);
				importModal.show(target);
			}
		});
		
		
		importModal = new CustModalWindow("modal_import"); 
		add(importModal);
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

package ch.epfl.bbcf.gdv.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.access.generep.AssembliesAccess;
import ch.epfl.bbcf.gdv.access.generep.SpeciesAccess;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.SequenceControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.ProjectPage.CustModalWindow;
import ch.epfl.bbcf.gdv.html.utility.FormChecker;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;



public class ImportFilePage extends WebPage{

	protected final ValueMap properties = new ValueMap();
	private FileUploadField uploadField;
	private Form form;
	private TextArea<String> url;




	public ImportFilePage(final ProjectPage projectPage, final CustModalWindow importModal) {
		form = new Form("form");
		form.add(new AjaxButton("submit"){
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form_) {
				String url = properties.getString("url");
				boolean sendMail = properties.getBoolean("send_mail");
				String species = importModal.getSpecies();
				String version = importModal.getVersion();
				int projectId = importModal.getProjectId();
				FormChecker checker = new FormChecker(form_,(UserSession)getSession());
				checker.checkImportFile(species,version,url,uploadField);
				if(checker.isFormSubmitable()){
					InputControl ic = new InputControl((UserSession)getSession());
					String result = ic.processInputs(projectId,url,uploadField.getFileUpload(),version,sendMail,false);
					info(result);
					importModal.close(target);
				} else {
					
				}


			}

		});

		//->url
		url = new TextArea<String>("url",new PropertyModel<String>(properties,"url"));
		url.setOutputMarkupId(true);
		form.add(url);
		AjaxButton clear = new AjaxButton("clear"){
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				url.setModelObject(null);
				target.addComponent(url);
			}
		};
		form.add(clear);

		//->upload
		uploadField = new FileUploadField("upload");
		form.add(uploadField);

		//->mail
		form.add(new CheckBox("send_mail",new PropertyModel(properties,"send_mail")));
		form.add(new FeedbackPanel("feedback"));
		add(form);
	}




	//	public ImportFilePage(PageParameters p) {
	//		super(p);
	//		FeedbackPanel feedback = new FeedbackPanel("feedback");
	//		add(feedback);
	//
	//		
	//			@Override
	//			public void onSubmit(){
	//				String url = properties.getString("url");
	//				boolean sendMail = properties.getBoolean("send_mail");
	//				SelectOption species = (SelectOption) ddcSpecies.getDefaultModelObject();
	//				SelectOption version = (SelectOption) ddcVersion.getDefaultModelObject();
	//				
	//				FormChecker checker = new FormChecker(this,(UserSession)getSession());
	//				checker.checkImportFilePageForm(url,uploadField,species,version);
	//				if(checker.isFormSubmitable()){
	//					
	//					InputControl ic = new InputControl((UserSession)getSession());
	//					String result = ic.processInputs(url,uploadField.getFileUpload(),version.getKey(),sendMail,false);
	//					info(result);
	//					
	//				}
	//			}
	//		};
	//		//->Species
	//		SelectOption[] spOptions = SpeciesAccess.getOrganismsSelectOpt();
	//		IChoiceRenderer<SelectOption> choiceRenderer = new ChoiceRenderer<SelectOption>("value", "key");
	//		ddcSpecies = new DropDownChoice<SelectOption>("species",new Model(),Arrays.asList(spOptions),choiceRenderer){
	//
	//			protected boolean wantOnSelectionChangedNotifications() {
	//				return true;
	//			}
	//			protected void onSelectionChanged(final SelectOption newSelection){
	//				ddcVersion.updateModel();
	//			}
	//		};
	//		//->Assembly
	//		ddcVersion = new DropDownChoice<SelectOption>("version",new Model(),
	//				new LoadableDetachableModel<List<SelectOption>>() {
	//			@Override
	//			protected List<SelectOption> load() {
	//				SelectOption species = (SelectOption) ddcSpecies.getDefaultModelObject();
	//				if(null==species){
	//					return new ArrayList<SelectOption>();
	//				}
	//				else {
	//					List<SelectOption> allAssemblies = Arrays.asList(AssembliesAccess.getAssembliesBySpeciesIdSelectOpt(species.getKey()));
	//					List<SelectOption> addedAssemblies = new ArrayList<SelectOption>();
	//					SequenceControl gC = new SequenceControl((UserSession)getSession());
	//					for (SelectOption so : allAssemblies){
	//						if(gC.isCreatedOnJBrowsoR(so.getKey(),so.getValue())){
	//							addedAssemblies.add(so);
	//						}
	//					}
	//					return addedAssemblies;
	//				}
	//			}
	//		},
	//		choiceRenderer);
	//
	//		form.add(ddcSpecies);
	//		form.add(ddcVersion);
	//		
	//		
	//		
	//		//->url
	//		url = new TextArea<String>("url",new PropertyModel<String>(properties,"url"));
	//		url.setOutputMarkupId(true);
	//		form.add(url);
	//		AjaxButton clear = new AjaxButton("clear"){
	//			@Override
	//			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
	//				url.setModelObject(null);
	//			}
	//
	//		};
	//		form.add(clear);
	//	
	//		//->upload
	//		uploadField = new FileUploadField("upload");
	//		form.add(uploadField);
	//
	//		//->mail
	//		form.add(new CheckBox("send_mail",new PropertyModel(properties,"send_mail")));
	//		add(form);
	//
	//		
	//	}



}

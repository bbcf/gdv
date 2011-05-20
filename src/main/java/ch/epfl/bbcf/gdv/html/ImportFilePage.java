package ch.epfl.bbcf.gdv.html;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.html.utility.FormChecker;



public class ImportFilePage extends WebPage{

	protected final ValueMap properties = new ValueMap();
	private FileUploadField uploadField;
	private Form form;
	private TextArea<String> url;
	private boolean admin;

	


	public ImportFilePage(final BasePage projectPage, final ch.epfl.bbcf.gdv.html.utility.CustModalWindow importModal) {
		form = new Form("form");
		form.add(new AjaxButton("submit"){
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form_) {
				String url = properties.getString("url");
				boolean sendMail = properties.getBoolean("send_mail");
				int spId = importModal.getSpeciesId();
				int projectId = importModal.getProjectId();
				int sequenceId = importModal.getSequenceId();
				boolean admin = importModal.isAdmin();
				FormChecker checker = new FormChecker(form_,(UserSession)getSession());
				checker.checkImportFile(spId,url,uploadField);
				if(checker.isFormSubmitable()){
					boolean result = InputControl.processInputs(((UserSession)getSession()).getUser(),
							projectId,url,uploadField.getFileUpload(),sequenceId,spId,sendMail,admin,
							new ArrayList<Group>(),null,null);
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
}

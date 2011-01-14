package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.GroupControl;
import ch.epfl.bbcf.gdv.html.PreferencesPage.CustModalWindow;
import ch.epfl.bbcf.gdv.html.utility.FormChecker;

public class AddUserToGroupPage extends WebPage{

	private FeedbackPanel feedback;

	protected final ValueMap properties = new ValueMap();

	public AddUserToGroupPage(PreferencesPage groupPage, final CustModalWindow modal) {
		final Form form = new Form("form");
		form.add(new AjaxButton("submit"){
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form_) {
				String userMail = properties.getString("user_mail");
				FormChecker checker = new FormChecker(form, (UserSession)getSession());
				checker.checkMail(userMail);
				if(checker.isFormSubmitable()){
					GroupControl gc = new GroupControl((UserSession)getSession());
					gc.addUserToGroup(modal.getGroupId(),userMail);
					modal.close(target);
				}

			}
		});

		TextField text = new TextField("user_mail",new PropertyModel(properties,"user_mail"));
		form.add(text);

		feedback = new FeedbackPanel("feedback");
		form.add(feedback);
		add(form);
	}


}

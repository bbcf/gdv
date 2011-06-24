package ch.epfl.bbcf.gdv.html;


import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.html.utility.FormChecker;

public class AlternativeLoginPage extends BasePage{

	protected final ValueMap properties = new ValueMap();

	public AlternativeLoginPage(PageParameters p) {
		super(p);

		Label logH = new Label("login_header","login");
		add(logH);
		Form form = new Form("form"){
			@Override
			protected void onSubmit() {
				Application.debug("submitted : login : "+properties.getString("login_")+" ppwd : "+properties.getString("pwd_$"));

				String mail=properties.getString("login_");
				String pwd = properties.getString("pwd_$");
				UserSession session = (UserSession)getSession();
				if(session.authenticate(mail,"alternate")){
					setResponsePage(new ProjectPage(new PageParameters()));
				}




			}
		};


		TextField<String> login = new TextField<String>(
				"login",new PropertyModel<String>(properties,"login_"));
		Label log = new Label("log","mail    : ");
		PasswordTextField pwd = new PasswordTextField(
				"pwd",new PropertyModel<String>(properties,"pwd_$"));
		Label pw = new Label("pw","password : ");



		form.add(log);
		form.add(login);
		form.add(pwd);
		form.add(pw);
		add(form);






		Label logC = new Label("create_header","create user");
		add(logC);


		Form form2 = new Form("form2"){
			@Override
			protected void onSubmit() {
				FormChecker formchecker = new FormChecker(this, (UserSession)getSession());
				String mail = properties.getString("mail");
				formchecker.checkMail(mail);

				String p1=properties.getString("pwd2");
				String p2=properties.getString("pwd3");
				formchecker.checkPasswords(p1, p2);
				if(formchecker.isFormSubmitable()){
					int uid= UserControl.createNewUser(mail, properties.getString("login2"), "", "", "", "", "alternate");
					if(uid!=-1){
						info("you can now login with : login : "+properties.getString("mail")+" and password : "+p1);
					} else {
						error("user creation failed");
					}
				}
			}
		};


		PasswordTextField pwd2 = new PasswordTextField(
				"password",new PropertyModel<String>(properties,"pwd2"));
		Label pw2 = new Label("pw2","password : ");
		PasswordTextField pwd3 = new PasswordTextField(
				"password2",new PropertyModel<String>(properties,"pwd3"));
		Label pw3 = new Label("pw3","password : ");
		TextField<String> mail = new TextField<String>(
				"mail",new PropertyModel<String>(properties,"mail"));
		Label ml = new Label("ml","mail    : ");

		form2.add(mail);form2.add(ml);
		form2.add(pwd2);form2.add(pwd3);form2.add(pw2);form2.add(pw3);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		form2.add(feedback);
		add(form2);














	}

}

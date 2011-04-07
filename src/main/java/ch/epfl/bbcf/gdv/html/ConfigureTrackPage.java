package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.utility.ConfigureModalWindow;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;

public class ConfigureTrackPage extends WebPage{

	public ConfigureTrackPage(ProjectPage projectPage,
			ConfigureModalWindow configureModal) {
		final TrackWrapper track = configureModal.getTrack();
		
		for(String cp : Configuration.getGDVCSSFiles()){
			add(CSSPackageResource.getHeaderContribution(cp));
		}
		for(String cp : Configuration.getJavascriptFiles()){
			add(JavascriptPackageResource.getHeaderContribution(cp));
		}
		
		final String jsControl = "initGDV_configure();";

		add(new AbstractBehavior() {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.renderJavascript(jsControl,"js_control_"+track.getId());
			}
		}); 
		
		
		
		
		
		

		Form form = new Form("form");

		Label trackInfo = new Label("track_info","configure track");
		form.add(trackInfo);
		final AjaxEditableLabel<String> editableTrackName = new AjaxEditableLabel<String>("track_info2",
				new Model<String>(track.getName())){
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				TrackControl tc = new TrackControl((UserSession)getSession());
				Track newTrack = tc.getTrackById(track.getId());
				if(null!=newTrack && null!=track.getName() && !track.getName().equalsIgnoreCase("in process")){
					if(null!=getEditor().getInput()){
						tc.renameTrack(track.getId(),getEditor().getInput());
					}
				}
			}
		};
		form.add(editableTrackName);
		Label trackInfo3 = new Label("track_info3","datatype : "+track.getType());
		form.add(trackInfo3);
		Label trackInfo4 = new Label("track_info4","created at : "+track.getDate());
		form.add(trackInfo4);
		
		
		//QUALITATIVE
		TextField colorPicker = new TextField("colorPicker");
		colorPicker.setOutputMarkupPlaceholderTag(true);
		colorPicker.setVisible(false);
		
		form.add(colorPicker);
		
		
		
		//QUANTITATIVE
		
		
		
		
		add(form);
	}

}

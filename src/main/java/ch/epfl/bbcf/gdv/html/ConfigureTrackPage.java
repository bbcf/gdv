package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
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
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;

public class ConfigureTrackPage extends BasePage{

	public ConfigureTrackPage(PageParameters p) {
		super(p);

		/* get the track id from parameters */
		Integer trackId = p.getInt("id");
		if(trackId==null){
			String err="no track id in the request";
			PageParameters params = new PageParameters();
			params.put("err", err);
			throw new RestartResponseAtInterceptPageException(new ErrorPage(params));
		}
		/* check if user is authorized */
		if(!UserControl.checkUserAuthorizedToConfigureTrack(trackId)){
			String err="you are not authorized to configure this track";
			PageParameters params = new PageParameters();
			params.put("err", err);
			throw new RestartResponseAtInterceptPageException(new ErrorPage(params));
		}

		final Track track = TrackControl.getTrackById(trackId);

		/* build the page */
		Form form = new Form("form");



		//editable name 
		final AjaxEditableLabel<String> editableTrackName = new AjaxEditableLabel<String>("editable_name",
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

		//quantitative color input (link with a dojo color picker on the HTML page)
		TextField<String> colorInput = new TextField<String>("color_input1");
		colorInput.setOutputMarkupPlaceholderTag(true);
		form.add(colorInput);


		//qualitative color input (link with the same dojo color picker on the HTML page)
		TextField<String> colorInput2 = new TextField<String>("color_input2");
		colorInput2.setOutputMarkupPlaceholderTag(true);
		form.add(colorInput2);

		//qualitative extended
		//TODO select display for each types

		//datatype switch
		switch(track.getType()){
		case QUALITATIVE: 
			colorInput.setVisible(false);
			break;
		case  QUANTITATIVE: 
			colorInput2.setVisible(false);
			break;
		case QUALITATIVE_EXTENDED: 
			colorInput.setVisible(false);
			colorInput2.setVisible(false);
			break;
		}





		add(form);
	}

}

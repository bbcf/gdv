package ch.epfl.bbcf.gdv.html;

import java.util.Arrays;

import javax.swing.text.html.ListView;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO.STYLE_HEIGHT;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.html.database.DataProjectProvider;
import ch.epfl.bbcf.gdv.html.database.DataTrackConfigureProvider;
import ch.epfl.bbcf.gdv.html.utility.FormChecker;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;
import ch.epfl.bbcf.gdv.html.wrapper.ProjectWrapper;
import ch.epfl.bbcf.gdv.html.wrapper.StyleWrapper;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;

public class ConfigureTrackPage extends BasePage{

	protected final ValueMap properties = new ValueMap();

	public ConfigureTrackPage(PageParameters p) {
		super(p);

		add(JavascriptPackageResource.getHeaderContribution(Configuration.getJavascriptUrl()+"/js/gdv.js"));
		add(JavascriptPackageResource.getHeaderContribution(Configuration.getJavascriptUrl()+"/jslib/dojo/dojo.js"));
		add(JavascriptPackageResource.getHeaderContribution(Configuration.getJavascriptUrl()+"/jslib/dojo/jbrowse_dojo.js"));
		add(CSSPackageResource.getHeaderContribution(Configuration.getJavascriptUrl()+"/jslib/dojox/widget/ColorPicker/ColorPicker.css"));

		final String jsControl = "initGDV_configure();";

		add(new AbstractBehavior() {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.renderJavascript(jsControl,"js_control");
			}
		}); 


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

		/* get the track */
		final Track track = TrackControl.getTrackById(trackId);

		/* get the color of quantitative track (no use if qualitative)*/
		String params = track.getParameters();
		String color ="red";
		try {
			JSONObject j = new JSONObject(params);
			color = j.getString("color");
		} catch (JSONException e) {
		}
		/* build the page */
		Form form = new Form("form");



		/* editable name */ 
		final AjaxEditableLabel<String> editableTrackName = new AjaxEditableLabel<String>("editable_name",
				new Model<String>(track.getName())){
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				Track newTrack = TrackControl.getTrackById(track.getId());
				if(null!=newTrack && null!=track.getName() && !track.getName().equalsIgnoreCase("in process")){
					if(null!=getEditor().getInput()){
						TrackControl.renameTrack(track.getId(),getEditor().getInput());
					}
				}
			}
		};
		form.add(editableTrackName);

		/* color input (link with a dojo color picker on the HTML page) */
		TextField<String> colorInput1 = new TextField<String>("color_input1",new PropertyModel<String>(properties,"color_input"));
		colorInput1.setOutputMarkupPlaceholderTag(true);
		colorInput1.setMarkupId("color_input");
		colorInput1.setVisible(false);
		colorInput1.setModelObject(color);
		form.add(colorInput1);


		
	
		
		
		/* style chooser */
		final DataTrackConfigureProvider dtcp = new DataTrackConfigureProvider(track.getId());

		DataView<StyleWrapper> data = new DataView<StyleWrapper>("style_data",dtcp) {
			@Override
			protected void populateItem(Item<StyleWrapper> item) {
				StyleWrapper sw = item.getModelObject();
				/* type name */
				Label name = new Label("name",sw.getName());
				item.add(name);
				/* height */
				RadioChoice rc = new RadioChoice("height",Arrays.asList(STYLE_HEIGHT.values()));
				rc.setModelObject(sw.getStyle_height());
				item.add(rc);
				/* color */
				Label color = new Label("color",sw.getStyle_color().toString());
				item.add(color);
				
			}
		};
		
		data.setOutputMarkupPlaceholderTag(true);
		form.add(data);
		
		
		
		//qualitative extended
		//TODO select display for each types

		//datatype switch
		switch(track.getType()){
		case QUALITATIVE: 
		case  QUANTITATIVE: 
			data.setVisible(false);
			break;
		case QUALITATIVE_EXTENDED: 
			break;
		}

		Button sub = new Button("sub"){
			public void onSubmit(){

				switch(track.getType()){
				case QUALITATIVE: 
					Application.debug("configure track qualitative");
					break;
				case  QUANTITATIVE: 
					Application.debug("configure track quantitative");
					String color = properties.getString("color_input");
					if(color!=null){
						TrackControl.buildTrackParams(track, color);
					}
					break;
				case QUALITATIVE_EXTENDED: 
					Application.debug("configure track qualitative extended");
					break;
				}
			}
		};
		form.add(sub);
		



		add(form);
	}

}

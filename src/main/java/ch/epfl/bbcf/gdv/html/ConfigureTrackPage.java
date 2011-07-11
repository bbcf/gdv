package ch.epfl.bbcf.gdv.html;

import java.util.Arrays;
import java.util.List;


import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
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
import ch.epfl.bbcf.gdv.control.model.StyleControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.html.database.DataStyleProvider;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;
import ch.epfl.bbcf.gdv.html.wrapper.StyleWrapper;

public class ConfigureTrackPage extends BasePage{

	protected final ValueMap properties = new ValueMap();
	
	WebMarkupContainer colorList;
	ListView<String> color_list;
	private StyleWrapper curSW;
	private DataStyleProvider dtcp;	
	
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
		/* get user id  */
		final int userId = ((UserSession)getSession()).getUserId();

		/* check if user is authorized */
		if(!UserControl.checkUserAuthorizedToConfigureTrack(userId,trackId)){
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





		/* color input (link with a dojo color picker on the HTML page, not visible) */
		TextField<String> colorInput1 = new TextField<String>("color_input1",new PropertyModel<String>(properties,"color_input"));
		colorInput1.setOutputMarkupPlaceholderTag(true);
		colorInput1.setMarkupId("color_input");
		colorInput1.setVisible(false);
		colorInput1.setModelObject(color);
		form.add(colorInput1);




		/* color picker */
		WebMarkupContainer colorPicker = new WebMarkupContainer("color_picker");
		colorPicker.setOutputMarkupPlaceholderTag(true);
		colorPicker.setMarkupId("color_picker");
		colorPicker.setVisible(false);
		add(colorPicker);



	

		/* color listview */
		colorList = new WebMarkupContainer("cont");
		colorList.setOutputMarkupPlaceholderTag(true);
		
		List<String> colors = StyleControl.getTypesColors();
		color_list = new ListView<String>("color_list", colors) {
			@Override
			protected void populateItem(ListItem<String> item) {
				final String c = item.getModelObject();
				Label color = new Label("color",new Model<String>(c));
				color.add(new SimpleAttributeModifier("style","color:"+c));
				color.add(new AjaxEventBehavior("onClick"){
					@Override
					protected void onEvent(AjaxRequestTarget target) {
						StyleWrapper sw = curSW;
						sw.setStyle_color(c);
						StyleControl.setStyleForUserAndType(userId, sw.getType(), sw.getStyleObject());
						colorList.setVisible(false);
						Application.debug("set color to "+c);
						dtcp.detach();
					}
				});
				item.add(color);
				
			}
		   
		};
		
		color_list.setOutputMarkupPlaceholderTag(true);
		colorList.add(color_list);
		colorList.setVisible(false);
		add(colorList);

		
		
		
		
		
		
		
		
		/* data style chooser
		 * for qualitative extended tracks
		 * (for track types) 
		 */
		dtcp = new DataStyleProvider(userId,track.getId());
		DataView<StyleWrapper> data = new DataView<StyleWrapper>("style_data",dtcp) {
			@Override
			protected void populateItem(Item<StyleWrapper> item) {
				final StyleWrapper sw = item.getModelObject();
				/* type name */
				Label name = new Label("name",sw.getType().getName());
				item.add(name);
				/* height */
				final RadioChoice<STYLE_HEIGHT> rc = new RadioChoice<STYLE_HEIGHT>(
						"height",new Model<STYLE_HEIGHT>(), Arrays.asList(STYLE_HEIGHT.values())){
					
					protected boolean wantOnSelectionChangedNotifications() {
						return true;
					}
					public void onSelectionChanged() {
						super.onSelectionChanged();
						Application.debug("OSC "+this.getDefaultModelObject());
						String newSelection = this.getDefaultModelObjectAsString();
						sw.setStyle_height(newSelection);
						StyleControl.setStyleForUserAndType(userId, sw.getType(), sw.getStyleObject());
					}
				};
				rc.setModelObject(sw.getStyle_height());
				item.add(rc);
				/* color */
				Label color = new Label("color",sw.getStyle_color().toString());
				color.add(new SimpleAttributeModifier("style","color:"+sw.getStyle_color().name()));
				item.add(color);
				color.add(new AjaxEventBehavior("onClick"){
					@Override
					protected void onEvent(AjaxRequestTarget target) {
						Application.debug("set color of "+sw.getStyleObject().getId());
						curSW = sw;
						colorList.setVisible(true);
						color_list.setVisible(true);
					}
					
				});
			}
		};
		data.setOutputMarkupPlaceholderTag(true);
		form.add(data);
		

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

package ch.epfl.bbcf.gdv.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO.STYLE_COLOR;
import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO.STYLE_HEIGHT;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Style;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Type;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.StyleControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.html.database.DataStyleProvider;
import ch.epfl.bbcf.gdv.html.database.DataTrackProvider;
import ch.epfl.bbcf.gdv.html.database.DataTrackProvider2;
import ch.epfl.bbcf.gdv.html.wrapper.StyleWrapper;

public class ConfigureTrackPage extends BasePage{


	private DataStyleProvider dtcp;	
	private Track curTrack;

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


		/* get the project id from parameters */
		Integer trackId = p.getInt("id");
		if(trackId==null){
			String err="no project id in the request";
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


		/* get the track*/
		final Track track = TrackControl.getTrackById(trackId);


		
		
		
		/* UTILITY */
		

		/* color input (link with a dojo color picker on the HTML page, not visible) 
		 * in order to select arbitrary color for a
		 * quantitative track
		 */
		final TextField<String> colorInput1 = new TextField<String>("color_input1",new Model<String>());
		colorInput1.setOutputMarkupPlaceholderTag(true);
		colorInput1.setMarkupId("color_input");
		colorInput1.setVisible(false);
		OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior(){
			@Override
			protected void onUpdate(AjaxRequestTarget target){
				String color = colorInput1.getDefaultModelObjectAsString();
				updateColor(curTrack, color);
			}
		};
		colorInput1.add(onChangeAjaxBehavior);
		add(colorInput1);

		/* color picker 
		 * the dojo color picker
		 * to have a reference here in the 
		 * java container 
		 */
		final WebMarkupContainer colorPicker = new WebMarkupContainer("color_picker");
		colorPicker.setOutputMarkupPlaceholderTag(true);
		colorPicker.setMarkupId("color_picker");
		colorPicker.setVisible(false);
		add(colorPicker);


		
		

//		/* color listview 
//		 * all color that are possible 
//		 * for qualitative extended tracks 
//		 */
//		color_cont = new WebMarkupContainer("color_cont");
//		DataColorsProvider dcp = new DataColorsProvider();
//		DataView<String> datacolors = new DataView<String>("colors_data",dcp) {
//			@Override
//			protected void populateItem(final Item<String> item) {
//				String c = item.getModelObject();
//				final Label color = new Label("color_name",c);
//				color.add(new SimpleAttributeModifier("style","color:"+c));
//				color.add(new AjaxEventBehavior("onClick"){
//					@Override
//					protected void onEvent(AjaxRequestTarget target) {
//						StyleWrapper sw = curSW;
//						sw.setStyle_color(color.getDefaultModelObject().toString());
//						Style style = StyleControl.getStyleByStyle(sw.getStyle_color(), sw.getStyle_height());
//						StyleControl.setStyleForUserAndType(userId, sw.getType(),style);
//						color_cont.setVisible(false);
//						target.addComponent(color_cont);
//						dtcp.detach();
//					}
//				});
//				item.add(color);
//			}
//			
//		};
//		
//		color_cont.setOutputMarkupPlaceholderTag(true);
//		color_cont.setVisible(false);
//		color_cont.add(datacolors);
//		add(color_cont);

		
		/* PAGE OBJECTS */
		
		
		
		
		
		/* editable name */
		AjaxEditableLabel<String> editableTrackName = new AjaxEditableLabel<String>("edit_name",
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
		add(editableTrackName);


		/* quantitative color */
		String params = track.getParameters();
		String colorstr ="red";
		try {
			JSONObject j = new JSONObject(params);
			colorstr = j.getString("color");
		} catch (JSONException e) {
		}
		Label color = new Label("color",colorstr);
		color.add(new SimpleAttributeModifier("style","color:"+colorstr));
		color.add(new AjaxEventBehavior("onClick"){
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				Application.debug("on click on color List");
				colorPicker.setVisible(true);
				target.addComponent(colorInput1);
			}
		});
		add(color);




		/* qualitative extended tracks */	






		
		/* data style chooser
		 * for qualitative extended tracks
		 * (for track types) 
		 */
		List<Type> types = TrackControl.getTrackTypes(trackId);
		dtcp = new DataStyleProvider(userId,types);
		DataView<StyleWrapper> data = new DataView<StyleWrapper>("style_data",dtcp) {
			@Override
			protected void populateItem(Item<StyleWrapper> item) {
				final StyleWrapper sw = item.getModelObject();
				/* type name */
				Label name = new Label("name",sw.getType().getName());
				item.add(name);
				/* height */
				List<STYLE_HEIGHT> spOptions = Arrays.asList(STYLE_HEIGHT.values());
				DropDownChoice<STYLE_HEIGHT> ddcChoice = new DropDownChoice<STYLE_HEIGHT>(
						"height",new Model<STYLE_HEIGHT>(),spOptions){
					protected boolean wantOnSelectionChangedNotifications() {
						return true	;
					}
					protected void onSelectionChanged(final STYLE_HEIGHT newSelection){
						Application.debug("style changed to :"+newSelection);
						Style newStyle = StyleControl.getStyleByStyle(sw.getStyle_color(), newSelection);
						StyleControl.setStyleForUserAndType(userId, sw.getType(), newStyle);
						dtcp.detach();
					}
				};
				ddcChoice.setModelObject(sw.getStyle_height());
				item.add(ddcChoice);

				//				final RadioChoice<STYLE_HEIGHT> rc = new RadioChoice<STYLE_HEIGHT>(
				//						"height",new Model<STYLE_HEIGHT>(), Arrays.asList(STYLE_HEIGHT.values())){
				//
				//					protected boolean wantOnSelectionChangedNotifications() {
				//						return true;
				//					}
				//					public void onSelectionChanged() {
				//						super.onSelectionChanged();
				//						String newSelection = this.getDefaultModelObjectAsString();
				//						sw.setStyle_height(newSelection);
				//						Style newStyle = StyleControl.getStyleByStyle(sw.getStyle_color(), sw.getStyle_height());
				//						StyleControl.setStyleForUserAndType(userId, sw.getType(), newStyle);
				//					}
				//				};
				//				rc.setModelObject(sw.getStyle_height());
				//				item.add(rc);

				/* color */
				List<STYLE_COLOR> colors = StyleControl.getTypesColors();
				DropDownChoice<STYLE_COLOR> ddcColor = new DropDownChoice<STYLE_COLOR>(
						"color",new Model<STYLE_COLOR>(),colors){
					protected boolean wantOnSelectionChangedNotifications() {
						return true	;
					}
					protected void onSelectionChanged(final STYLE_COLOR newSelection){
						Application.debug("style changed to :"+newSelection);
						Style newStyle = StyleControl.getStyleByStyle(newSelection, sw.getStyle_height());
						StyleControl.setStyleForUserAndType(userId, sw.getType(), newStyle);
						dtcp.detach();
					}
					@Override
					protected void onComponentTag(final ComponentTag tag){
						super.onComponentTag(tag);
						Application.debug(tag.getId()+"  "+tag.getName()+"   "+tag.getPath()+"  "+tag.getAttributes());
						tag.put("style","color:"+tag.getName());
					}
				};
				ddcColor.setModelObject(sw.getStyle_color());
				item.add(ddcColor);
				
				
				

				/* color */
//				Label color = new Label("color",sw.getStyle_color().toString());
//				color.add(new SimpleAttributeModifier("style","color:"+sw.getStyle_color().name()));
//				item.add(color);
//				color.add(new AjaxEventBehavior("onClick"){
//					private static final long serialVersionUID = -5233105301407583504L;
//
//					@Override
//					protected void onEvent(AjaxRequestTarget target) {
//						curSW = sw;
//						color_cont.setVisible(true);
//						target.addComponent(color_cont);
//					}
//
//				});
			}
		};
		data.setOutputMarkupPlaceholderTag(true);
		add(data);
	}



	private void updateColor(Track track,String color){
		switch(track.getType()){
		case QUALITATIVE: 
			Application.debug("configure track qualitative");
			break;
		case  QUANTITATIVE: 
			Application.debug("configure track quantitative");
			if(color!=null){
				TrackControl.buildTrackParams(track, color);
			}
			break;
		case QUALITATIVE_EXTENDED: 
			Application.debug("configure track qualitative extended");
			break;
		}
	}

}

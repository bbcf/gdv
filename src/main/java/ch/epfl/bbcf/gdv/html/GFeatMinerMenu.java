//package ch.epfl.bbcf.gdv.html;
//
//import java.util.List;
//import java.util.Set;
//
//import org.apache.wicket.ajax.AjaxRequestTarget;
//import org.apache.wicket.ajax.markup.html.form.AjaxButton;
//import org.apache.wicket.behavior.SimpleAttributeModifier;
//import org.apache.wicket.markup.html.basic.Label;
//import org.apache.wicket.markup.html.form.Button;
//import org.apache.wicket.markup.html.form.Form;
//import org.apache.wicket.markup.html.list.ListItem;
//import org.apache.wicket.markup.html.list.ListView;
//import org.apache.wicket.markup.html.panel.Panel;
//import org.apache.wicket.model.IModel;
//
//import ch.epfl.bbcf.gdv.access.database.pojo.Track;
//import ch.epfl.bbcf.gdv.config.Configuration;
//import ch.epfl.bbcf.gdv.model.gfeatminer.GFeatMinerElement;
//
//public class GFeatMinerMenu extends Panel{
//
//	public GFeatMinerMenu(String id,Set<Track> tracks) {
//		super(id);
//
//		Label title = new Label("gfm","gFeatMiner");
//		add(title);
//		Form form = new Form("gfm_form");
//		ListView<GFeatMinerElement> entries = new ListView<GFeatMinerElement>
//		("items",Configuration.getGFeatMinerElements()){
//			@Override
//			protected void populateItem(ListItem<GFeatMinerElement> item) {
//				final GFeatMinerElement el = item.getModelObject();
//				Button button = new AjaxButton("gfeat_but"){
//					@Override
//					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
//						el.doJob(target);
//					}
//				};
//				button.add(new SimpleAttributeModifier("value",el.getLabel()));
//				item.add(button);
//			}
//		};
//		form.add(entries);
//		add(form);
//	}
//}

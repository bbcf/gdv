package ch.epfl.bbcf.gdv.html;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.utility.ImageRessource;

public class ImageDisplayer extends BasePage{

	public ImageDisplayer(PageParameters p) {
		super(p);


		Integer imageId = p.getInt("id");
		if(imageId==null){
			String err="no image id in the request";
			PageParameters params = new PageParameters();
			params.put("err", err);
			throw new RestartResponseAtInterceptPageException(new ErrorPage(params));
		}

		if(!UserControl.checkUserAuthorizedToViewImage(imageId)){
			String err="you are not authorized to view this result";
			PageParameters params = new PageParameters();
			params.put("err", err);
			throw new RestartResponseAtInterceptPageException(new ErrorPage(params));
		}
		
		
		List<String> paths = new ArrayList<String>();

		File directory = new File(Configuration.getgFeatMinerDirectory()+"/"+imageId);
		if(directory.exists()){
			File[] files = directory.listFiles();
			if(null!=files){
				for(File f : files) {
					paths.add(f.getAbsolutePath());
				}
			}
		}

		ListView<String> listview = new ListView<String>("images", paths) {
			@Override
			protected void populateItem(ListItem<String> item) {
				String path = item.getModelObject();
				item.add(new Image("image",new ImageRessource(path)));
			}

		};
		add(listview);
	}
}

package ch.epfl.bbcf.gdv.html.utility;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class MenuElement extends Panel{
	
	public MenuElement(String id, IModel<?> model,Label label,Image image) {
		super(id, model);
		add(label);
		add(image);
	}
	
}

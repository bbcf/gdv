package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;

import ch.epfl.bbcf.gdv.config.Configuration;

public class PNGPopupPage extends WebPage{

	public PNGPopupPage(){
		Image image = new Image("_png","");
		image.add(new SimpleAttributeModifier("src","../public/images/test.png"));
		add(image);
	}
}

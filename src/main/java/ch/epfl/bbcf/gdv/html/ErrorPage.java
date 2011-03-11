package ch.epfl.bbcf.gdv.html;

import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

import ch.epfl.bbcf.gdv.config.Application;

public class ErrorPage extends BasePage{


	public ErrorPage(PageParameters p) {
		super(p);
		String error = p.getString("err");
		if(null==error || error.equalsIgnoreCase("")){
			error = "page doesn't exist";
		}
		add(new Label("error",error));
		Link l=new Link("link"){
			@Override
			public void onClick() {
				setResponsePage(HomePage.class);
			}
			
		};
		l.add(new Label("ret","return to Home"));
		add(l);
	}

}

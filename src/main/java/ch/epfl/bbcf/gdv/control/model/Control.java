package ch.epfl.bbcf.gdv.control.model;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;

/**
 * Control between the view and the application process
 * @author jarosz
 *
 */
public class Control extends Thread{
	
	protected UserSession session;
	
	public Control(UserSession session){
		this.session = session;
	}


}

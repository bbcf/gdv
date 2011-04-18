package ch.epfl.bbcf.gdv.control.http;


import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.command.Command;
import ch.epfl.bbcf.gdv.control.http.command.PostAccess;
import ch.epfl.bbcf.gdv.control.http.command.TrackError;
import ch.epfl.bbcf.gdv.control.http.command.TrackParsingSuccess;
import ch.epfl.bbcf.gdv.control.http.command.TrackStatus;

public class FrontController {

	private static Logger log = Logs.initPostLogger(FrontController.class.getName());
	protected RequestParameters params;
	protected PrintWriter out;

	public FrontController(RequestParameters params,PrintWriter out){
		this.params = params;
		this.out = out;
	}

	public void doRequest() {
		if(params.getId()!=null){
			Command command = null;
			//CHANGE STATUS OF A TRACK
			if(params.getId().equalsIgnoreCase("track_status")){
				command = new TrackStatus(params,out);
				//ERROR IN TRACK PROCESSING
			} else if(params.getId().equalsIgnoreCase("track_error")){
				command = new TrackError(params,out);
				//TRANSFORM TO SQLITE SUCCED
			} else if(params.getId().equalsIgnoreCase("track_parsing_success")){
				command = new TrackParsingSuccess(params,out);
				//POST ACCESS TO GDV
			} else if(params.getId().equalsIgnoreCase(Configuration.getGdv_post_access())){
				command = new PostAccess(params,out);
			}
			if(null!=command){
				command.doRequest();
			} else {
				log.error("no command");
				new AbortWithHttpStatusException(400,true);
			}
		} else {
			log.error("no id");
			throw new AbortWithHttpStatusException(400,true);
		}

	}
}

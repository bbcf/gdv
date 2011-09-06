package ch.epfl.bbcf.gdv.control.http;


import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.control.http.command.Command;
import ch.epfl.bbcf.gdv.control.http.command.JobAccess;
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
			switch(params.getId()){
			case gdv_post : command = new PostAccess(params,out);
			break;
			case job : command = new JobAccess(params,out);
			break;
			case track_error: command = new TrackError(params,out);
			break;
			case track_status: command = new TrackStatus(params,out);
			break;
			case track_success: command = new TrackParsingSuccess(params,out);
			break;
			default:new AbortWithHttpStatusException(400,true);
			}
			command.doRequest();
		} else {
			throw new AbortWithHttpStatusException(400,true);
		}

	}
}

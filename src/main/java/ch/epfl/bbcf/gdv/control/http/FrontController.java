package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.command.Command;
import ch.epfl.bbcf.gdv.control.http.command.PostAccess;
import ch.epfl.bbcf.gdv.control.http.command.TrackError;
import ch.epfl.bbcf.gdv.control.http.command.TrackParsingSuccess;
import ch.epfl.bbcf.gdv.control.http.command.TrackStatus;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.formats.json.JSONProcessor;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.mail.Sender;

public class FrontController {

	private static Logger log = Logs.initPOSTLogger();
	private RequestParameters params;
	private UserSession session;
	private WebResponse webResponse;

	public FrontController(RequestParameters params,UserSession session, WebResponse webResponse){
		this.session = session;
		this.params = params;
		this.webResponse = webResponse;
	}

	public void doRequest() {
		if(params.getId()!=null){
			Command command = null;
			//CHANGE STATUS OF A TRACK
			if(params.getId().equalsIgnoreCase("track_status")){
				command = new TrackStatus(session,params,webResponse);
				//ERROR IN TRACK PROCESSING
			} else if(params.getId().equalsIgnoreCase("track_error")){
				command = new TrackError(session,params,webResponse);
				//TRANSFORM TO SQLITE SUCCED
			} else if(params.getId().equalsIgnoreCase("track_parsing_success")){
				command = new TrackParsingSuccess(session, params, webResponse);
				//HTC
			} else if(params.getId().equalsIgnoreCase("hts3cseq")){
				command = new PostAccess(session, params, webResponse);
				//NEW HTC3CSEQ PROJECT
			}
			if(null!=command){
				command.doRequest();
			} else {
				new AbortWithHttpStatusException(400,true);
			}
		} else {
			throw new AbortWithHttpStatusException(400,true);
		}

	}
}

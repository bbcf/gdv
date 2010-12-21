package ch.epfl.bbcf.gdv.control.http.command;

import org.apache.wicket.protocol.http.WebResponse;

import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.TrackControl;

public class TrackError extends Command{

	public TrackError(UserSession session, RequestParameters params,
			WebResponse webResponse) {
		super(session, params, webResponse);
	}

	@Override
	protected void initLog() {
		log = Logs.initLogger(TrackError.class.getName());
	}

	@Override
	public void doRequest() {
		checkParams(params.getTrackId(),params.getMessage(),params.getType());
		int trackId = Integer.parseInt(params.getTrackId());
		if(params.getType().equalsIgnoreCase("ext")) {
			TrackControl.updateTrack(trackId,params.getMessage());
		} else if(params.getType().equalsIgnoreCase("md5")){
			TrackControl.updateTrack(trackId,"server error (cannot get md5 of the file)");
		} else if(params.getType().equalsIgnoreCase("parsing")){
			TrackControl.updateTrack(trackId,"error when parsing the file : "+params.getMessage());
		} else if(params.getType().equalsIgnoreCase("chrList")){
			TrackControl.updateTrack(trackId,"server error ("+params.getMessage()+")");
		}
		
	}

}

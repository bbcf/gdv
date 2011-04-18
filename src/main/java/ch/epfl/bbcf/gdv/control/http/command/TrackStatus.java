package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;

import org.apache.wicket.protocol.http.WebResponse;

import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.TrackControl;

public class TrackStatus extends Command{

	

//	@Override
//	protected void initLog() {
//		log = Logs.initLogger(TrackStatus.class.getName());
//	}

	public TrackStatus(RequestParameters params, PrintWriter out) {
		super(params, out);
	}

	@Override
	public void doRequest() {
		checkParams(params.getTrackId());
		int trackId = Integer.parseInt(params.getTrackId());
		String status = params.getMessage();
		try {
			TrackControl.updatePercentage(trackId, Integer.parseInt(status));
		}catch(NumberFormatException e){
			TrackControl.updateTrack(trackId,status);
		}
	}

	
	
}

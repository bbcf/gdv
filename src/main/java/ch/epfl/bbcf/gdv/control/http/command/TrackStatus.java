package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;

import org.apache.wicket.protocol.http.WebResponse;

import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.JobControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;

public class TrackStatus extends Command{

	


	public TrackStatus(RequestParameters params, PrintWriter out) {
		super(params, out);
	}

	@Override
	public void doRequest() {
		checkParams(params.getJobId());
		checkParams(params.getData());
		Track track = TrackControl.getTrackIdWithJobId(params.getJobId());
		try {
			TrackControl.updatePercentage(track.getId(), Integer.parseInt(params.getData()));
		}catch(NumberFormatException e){
			//JobControl.updateTrackJo
			TrackControl.updateTrack(track.getId(),params.getData());
		}
	}

	
	
}

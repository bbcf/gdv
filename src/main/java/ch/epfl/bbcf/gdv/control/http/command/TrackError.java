package ch.epfl.bbcf.gdv.control.http.command;

import java.io.File;
import java.util.List;

import org.apache.wicket.protocol.http.IRequestLogger;
import org.apache.wicket.protocol.http.RequestLogger.SessionData;
import org.apache.wicket.protocol.http.WebResponse;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;

public class TrackError extends Command{

	public TrackError(UserSession session, RequestParameters params,
			WebResponse webResponse) {
		super(session, params, webResponse);
	}

//	@Override
//	protected void initLog() {
//		log = Logs.initLogger(TrackError.class.getName());
//	}

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
		//don't delete the track, just the input
		//let the user delete the track
		
//		Application.error("deleting track : "+trackId);
		TrackControl tc = new TrackControl(session);
//		try {
//			Thread.sleep(30000);
//		} catch (InterruptedException e) {
//			Application.error(e);
//		}
		
		//TrackControl.deleteTrack(trackId);
		Track track = tc.getTrackById(trackId);
		InputControl ic = new InputControl(session);
		ic.removeInput(track.getInput());
		FileManagement.deleteDirectory(
				new File(
						Configuration.getFilesDir()+"/"+track.getName()));
		
	}

}

package ch.epfl.bbcf.gdv.control.http.command;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.mail.Sender;

public class TrackParsingSuccess extends Command{

	private static Logger log = Logs.initPOSTLogger(TrackParsingSuccess.class.getName());
	
	public TrackParsingSuccess(UserSession session, RequestParameters params,
			WebResponse webResponse) {
		super(session, params, webResponse);
	}

	@Override
	public void doRequest() {
		checkParams(params.getType(),params.getDb(),params.getUsermail());
		if(params.getType().equalsIgnoreCase("quantitative")){
			Application.debug("writing new job to calcul score on "+params.getDb());
			SQLiteAccess.writeNewJobCalculScores(params.getTrackId(),params.getDb(),Configuration.getFilesDir(),params.getDb(),Configuration.getTracks_dir(),"0",params.getUsermail());
		} else if(params.getType().equalsIgnoreCase("qualitative")){
			//UPDATE qualitatif

			log.debug("db "+params.getDb()+" completed");
			TrackControl.updateTrack(Integer.parseInt(params.getTrackId()),"completed");
			if(!params.getUsermail().equalsIgnoreCase("nomail")){
				Sender.sendMessage(" File processed ","Your file has been processed by GDV you can now browse your projects at "+Configuration.getProjectUrl() , false, params.getUsermail());
			}
		} else {
			throw new AbortWithHttpStatusException(400,true);
		}
	}

}

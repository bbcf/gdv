package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;

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

	public TrackParsingSuccess(RequestParameters params, PrintWriter out) {
		super(params, out);
	}

	private static Logger log = Logs.initPostLogger(TrackParsingSuccess.class.getName());
	
	@Override
	public void doRequest() {
		checkParams(params.getType(),params.getDb(),params.getUsermail());
		if(params.getType().equalsIgnoreCase("quantitative")){
			Application.debug("writing new job to calcul score on "+params.getDb());
			SQLiteAccess access = new SQLiteAccess(Configuration.getCompute_scores_daemon());
			access.writeNewJobCalculScores(params.getTrackId(),params.getDb(),Configuration.getFilesDir(),params.getDb(),Configuration.getTracks_dir(),"0",params.getUsermail(),
					Configuration.getGdv_appli_proxy()+"/post",Configuration.getTmp_dir());
			access.close();
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

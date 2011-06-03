package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.JobControl;
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
		Application.debug("TPS :job number  "+params.getJobId());
		checkParams(params.getJobId());
		if(null==params.getDbType()){
			throw new AbortWithHttpStatusException(400,true);
		}
		switch(params.getDbType()){

		case qualitative :
			JobControl.updateTrackJobSuccess(params.getJobId());
			break;


		case quantitative :
			if(params.getData().equalsIgnoreCase("end")){
				JobControl.updateTrackJobSuccess(params.getJobId());
			} else {
				Track track = TrackControl.getTrackIdWithJobId(params.getJobId());
				SQLiteAccess access = new SQLiteAccess(Configuration.getCompute_scores_daemon());
				access.writeNewJobCalculScores(params.getJobId(),track.getInput(),Configuration.getFilesDir(),track.getInput(),Configuration.getTracks_dir(),0,"nomail",
						Configuration.getGdv_appli_proxy()+"/post",Configuration.getTmp_dir());
				access.close();
			}
			break;
		default:
			throw new AbortWithHttpStatusException(400,true);

		}
	}

}

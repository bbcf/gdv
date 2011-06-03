package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;


import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.JobControl;

public class TrackError extends Command{

	
public TrackError(RequestParameters params, PrintWriter out) {
		super(params, out);
	}


	@Override
	public void doRequest() {
		checkParams(params.getData());
		checkParams(params.getJobId());
		JobControl.updateTrackJobError(params.getJobId(), params.getData());
	}

}

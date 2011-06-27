package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;


import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;

public abstract class Command {

	public enum ID {job,gdv_post,track_error,track_success,track_status};
	public enum ACTION {new_selection,gfeatminer,status,gfeatresponse};
	public enum DB_TYPE {qualitative,quantitative};
	public enum STATUS {error,running,success};
	
	protected RequestParameters params;
	protected PrintWriter out;
	protected static Logger log = Logs.initPostLogger(Command.class.getName());

	public Command(RequestParameters params,PrintWriter out) {
		this.params = params;
		this.out=out;
	}

	public abstract void doRequest();

	protected void checkParams(String... params) {
		for(String p : params){
			if(null==p){
				failed("missing param(s)");
			}
		}

	}
	protected void checkParams(Integer ... params) {
		for(Integer i : params){
			if(null==i){
				failed("missing param(s)");
			}
		}

	}

	protected void failed(String message){
		out.write(message);
		out.close();
	}

	protected void success(String mess){
		out.write(mess);
		out.close();
	}
	protected void success(final JSONObject json) {
		out.write(json.toString());
		out.close();
	}
	protected void success(boolean b) {
		if(b){
			out.write("true");
		} else {
			out.write("false");
		}
		out.close();
	}


	protected void error(JSONException e) {
		out.write(e.getMessage());
		out.close();
	}

	protected void error(Exception e) {
		out.write(e.getMessage());
		out.close();
	}

	protected void error(String mess) {
		out.write("{error:"+mess+"}");
		out.close();
	}
}

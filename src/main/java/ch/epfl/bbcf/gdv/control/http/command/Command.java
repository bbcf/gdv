package ch.epfl.bbcf.gdv.control.http.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;

public abstract class Command {

	protected RequestParameters params;
	protected PrintWriter out;
	protected static Logger log = Logs.initPostLogger(Command.class.getName());

	public Command(RequestParameters params,PrintWriter out) {
		this.params = params;
		this.out=out;
	}

	//protected abstract void initLog();
	public abstract void doRequest();

	protected void checkParams(String... params) {
		for(String p : params){
			if(null==p){
				failed("missing param(s)");
			}
		}

	}

	protected void failed(String message){
		out.write(message);
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
}

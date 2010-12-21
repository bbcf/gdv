package ch.epfl.bbcf.gdv.control.http.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;

public abstract class Command {

	protected UserSession session;
	protected RequestParameters params;
	protected WebResponse webResponse;
	
	protected Logger log;

	public Command(UserSession session, RequestParameters params, WebResponse webResponse) {
		this.session = session;
		this.params = params;
		this.webResponse = webResponse;
	}

	protected abstract void initLog();
	public abstract void doRequest();
	
	protected void checkParams(String... params) {
		for(String p : params){
			if(null==p){
				log.error("params needed == null");
				throw new AbortWithHttpStatusException(400,true);
			}
		}

	}


	protected void success(int id){
		webResponse.getHttpServletResponse().setContentType("text/plain");
		try {
			webResponse.getHttpServletResponse().getOutputStream().print(id);
		} catch (IOException e) {
			log.error(e);
		}
		success();
	}
	protected void success(){
		webResponse.getHttpServletResponse().setStatus(HttpServletResponse.SC_OK) ;
	}
	
	
}
package ch.epfl.bbcf.gdv.config;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.request.ClientInfo;
import org.apache.wicket.request.IRequestCycleProcessor;

public class MyRequestCycle extends RequestCycle{

	protected MyRequestCycle(Application application, Request request,
			Response response) {
		super(application, request, response);
	}

	@Override
	public IRequestCycleProcessor getProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ClientInfo newClientInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void redirectTo(Page arg0) {
		// TODO Auto-generated method stub
		
	}

}

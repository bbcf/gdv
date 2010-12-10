package ch.epfl.bbcf.gdv.html;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;


import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.http.FrontController;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.TrackControl;

public class PostPage extends WebPage{

	private Logger log = Logs.initPOSTLogger();
	public PostPage(PageParameters p) {
		super(p);
		String post = "POST => ";
		Map<String, String[]> map = ((WebRequestCycle) RequestCycle.get()).getRequest().getParameterMap();
		for (Entry<String, String[]> entry : map.entrySet()){
			post+="- "+entry.getKey();
			for(String str : entry.getValue()){
				post+=" : "+str+" ";
			}
		}
		log.debug(post);
		RequestParameters params = new RequestParameters(map);
		FrontController fc = new FrontController(params,(UserSession)getSession(),
				((WebResponse)getResponse()));
		fc.doRequest();
		
		
//		Params params = new Params(map);
//		if(params.getId()!=null){
//			if(params.getId().equalsIgnoreCase("track_status")){
//				int trackId = Integer.parseInt(params.getTrackId());
//				String status = params.getMessage();
//				try {
//					TrackControl.updatePercentage(trackId, Integer.parseInt(status));
//				}catch(NumberFormatException e){
//					TrackControl.updateTrack(trackId,status);
//				}
//
//			} else if(params.getId().equalsIgnoreCase("track_error")){
//				int trackId = Integer.parseInt(params.getTrackId());
//				String error = params.getMessage();
//				TrackControl.updateTrack(trackId, error);
//			}
//		}
	}







}

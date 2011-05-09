package ch.epfl.bbcf.gdv.access.gfeatminer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.bbcfutils.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;

public class GFeatMinerAccess {

	private static final String GFEATMINER_VERSION="1.0.0";
	private static final String GFEATMINER_CONTEXT = "GFMserver";
	private static Logger log = Logs.initLogger("gFeatMiner.log",GFeatMinerAccess.class);
	/**
	 * Send the map of parameters to GFeatMiner
	 * @param parameters the parameters
	 */
	public static void sendToGFeatMiner(Map<String, String> parameters){
			String body = "";
			for(Map.Entry<String, String> entry : parameters.entrySet()){
				body+=entry.getKey()+"="+entry.getValue()+"&";
			}
			body=body.substring(0, body.length()-1);
			log.debug("sendMapToGFMserver :");
			log.debug(body);
			Application.debug("sendMapToGFMserver :");
			Application.debug(body);
			try {
				InternetConnection.sendPOSTConnection(
						getGFMUrl(), body, InternetConnection.MIME_TYPE_FORM_APPLICATION);
			} catch (IOException e) {
				log.error(e);
			}
	}
	
	
	/**
	 * the url where is located the gFeatMiner server
	 * @return
	 */
	public static final String getGFMUrl(){
		return Configuration.getGdvTomcatServ()+"/"+GFEATMINER_CONTEXT;
	}
	/**
	 * the filter which handle the requests of gFeatMiner
	 * @return
	 */
	public static final String getGFMFilterUrl(){
		return Configuration.getGdvTomcatServ()+"/GFeatMiner";
	}
	
	/**
	 * add some requested parameters to the GFM request
	 * @param parameters - the parameters
	 * @param jobId - the job identifier
	 */
	public static void addGFMRequestedParameters(Map<String, String> parameters, int jobId) {
		try {
			parameters.put("callback_url", URLEncoder.encode(getGFMFilterUrl(),"UTF-8"));
			parameters.put("job_id", URLEncoder.encode(Integer.toString(jobId),"UTF-8"));
			parameters.put("output_location", URLEncoder.encode(Configuration.getgFeatMinerDirectory()+"/"+jobId,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Application.error(e);
		}

	}
}

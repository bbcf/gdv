package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.bbcfutils.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;




public class GFeatMinerFilter implements Filter{

	private static final String GFEATMINER_VERSION="1.0.0";
	private static final String FROM_BROWSER_INTERFACE="1";
	private static final String FROM_GFM="2";

	private static Logger log = Logs.initLogger("gFeatMiner.log",GFeatMinerFilter.class);

	@Override
	public void destroy() {

	}
	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filter) throws IOException, ServletException {
		String post = "GFeatMinerFilter :  ";
		Map<String, String[]> map = request.getParameterMap();
		for (Entry<String, String[]> entry : map.entrySet()){
			post+="- "+entry.getKey();
			for(String str : entry.getValue()){
				post+=" : "+str+" ";
			}
		}
		log.debug(post);
		Application.debug(post);
		doGet(request,response);

	}

	private void doGet(ServletRequest request, ServletResponse response) {

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(out!=null){
			Map<String, String[]> map = request.getParameterMap();
			Params params = new Params(map);

			if(params.getFrom()!=null){
				if(params.getFrom().equalsIgnoreCase(FROM_BROWSER_INTERFACE)){
					Map<String,String> gfmMap = getMapFromParams(map);
					//TODO update database with new job
					int jobId = 1;
					//get the path of the files
					changeFilePath(gfmMap);
					addGFMRequestedParameters(gfmMap,jobId);
					sendMapToGFMserver(gfmMap);



				} else if(params.getFrom().equalsIgnoreCase(FROM_GFM)){
					//TODO handle request and update database





				} else {
					out.write("wrong param : from :"+params.getFrom());
					throw new AbortWithHttpStatusException(400, true);
				}




			} else {
				out.write("missing param : 'from'");
				throw new AbortWithHttpStatusException(400, true);
			}
		}
		out.close();
	}

	/**
	 * get the path of the files selected in the browser view
	 * @param gfmMap
	 */
	private void changeFilePath(Map<String, String> gfmMap) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Send the request to GFeatMiner
	 * @param gfmMap
	 */
	private void sendMapToGFMserver(Map<String, String> gfmMap) {
		String body = "";
		for(Map.Entry<String, String> entry : gfmMap.entrySet()){
			body+=entry.getKey()+"="+entry.getValue()+"&";
		}
		body=body.substring(0, body.length()-1);
		log.debug("sendMapToGFMserver :");
		log.debug(body);
		Application.debug("sendMapToGFMserver :");
		Application.debug(body);
		try {
			InternetConnection.sendPOSTConnection(Configuration.getGdvTomcatServ()+"/GFMserver", body, InternetConnection.MIME_TYPE_FORM_APPLICATION);
		} catch (IOException e) {
			log.error(e);
		}
	}
	/**
	 * Convenient method to transform a String[] to a String
	 * @param map
	 * @return
	 */
	private Map<String, String> getMapFromParams(Map<String, String[]> map) {
		Map<String, String> newMap = new HashMap<String, String>();
		for(Map.Entry<String, String[]> entry : map.entrySet()){
			String[]values = entry.getValue();
			String value="";
			for(String str:values){
				value+=str+",";
			}
			value = value.substring(0, value.length()-1);
			newMap.put(entry.getKey(),value);
		}
		return newMap;
	}

	/**
	 * add some parameters to send to GFM server
	 * @param gfmMap
	 * @param i 
	 */
	private static void addGFMRequestedParameters(Map<String, String> gfmMap, int jobId) {
		gfmMap.put("version", GFEATMINER_VERSION);
		gfmMap.put("callback_url", Configuration.getGdv_appli_proxy()+"/GFeatMiner");
		gfmMap.put("job_id", Integer.toString(jobId));

	}


	private class Params {
		private String from;

		public Params(Map<String, String[]> map) {
			if(map!=null){
				try{
					this.from = map.get("from")[0];
				} catch (NullPointerException e){};
			}
		}

		public String toString(){
			return this.getClass().getName()+"\n" +
			"from : "+this.from+"\n";
		}


		public String getFrom(){
			return this.from;
		}
	}

}

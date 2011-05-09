package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.bbcfutils.access.InternetConnection;
import ch.epfl.bbcf.gdv.access.gfeatminer.GFeatMinerAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.control.model.GFeatMinerControl;




public class GFeatMinerFilter implements Filter{

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
				//SEND TO GFM server
				if(params.getFrom().equalsIgnoreCase(FROM_BROWSER_INTERFACE)){
					Map<String,String> gfmMap = getMapFromParams(map);
					int jobId = GFeatMinerControl.createNewJob(
							Integer.parseInt(params.getProjectId()));
					JSONObject form = changeFilePath(params);
					if(form!=null){
						gfmMap.put("form",form.toString());
						GFeatMinerAccess.addGFMRequestedParameters(gfmMap,jobId);
						GFeatMinerAccess.sendToGFeatMiner(gfmMap);
					}



					//HANDLE RESPONSE FROM GFM server
				} else if(params.getFrom().equalsIgnoreCase(FROM_GFM)){
					int jobId = Integer.parseInt(params.getJobId());
					JSONObject result;
					try {
						params.getResult();
						result = new JSONObject(params.getResult());
						JSONArray files = result.getJSONArray("files");
						Application.debug("jobId "+jobId+" files : "+files.toString());
						GFeatMinerControl.handleResultFromGFeatMiner(jobId,result);
					} catch (JSONException e) {
						Application.error(e);
					}


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
	 * & change the 'tracks parameters of the json'
	 * 
	 * @param params
	 * @return 
	 */
	private JSONObject changeFilePath(Params params) {
		if(params.getForm()!=null){
			try {
				JSONObject json = new JSONObject(params.getForm());
				JSONObject tracks = json.getJSONObject("tracks");
				if(tracks!=null){
					JSONObject newtracks = new JSONObject();
					Iterator<String> it = tracks.keys();
					while(it.hasNext()){
						String key = it.next();
						JSONObject track = tracks.getJSONObject(key);
						String name = track.getString("name");
						String path = track.getString("path");
						if(name!=null && path !=null){
							path=Configuration.getFilesDir()+"/"+path;
							track.put("path", path);
							newtracks.put(key, track);
						}
					}
					json.put("tracks", newtracks);
					return json;
				}
			} catch (JSONException e) {
				Application.error(e);
			}
		}
		return null;

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




	private class Params {
		private String projectId;
		private String from;
		private String form;

		private String jobId;
		private String result;


		public Params(Map<String, String[]> map) {
			if(map!=null){
				try{
					this.projectId = map.get("project_id")[0];
				} catch (NullPointerException e){};
				try{
					this.from = map.get("from")[0];
				} catch (NullPointerException e){};
				try{
					this.form = map.get("form")[0];
				} catch (NullPointerException e){};
				try{
					this.jobId = map.get("job_id")[0];
				} catch (NullPointerException e){};
				try{
					this.result = map.get("result")[0];
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

		public void setForm(String form) {
			this.form = form;
		}

		public String getForm() {
			return form;
		}

		public void setJobId(String jobId) {
			this.jobId = jobId;
		}

		public String getJobId() {
			return jobId;
		}

		public void setResult(String result) {
			this.result = result;
		}

		public String getResult() {
			return result;
		}

		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}

		public String getProjectId() {
			return projectId;
		}
	}

}

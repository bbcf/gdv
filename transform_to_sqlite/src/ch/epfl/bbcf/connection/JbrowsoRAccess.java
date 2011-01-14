package ch.epfl.bbcf.connection;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.daemon.Launcher;


public class JbrowsoRAccess {

	public static final Logger logger = Launcher.initLogger(JbrowsoRAccess.class.getName());
	private static final String JBROWSOR_URL = "http://ptbbpc1.epfl.ch/jbrowsor/jbrowse";
	private JSONArray refSeqsJson;


	public static boolean testConnection(){
		URL url;
		URLConnection urlConnection = null;
		try {
			url = new URL("http://ptbbpc1.epfl.ch/jbrowsor/jbrowse/data/67/data/refSeqs.js");
			urlConnection = url.openConnection();
		} catch (IOException e){
			logger.error(e);
		}
		return urlConnection!=null;
	}
	
//	public JbrowsoRAccess(String assemblyId){
//		String adress = JBROWSOR_URL+"/data/"+assemblyId+"/data/refSeqs.js";
//		String refSeqs = InternetConnection.sendGETConnection(adress);
//		String[] tmp = refSeqs.split("=");
//		String json = tmp[1];
//		try {
//			this.refSeqsJson = new JSONArray(json);
//		} catch (JSONException e) {
//			logger.error(e);
//		}
//	}
//
//	public int getChromosomeLength(String chr){
//		for(int i=0;i<refSeqsJson.length();i++){
//			try {
//				JSONObject chromosome = (JSONObject) refSeqsJson.get(i);
//				if(chromosome.getString("name").equalsIgnoreCase(chr)){
//					return chromosome.getInt("length");
//				}
//			} catch (JSONException e) {
//				logger.error(e);
//			}
//		}
//		return -1;
//	}
//	
	
	
	
//	public List<String> getChromosomeList(String assemblyId){
//		
//	}
}

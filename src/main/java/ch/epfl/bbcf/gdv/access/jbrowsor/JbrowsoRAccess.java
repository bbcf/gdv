package ch.epfl.bbcf.gdv.access.jbrowsor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


import ch.epfl.bbcf.gdv.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

public abstract class JbrowsoRAccess {
	
	public final static String SERV = "http://ptbbpc1.epfl.ch/jbrowsor/";
	public final static String SERV_JS = "/";
	
	public final static String NEW_GENOME = SERV+"genomes";
	public final static String GENOME_STATUS = SERV+"genomes/";//don't forget to add '[genomeId].json' at the end
	public final static String ANNOTATION_STATUS = SERV+"tracks/";
	public final static String NEW_TRACK = SERV+"tracks";
	public final static String GET_VIEW = SERV+"jbrowse_views.json";
	public final static String JBROWSE = SERV+"jbrowse";
	public final static String JAVASCRIPT_VIEW = SERV+"jbrowse_views/";//don't forget to add '[viewId].js
	//public final static String JBROWSE_DATA = "../../tracks_dev/";
	
	//private static final String CALLBACK_URL = Configuration.APPLI_URL+"/tracks_status/";
//	public static final String[] JBrowseJavascript = {
//		//SERV_JS+"js/gdv_link.js",
//		SERV_JS+"js/Browser.js",
//		SERV_JS+"js/Util.js",
//		SERV_JS+"js/NCList.js",
//		SERV_JS+"js/LazyPatricia.js",
//		SERV_JS+"js/Track.js",
//		SERV_JS+"js/SequenceTrack.js",
//		SERV_JS+"js/FeatureTrack.js",
//		SERV_JS+"js/StaticTrack.js",
//		SERV_JS+"js/ImageTrack.js",
//		SERV_JS+"js/prototype_reduced.js",
//		SERV_JS+"js/rectmarquee.js",
//		SERV_JS+"js/Selection.js",
//		SERV_JS+"js/SelectionHandler.js",
//		SERV_JS+"js/GenomeView.js",
//		SERV_JS+"jslib/dojo/dojo.js",
//		SERV_JS+"jslib/dojo/jbrowse_dojo.js"
//	};

	public static final String[] JBrowseCSS = {
		SERV_JS+"jslib/dijit/themes/tundra/tundra.css",
		SERV_JS+"jslib/dojo/resources/dojo.css",
		SERV_JS+"genome.css",
		SERV_JS+"js/marker.css",
		SERV_JS+"js/selection.css"
	};
	
	
	
	
	
	

	//->GENOMES
	public static int createGenome(String version,int taxId,String speciesName,String chrList,String url) {
		int genomeId = -1;
		try {
			String body =
				"genome[name]=" + URLEncoder.encode(version, "UTF-8") +
				"&genome[tax_id]=" + taxId +
				"&genome[species]=" + URLEncoder.encode(speciesName, "UTF-8") +
				"&genome[url]=" + URLEncoder.encode(url, "UTF-8") +
				"&genome[chr_list]=" + URLEncoder.encode(chrList, "UTF-8") +
				"&genome[hidden]=false";
			String result = InternetConnection.sendPOSTConnection(NEW_GENOME, body);
			genomeId = processGenomeResult(result);
		} catch (UnsupportedEncodingException e) {
			Application.error(e);
		}
		return genomeId;
	}

	/**
	 * return the id of the genome created by JBrowsoR
	 */
	private static int processGenomeResult(String result) {
		String tmp[] = result.split("\\s");
		int id = -1;
		if(!tmp[tmp.length-1].equalsIgnoreCase(".")){
			id = Integer.valueOf(tmp[tmp.length-1].substring(0,tmp[tmp.length-1].length()-1));
		}
		Application.debug("genome created with id :"+id);
		return id;
	}
	
	public static boolean checkGenomeCreation(int jbId) {
		String result = InternetConnection.sendGETConnection(GENOME_STATUS+jbId+".json");
		try {
			JSONObject json = new JSONObject(result);
			int status = json.getInt("status_id");
			if(status == 1 | status == 2){
				return false;
			}
			else if(status == 3){
				return true;
			}
			else {
				return false;
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return false;
	}
	
	public static boolean checkAnnotationCreation(int trackId) {
		String result = InternetConnection.sendGETConnection(ANNOTATION_STATUS+trackId+".json");
		try {
			JSONObject json = new JSONObject(result);
			int status = json.getInt("status_id");
			if(status == 1 | status == 2){
				return false;
			}
			else if(status == 3){
				return true;
			}
			else {
				return false;
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return false;
	}
	
	public static int getTrackStatus(int jbTrackId) {
		String result = InternetConnection.sendGETConnection(ANNOTATION_STATUS+jbTrackId+".json");
		try {
			JSONObject json = new JSONObject(result);
			int status = json.getInt("status_id");
			return status;
		} catch (JSONException e) {
			Application.error(e);
		}
		return -1;
	}
	
	//->ANNOTATIONS
	public static int createAnnotation(String annotationName,
			int assemblyId, int dataTypeId, int fileTypeId, String url, String params) {
		if((null!=annotationName) && (-1!=assemblyId) &&
				(0!=dataTypeId) &&(0!=fileTypeId) &&(null!=url)){
			if(null==params){params="";}
			try {
//				String body = 
//					"track[name]=" + URLEncoder.encode(annotationName, "UTF-8") +
//					"&track[genome_id]=" + assemblyId +
//					"&track[data_type_id]=" + dataTypeId +
//					"&track[file_type_id]=" + fileTypeId +
//					"&track[url]=" + URLEncoder.encode(url, "UTF-8") +
//					"&track[jbrowse_params]=" + URLEncoder.encode(params, "UTF-8") +
//					"&url_callback[track_id]=" + URLEncoder.encode(JbrowsoRAccess.CALLBACK_URL, "UTF-8");
				String result =  InternetConnection.sendPOSTConnection(NEW_TRACK,"body");
				return processAnnotationResult(result);
			}catch(Exception e) {
				Application.error(e);
			}
		}
		return -1;
	}
	private static int processAnnotationResult(String result) {
		Application.debug("annotation result :\n "+result);
		String tmp[] = result.split("\\s");
		int id = -1;
		if(!tmp[tmp.length-1].equalsIgnoreCase(".")){
			id = Integer.valueOf(tmp[tmp.length-1].substring(0,tmp[tmp.length-1].length()-1));
		}
		return id;
	}

	//->VIEW
	public static JSONObject getViewIdByAnnotationJBId(List<Integer> idList) {
		JSONObject json = null;
		String ids ="";
		for(int i : idList){
			ids+=i+",";
		}
		ids=ids.substring(0, ids.length()-1);
		try {
			String body = 
				"jbrowse_view[track_list]="+URLEncoder.encode(ids, "UTF-8");
			String result = InternetConnection.sendPOSTConnection(GET_VIEW,body);
			json = new JSONObject(result);
		}catch(Exception e) {
			Application.error(e);
		}
		return json;
	}
	public static String getJavascriptByViewId(String id){
		String adress = JAVASCRIPT_VIEW+id+".js";
		return InternetConnection.sendGETConnection(adress);
	}

	public static String getRefseq(int sequenceId) {
		String adress = JBROWSE+"/data/"+sequenceId+"/data/refSeqs.js";
		return InternetConnection.sendGETConnection(adress);
	}

	

	

	
}


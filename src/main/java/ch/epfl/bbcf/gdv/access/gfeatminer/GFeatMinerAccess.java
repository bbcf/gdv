package ch.epfl.bbcf.gdv.access.gfeatminer;

import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.gdv.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Logs;

public class GFeatMinerAccess {

	private static final Logger logger = Logs.initGFeatMinerLogger();
	
	private static final String url = "http://sugar.epfl.ch/gMiner";

	public static String sendReauest(Map<String, String> params) {
		String body = "[gMiner]\n";
		for(Map.Entry<String, String> entry : params.entrySet()){
			body+=entry.getKey()+"="+entry.getValue()+"\n";
		}
		return InternetConnection.sendPOSTConnection(url, body);
	}
}

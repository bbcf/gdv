package ch.epfl.bbcf.gdv.access;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import mx4j.log.Logger;

import ch.epfl.bbcf.gdv.config.Application;

public class InternetConnection {

	public static boolean testConnection(String adress){
		//Application.debug("testing connection to "+adress);
		URL url;
		URLConnection urlConnection = null;
		try {
			url = new URL(adress);
			urlConnection = url.openConnection();
		} catch (IOException e){
			Application.error(e);
		}
		return urlConnection!=null;

	}
	public static String sendGETConnection(final String adress){
		if(testConnection(adress)){
			String result ="";
			//Application.debug("GET connection to : "+adress);
			try {
				URL url;
				URLConnection urlConnection = null;
				DataInputStream inStream;
				url = new URL(adress);
				try {
					urlConnection = url.openConnection();
					((HttpURLConnection)urlConnection).setRequestMethod("GET");
				} catch(IOException e) {
					Application.error(e);
				}
				String buffer;
				inStream = new DataInputStream(urlConnection.getInputStream());
				InputStreamReader isr = new InputStreamReader(inStream);
				BufferedReader br = new BufferedReader(isr);
				while(null!=(buffer = br.readLine())){
					result+=buffer+"\n";
				}
				inStream.close();
				if(null==result||result.equalsIgnoreCase("")){
					Application.error("The connection to send no resut "+adress);
				}
			}
			catch(Exception e) {
				Application.error(e);
			}
			//pplication.debug("Result of connection : "+result);
			return result;
		}
		return null;
	}

	public static String sendPOSTConnection(final String adress,final String body){
		String result ="";
		if(testConnection(adress)){
			//Application.debug("POST connection to :"+adress+"\nwith body :\n"+body+"\n");
			try {
				URL url;
				URLConnection urlConnection = null;
				DataOutputStream outStream;
				DataInputStream inStream;
				url = new URL(adress);
				try {
					urlConnection = url.openConnection();
				} catch(IOException e) {
					Application.error(e);
				}
				urlConnection.setDoInput(true);
				urlConnection.setDoOutput(true);
				urlConnection.setUseCaches(false);
				urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				urlConnection.setRequestProperty("Content-Length", ""+ body.length());
				outStream = new DataOutputStream(urlConnection.getOutputStream());
				outStream.writeBytes(body);
				outStream.flush();
				outStream.close();
				String buffer;
				inStream = new DataInputStream(urlConnection.getInputStream());
				InputStreamReader isr = new InputStreamReader(inStream);
				BufferedReader br = new BufferedReader(isr);
				while(null!=(buffer = br.readLine())){
					result+=buffer;
				}
				inStream.close();
				outStream.close();
				if(null==result||result.equalsIgnoreCase("")){
					Application.error("The connection to send no resut "+adress);
				}
				else{
					//Application.debug("result of the connection : "+result);
				}
				return result;
			}
			
			catch(Exception e) {
				Application.debug(body);
				for(StackTraceElement el : e.getStackTrace()){
					Application.debug(el.getClassName()+"."+el.getMethodName()+"."+el.getLineNumber());
				}
				Application.error(result);
			}
		}
		return null;
	}
}
package ch.epfl.bbcf.access.connection;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class InternetConnection {

	public static boolean testConnection(String adress){
		URL url;
		URLConnection urlConnection = null;
		try {
			url = new URL(adress);
			urlConnection = url.openConnection();
		} catch (IOException e){
			e.printStackTrace();
		}
		return urlConnection!=null;

	}
	public static String sendGETConnection(final String adress) throws IOException{
		if(testConnection(adress)){
			String result ="";
			URL url;
			URLConnection urlConnection = null;
			DataInputStream inStream;
			url = new URL(adress);
			urlConnection = url.openConnection();
			((HttpURLConnection)urlConnection).setRequestMethod("GET");
			String buffer;
			inStream = new DataInputStream(urlConnection.getInputStream());
			InputStreamReader isr = new InputStreamReader(inStream);
			BufferedReader br = new BufferedReader(isr);
			while(null!=(buffer = br.readLine())){
				result+=buffer+"\n";
			}
			inStream.close();
			if(null==result||result.equalsIgnoreCase("")){
				System.err.println("The connection to send no resut "+adress);
			}
			return result;
		}
		return null;
	}

	public static String sendPOSTConnection(final String adress,final String body) throws IOException{
		String result ="";
		if(testConnection(adress)){
			URL url;
			URLConnection urlConnection = null;
			DataOutputStream outStream;
			DataInputStream inStream;
			url = new URL(adress);
			urlConnection = url.openConnection();
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
				System.err.println("The connection to send no resut "+adress);
			}
			else{
			}
			return result;
		}
		return null;
	}
}
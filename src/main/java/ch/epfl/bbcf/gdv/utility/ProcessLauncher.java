package ch.epfl.bbcf.gdv.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

public class ProcessLauncher {

	private String command;
	private List<String> args;
	private String result;

	private ProcessLauncher(String command){
		this.command = command;
		this.args = new ArrayList<String>();
	}

	private void addArg(String arg){
		this.args.add(arg);
	}

	private boolean launch(String directory){
		String[] commandLine = new String[args.size()+1];
		commandLine[0]=command;
		for(int i=1;i<=args.size();i++){
			commandLine[i]=args.get(i-1);
		}
		Process process = null;
		ProcessBuilder pb = new ProcessBuilder(commandLine);

		pb.directory(new File(directory));
		String str = "";
		for(String s : commandLine){
			str+=s+" ";
		}
	//	Application.debug("launching process : "+str+" on directory : "+directory);
		try {
			process = pb.start();
		} catch (IOException e) {
			Application.error(e);
		}
		InputStream is = null;
		try {
			switch(process.waitFor()){
			case 0: is = process.getInputStream();break;
			default: is = process.getErrorStream();break;
			}
		} catch (InterruptedException e) {
			Application.error(e);
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		try {
			setResult(br.readLine());
		} catch (IOException e1) {
			Application.error(e1);
		}
		switch(process.exitValue()){
		case 0: return true;
		default: return false;
		}

	}


	public static boolean launchSVGProcess(String directory, String md5,
			String trackDirectory) {
		String[] commandLine = new String[5];
		commandLine[0]="java";
		commandLine[1]="-jar";
		commandLine[2]="sqlite2svg.jar";
		commandLine[3]=md5;
		commandLine[4]=trackDirectory;
		Process process = null;
		ProcessBuilder pb = new ProcessBuilder(commandLine);

		pb.directory(new File(directory));
		String str = "";
		for(String s : commandLine){
			str+=s+" ";
		}
		Application.debug("launching process : "+str+" on directory : "+directory);
		
		try {
			process = pb.start();
		} catch (IOException e) {
			Application.error(e);
		}
		InputStream is = null;
		try {
			switch(process.waitFor()){
			case 0: is = process.getInputStream();break;
			default: is = process.getErrorStream();break;
			}
		} catch (InterruptedException e) {
			Application.error(e);
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		try {
			String line= null;
			while((line = br.readLine())!=null)
			Application.debug(line);
		} catch (IOException e1) {
			Application.error(e1);
		}
		switch(process.exitValue()){
		case 0: return true;
		default: return false;
		}

		
		
	}

		public static String getFileMD5(File file) throws ProcessLauncherError{
			ProcessLauncher pl = new ProcessLauncher("md5sum");
			pl.addArg(file.getName());
			String fileDirectory = file.getAbsolutePath();
			String directory = fileDirectory.substring(0,fileDirectory.length()-file.getName().length());
			if(pl.launch(directory)){
				return pl.getResult().split("\\s")[0].trim();
			}
			else{
				throw new ProcessLauncherError(pl.getResult());
			}
		}



		/**
		 * @param result the result to set
		 */
		public void setResult(String result) {
			this.result = result;
		}

		/**
		 * @return the result
		 */
		public String getResult() {
			return result;
		}

		public static void launchPerlProcess(File tmpFile, JSONObject params) {
			// TODO Auto-generated method stub
			
		}



	}

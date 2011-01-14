package ch.epfl.bbcf.access.gdv;

import java.io.Console;
import java.io.IOException;
import java.util.Map;

import ch.epfl.bbcf.access.connection.InternetConnection;

/**
 * 
 * @author Yohan Jarosz
 *
 */
public class PostToGDV {

	public static final String GDV_ADRESS = "http://svitsrv25.epfl.ch/gdv_dev/post";
	private static final String GDV_POST = "gdv_post";

	/**
	 * Main entry of the program
	 * @param args
	 */
	public static void main(String[] args) {
		Console c = System.console();
		if (c == null) {
			System.err.println("No console.");
			System.exit(1);
		}
		String mail = c.readLine("Enter your mail: ");
		System.out.println("Enter your gdv key\n" +
		"(on preferences page on the web interface)");
		String pass = new String(c.readPassword("key : "));
		String body = "id="+GDV_POST+"&command=request_login&url="+mail+"&obfuscated="+pass;
		try {
			String ok = InternetConnection.sendPOSTConnection(GDV_ADRESS, body);
			if(ok.equalsIgnoreCase("true")){
				boolean b = parseArgs(args,mail,pass);
				if(!b){
					usage();
				}
			} else {
				System.err.println(ok);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


	}




	/**
	 * parse the arguments provided by the user
	 * @param args - the args
	 * @param mail - the login
	 * @param pass - the password
	 * @return
	 */
	private static boolean parseArgs(String[] args, String mail, String pass) {
		if(args.length>1){
			String id = GDV_POST;
			String command = args[0];
			if(RequestParameters.hasCommand(command)){
				RequestParameters params = buildRequestParameters(args);
				if(null!=params){
					Command control = Command.getControl(command,mail,pass);
					if(null==control){
						return false;
					} else {
						return control.doRequest(id,params);
					}
				}
			}
		}
		return false;
	}

	/**
	 * build an RequestParamters object with all args provided 
	 * by user
	 * @param args - the arguments
	 * @return RequestParamters
	 */
	private static RequestParameters buildRequestParameters(String[] args) {
		RequestParameters params = new RequestParameters();
		for(int i=2;i<args.length;i++){
			if(i+1<args.length){
				if(args[i].startsWith("--")){
					String cmd = args[i].substring(2, args[i].length());
					if(cmd.equalsIgnoreCase(RequestParameters.PROJECT_ID_PARAM)){
						params.setProjectId(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.URL_PARAM)){
						params.setUrl(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.DATATYPE_PARAM)){
						params.setDatatype(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.TYPE_PARAM)){
						params.setType(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.OBFUSCATED_PARAM)){
						params.setObfuscated(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.SEQUENCE_ID_PARAM)){
						params.setSequenceId(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.NAME_PARAM)){
						params.setName(args[i+1]);
					} else {
						System.err.println("the parameter "+args[i]+" is not a parameter.");
					}
				} else {
					System.err.println(args[i]+" is not a parameter. Perhaps you didn't add --.");
				}
			} else {
				System.err.println("the parameters "+args[i]+" is not defined");
				return null;
			}
			i++;
		}
		return params;
	}


	private static boolean newTrack(String id, String command,RequestParameters params) {
		if(checkParams(params.getType(),params.getUrl(),params.getProjectId(),params.getObfuscated())){
			String body = "id="+id+"&command="+command+"&"+RequestParameters.TYPE_PARAM+"="+params.getType()
			+"&"+RequestParameters.URL_PARAM+"="+params.getUrl()
			+"&"+RequestParameters.PROJECT_ID_PARAM+"="+params.getProjectId()
			+"&"+RequestParameters.OBFUSCATED_PARAM+"="+params.getObfuscated();
			try {
				System.out.println(InternetConnection.sendPOSTConnection(GDV_ADRESS, body));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("missing param(s) for add_track");
		}
		return false;
	}

	private static boolean newSqlite(String id, String command,RequestParameters params) {
		if(checkParams(params.getUrl(),params.getProjectId(),params.getDatatype())){
			String body = "id="+id+"&command="+command+"&"+RequestParameters.URL_PARAM+"="+params.getUrl()
			+"&"+RequestParameters.PROJECT_ID_PARAM+"="+params.getProjectId()
			+"&"+RequestParameters.DATATYPE_PARAM+"="+params.getDatatype();
			try {
				System.out.println(InternetConnection.sendPOSTConnection(GDV_ADRESS, body));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			System.err.println("missing param(s) for add_track");
		}
		return false;
	}

	private static boolean newProject(String id, String command, RequestParameters params) {
		if(checkParams(params.getType(),params.getSequenceId(),params.getName(),params.getObfuscated())){
			String body = "id="+id+"&command="+command+"&"+RequestParameters.TYPE_PARAM+"="+params.getType()
			+"&"+RequestParameters.SEQUENCE_ID_PARAM+"="+params.getSequenceId()
			+"&"+RequestParameters.NAME_PARAM+"="+params.getName()
			+"&"+RequestParameters.OBFUSCATED_PARAM+"="+params.getObfuscated();
			try {
				System.out.println(InternetConnection.sendPOSTConnection(GDV_ADRESS, body));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("missing param(s) for new_project");
		}
		return false;
	}


	/**
	 * check if a parameter required is given or not
	 * @param params
	 * @return
	 */
	private static boolean checkParams(String... params) {
		for(String p : params){
			if(null==p){
				return false;
			}
		}
		return true;
	}

	/**
	 * convenient method to write a table to the output
	 * @param values
	 * @return
	 */
	private static String writeList(String[] values) {
		String str = "";
		for(String v :values){
			str+=v+", ";
		}
		str=str.substring(0, str.length()-2);
		return str;
	}

	/**
	 * print help to the console output
	 */
	private static void usage() {
		String str = "\n-----------" +
		"\n-----------" +
		"\n---USAGE---\n--SEND PROCESSES GDV\n" +
		"first arg : must be the gdv_password\n" +
		"second arg : must be the command ("+
		writeList(RequestParameters.commands)+
		")\n" +
		"the other args are given like;\n" +
		"\t --<arg> <value> (without <>) " +
		"like e.g: --name Bob";
		System.out.println(str);
		System.out.println();
		System.out.println("--REQUESTED PARAMETERS : ");
		for(Map.Entry<String,String[]> entry : RequestParameters.getMapcommands().entrySet()){
			System.out.println("for command "+entry.getKey()+" : ");
			System.out.println("\t "+writeList(entry.getValue()));
		}
		System.out.println("-----------\n-----------");

	}
}

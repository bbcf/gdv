package ch.epfl.bbcf.access.gdv;

import java.io.Console;
import java.io.IOException;
import java.util.Map;


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
		if(!parseArgs(args)){
			usage();
		}
	}




	/**
	 * parse the arguments provided by the user
	 * @param args - the args
	 * @param mail - the login
	 * @param pass - the password
	 * @return
	 */
	private static boolean parseArgs(String[] args) {
		String id = GDV_POST;
		RequestParameters params = buildRequestParameters(args);
		System.out.println("parsing args .... ");
		if(null!=params){
			if(checkParams(params.getMail(),params.getKey(),params.getCommand())){
				Command control = Command.getControl(params.getCommand(), params.getMail(), params.getKey());
				if(null!=control){
					return control.doRequest(id,params);
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
		for(int i=0;i<args.length;i++){
			if(i+1<args.length){
				if(args[i].startsWith("--")){
					String cmd = args[i].substring(2, args[i].length());
					if(cmd.equalsIgnoreCase(RequestParameters.PROJECT_ID_PARAM)){
						params.setProjectId(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.URL_PARAM)){
						params.setUrl(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.COMMAND_PARAM)){
						params.setCommand(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.KEY_PARAM)){
						params.setKey(args[i+1]);
					} else if(cmd.equalsIgnoreCase(RequestParameters.MAIL_PARAM)){
						params.setMail(args[i+1]);
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


	/**
	 * check if a parameter required is given or not
	 * @param params
	 * @return
	 */
	public static boolean checkParams(String... params) {
		for(String p : params){
			if(null==p){
				System.err.println("missing param(s)");
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
		"\n" +
		"the args are given like;\n" +
		"\t --<arg> <value> (without <>) " +
		"e.g: --name Bob";
		System.out.println(str);
		System.out.println();
		System.out.println("--REQUESTED PARAMETERS : ");
		for(Map.Entry<String,String[]> entry : RequestParameters.getMapcommands().entrySet()){
			System.out.println(entry.getKey()+" : ");
			System.out.println("\t "+writeList(entry.getValue()));
		}
		System.out.println("-----------\n-----------");

	}
}

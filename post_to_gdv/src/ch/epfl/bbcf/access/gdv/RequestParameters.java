package ch.epfl.bbcf.access.gdv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RequestParameters {

	public static final String NEW_PROJECT_COMMAND ="new_project";
	public static final String ADD_TRACK_COMMAND ="add_track";
	public static final String ADD_SQLITE_COMMAND ="add_sqlite";

	public static final String[] commands = 
	{NEW_PROJECT_COMMAND,ADD_TRACK_COMMAND,ADD_SQLITE_COMMAND};

	public static final String 
	PROJECT_ID_PARAM = "project_id",
	DATATYPE_PARAM="datatype",
	TYPE_PARAM="type",
	OBFUSCATED_PARAM="obfuscated",
	SEQUENCE_ID_PARAM="seq_id",
	NAME_PARAM="name",
	URL_PARAM="url";
	
	private static final Map<String,String[]> mapCommands = buildMapCommands();

	private String url,projectId,
	datatype,type,obfuscated,sequenceId,name;
	
	private String mail,pass;



	public static boolean hasCommand(String cmd){
		if(cmd.equalsIgnoreCase("h")|| cmd.equalsIgnoreCase("help")||
				cmd.equalsIgnoreCase("usage")){
			return false;
		}
		for(String str : commands){
			if(str.equalsIgnoreCase(cmd)){
				return true;
			}
			
		}
		return false;
	}

	/**
	 * method to tell the user
	 * what a command need for arguments
	 * in order to work
	 * @return Map
	 */
	private static Map<String,String[]> buildMapCommands() {
		Map<String,String[]> map = new HashMap<String,String[]>();
		String newProject[] = {TYPE_PARAM,SEQUENCE_ID_PARAM,NAME_PARAM,OBFUSCATED_PARAM};
		String addTrack[] = {TYPE_PARAM,URL_PARAM,PROJECT_ID_PARAM,OBFUSCATED_PARAM};
		String addSqlite[] = {URL_PARAM,PROJECT_ID_PARAM,DATATYPE_PARAM};
		map.put(NEW_PROJECT_COMMAND,newProject);
		map.put(ADD_TRACK_COMMAND,addTrack);
		map.put(ADD_SQLITE_COMMAND,addSqlite);
		return map;
	}



	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}



	public String getProjectId() {
		return projectId;
	}



	public void setSequenceId(String sequenceId) {
		this.sequenceId = sequenceId;
	}



	public String getSequenceId() {
		return sequenceId;
	}



	public void setType(String type) {
		this.type = type;
	}



	public String getType() {
		return type;
	}



	public void setObfuscated(String obfuscated) {
		this.obfuscated = obfuscated;
	}



	public String getObfuscated() {
		return obfuscated;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getName() {
		return name;
	}



	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}



	public String getDatatype() {
		return datatype;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public String getUrl() {
		return url;
	}

	public static Map<String,String[]> getMapcommands() {
		return mapCommands;
	}



	public void setMail(String mail) {
		this.mail = mail;
	}



	public String getMail() {
		return mail;
	}



	public void setPass(String pass) {
		this.pass = pass;
	}



	public String getPass() {
		return pass;
	}


	

}

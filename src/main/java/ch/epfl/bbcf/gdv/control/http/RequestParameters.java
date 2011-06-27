package ch.epfl.bbcf.gdv.control.http;

import java.util.Map;

import ch.epfl.bbcf.gdv.control.http.command.Command;
import ch.epfl.bbcf.gdv.control.http.command.Command.ID;
import ch.epfl.bbcf.gdv.control.http.command.PostAccess;
import ch.epfl.bbcf.gdv.control.http.command.PostAccess.COMMAND;

public class RequestParameters {
	
	
	private Command.ACTION action;
	//private Command.TYPE type;
	private Command.DB_TYPE dbType;
	private Command.ID id;
	private Command.STATUS status;
	
	private String datatype; //qualitative or quantitative
	private String trackId; 
	private String message; 
	private String db;
	private String usermail;//mail to send a reply when at the end of processing files
	private COMMAND command;//command launched by user (new project,add track,...)
	private String name;
	private String seqId;
	private String url;
	private int projectId;
	private int nrAssemblyId;
	private String file;
	private String obfuscated;
	private String mail;
	private String key;
	private String isPublic;
	private int jobId;
	private String data;
	private String selections;
	
	
	public RequestParameters(Map<String, String[]> map) {
		if(map!=null){
			try{
				this.id = Command.ID.valueOf(map.get("id")[0]);
			} catch (NullPointerException e){};
			try{
				this.status = Command.STATUS.valueOf(map.get("status")[0]);
			} catch (NullPointerException e){};
			try{
				this.dbType = Command.DB_TYPE.valueOf(map.get("db_type")[0]);
			} catch (NullPointerException e){};
			try{
				this.action = Command.ACTION.valueOf(map.get("action")[0]);
			} catch (NullPointerException e){};
			try{
				this.jobId = Integer.parseInt(map.get("job_id")[0]);
			} catch (NullPointerException e){};
			try{
				this.key = map.get("key")[0];
			} catch (NullPointerException e){};
			try{
				this.data = map.get("data")[0];
			} catch (NullPointerException e){};
//			try{
//				this.type  = Command.TYPE.valueOf(map.get("type")[0]);
//			} catch (NullPointerException e){};
			try{
				this.trackId = map.get("track_id")[0];
			} catch (NullPointerException e){};
			try{
				this.message = map.get("mess")[0];
			} catch (NullPointerException e){};
			try{
				this.db = map.get("db")[0];
			} catch (NullPointerException e){};
			try{
				this.usermail = map.get("usermail")[0];
			} catch (NullPointerException e){};
			try{
				this.command = PostAccess.COMMAND.valueOf(map.get("command")[0]);
			} catch (NullPointerException e){};
			try{
				this.name = map.get("name")[0];
			} catch (NullPointerException e){};
			try{
				this.seqId = map.get("seq_id")[0];
			} catch (NullPointerException e){};
			try{
				this.url = map.get("url")[0];
			} catch (NullPointerException e){};
			try{
				this.projectId = Integer.parseInt(map.get("project_id")[0]);
			} catch (NullPointerException e){};
			try{
				this.nrAssemblyId = Integer.parseInt(map.get("nrass")[0]);
			} catch (NullPointerException e){};
			try{
				this.obfuscated = map.get("obfuscated")[0];
			} catch (NullPointerException e){};
			try{
				this.datatype = map.get("datatype")[0];
			} catch (NullPointerException e){};
			try{
				this.file = map.get("file")[0];
			} catch (NullPointerException e){};
			try{
				this.mail = map.get("mail")[0];
			} catch (NullPointerException e){};
			try{
				this.isPublic = map.get("public")[0];
			} catch (NullPointerException e){};
			try{
				this.selections = map.get("selections")[0];
			} catch (NullPointerException e){};
		}
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(ID id) {
		this.id = id;
	}
	/**
	 * Reserved for the FrontController
	 * @return the id
	 */
	public ID getId() {
		return id;
	}
//	/**
//	 * @param type the type to set
//	 */
//	public void setType(TYPE type) {
//		this.type = type;
//	}
//	/**
//	 * @return the type
//	 */
//	public TYPE getType() {
//		return type;
//	}
	/**
	 * @param trackId the trackId to set
	 */
	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}
	/**
	 * @return the trackId
	 */
	public String getTrackId() {
		return trackId;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}


	/**
	 * @param db the db to set
	 */
	public void setDb(String db) {
		this.db = db;
	}


	/**
	 * @return the db
	 */
	public String getDb() {
		return db;
	}


	/**
	 * @param usermail the usermail to set
	 */
	public void setUsermail(String usermail) {
		this.usermail = usermail;
	}


	/**
	 * @return the usermail
	 */
	public String getUsermail() {
		return usermail;
	}


	/**
	 * @param htc3cseq_number the command to set
	 */
	public void setCommand(COMMAND command) {
		this.command = command;
	}


	/**
	 * @return the command
	 */
	public COMMAND getCommand() {
		return command;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param seq_id the species to set
	 */
	public void setSequenceId(String species) {
		this.seqId = species;
	}


	/**
	 * @return the species
	 */
	public String getSequenceId() {
		return seqId;
	}


	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}


	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}


	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}


	/**
	 * @return the projectId
	 */
	public int getProjectId() {
		return projectId;
	}


	/**
	 * @param nrAssemblyId the nrAssemblyId to set
	 */
	public void setNrAssemblyId(int nrAssemblyId) {
		this.nrAssemblyId = nrAssemblyId;
	}


	/**
	 * @return the nrAssemblyId
	 */
	public int getNrAssemblyId() {
		return nrAssemblyId;
	}


	/**
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}


	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}


	public void setObfuscated(String obfuscated) {
		this.obfuscated = obfuscated;
	}


	public String getObfuscated() {
		return obfuscated;
	}


	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}


	public String getDatatype() {
		return datatype;
	}


	public void setMail(String mail) {
		this.mail = mail;
	}


	public String getMail() {
		return mail;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public String getKey() {
		return key;
	}


	public void setPublic(String isPublic) {
		this.isPublic = isPublic;
	}


	public String isPublic() {
		return isPublic;
	}


	public void setJobId(int jobId) {
		this.jobId = jobId;
	}


	public int getJobId() {
		return jobId;
	}


	public void setAction(Command.ACTION action) {
		this.action = action;
	}


	public Command.ACTION getAction() {
		return action;
	}


	public void setDbType(Command.DB_TYPE dbType) {
		this.dbType = dbType;
	}


	public Command.DB_TYPE getDbType() {
		return dbType;
	}


	public void setData(String data) {
		this.data = data;
	}


	public String getData() {
		return data;
	}


	public void setStatus(Command.STATUS status) {
		this.status = status;
	}


	public Command.STATUS getStatus() {
		return status;
	}


	public void setSelections(String selections) {
		this.selections = selections;
	}


	public String getSelections() {
		return selections;
	}

}

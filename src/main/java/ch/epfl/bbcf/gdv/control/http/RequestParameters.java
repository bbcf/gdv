package ch.epfl.bbcf.gdv.control.http;

import java.util.Map;

public class RequestParameters {

	private String id;//type of request
	private String type;
	private String datatype;
	private String trackId;
	private String message;
	private String db;
	private String usermail;
	private String command;
	private String name;
	private String seqId;
	private String url;
	private String projectId;
	private String nrAssemblyId;
	private String file;
	private String obfuscated;
	private String mail;
	private String pass;
	
	
	public RequestParameters(Map<String, String[]> map) {
		if(map!=null){
			try{
				this.id = map.get("id")[0];
			} catch (NullPointerException e){};
			try{
				this.type  = map.get("type")[0];
			} catch (NullPointerException e){};
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
				this.command = map.get("command")[0];
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
				this.projectId = map.get("project_id")[0];
			} catch (NullPointerException e){};
			try{
				this.nrAssemblyId = map.get("nrass")[0];
			} catch (NullPointerException e){};
			try{
				this.obfuscated = map.get("obfuscated")[0];
			} catch (NullPointerException e){};
			try{
				this.obfuscated = map.get("datatype")[0];
			} catch (NullPointerException e){};
			try{
				this.file = map.get("file")[0];
			} catch (NullPointerException e){};
		}
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
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
	public void setCommand(String command) {
		this.command = command;
	}


	/**
	 * @return the command
	 */
	public String getCommand() {
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
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}


	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}


	/**
	 * @param nrAssemblyId the nrAssemblyId to set
	 */
	public void setNrAssemblyId(String nrAssemblyId) {
		this.nrAssemblyId = nrAssemblyId;
	}


	/**
	 * @return the nrAssemblyId
	 */
	public String getNrAssemblyId() {
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


	public void setPass(String pass) {
		this.pass = pass;
	}


	public String getPass() {
		return pass;
	}

}

package ch.epfl.bbcf.access.gdv;

import java.io.IOException;

import ch.epfl.bbcf.access.connection.InternetConnection;


public abstract class Command {


	private String command;
	private String mail;
	private String pass;

	protected Command(String command,String mail,String pass){
		this.command = command;
		this.mail = mail;
		this.pass = pass;
	}
	/**
	 * prepare the parameters to send to GDV
	 * @param id
	 * @param params
	 */
	protected abstract boolean doRequest(String id, RequestParameters params);

	/**
	 * send a post to GDV application
	 * @param body
	 * @return
	 */
	protected boolean send(String body2){
		String body = "mail="+mail+"&pass="+pass+"&command="+command+"&"+body2;
		try {
			System.out.println(InternetConnection.sendPOSTConnection(PostToGDV.GDV_ADRESS, body));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}




	public static class Project extends Command{
		protected Project(String command,String mail,String pass){
			super(command, mail, pass);
		}
		@Override
		protected boolean doRequest(String id, RequestParameters params) {
			if(checkParams(params.getType(),params.getSequenceId(),params.getName(),params.getObfuscated())){
				String body = "id="+id+"&"+RequestParameters.TYPE_PARAM+"="+params.getType()
				+"&"+RequestParameters.SEQUENCE_ID_PARAM+"="+params.getSequenceId()
				+"&"+RequestParameters.NAME_PARAM+"="+params.getName()
				+"&"+RequestParameters.OBFUSCATED_PARAM+"="+params.getObfuscated();	
				return send(body);
			}
			return false;

		}


	}


	public static class SQLite extends Command{
		protected SQLite(String command,String mail,String pass){
			super(command, mail, pass);
		}

		@Override
		protected boolean doRequest(String id, RequestParameters params) {
			if(checkParams(params.getUrl(),params.getProjectId(),params.getDatatype())){
				String body = "id="+id+"&"+RequestParameters.URL_PARAM+"="+params.getUrl()
				+"&"+RequestParameters.PROJECT_ID_PARAM+"="+params.getProjectId()
				+"&"+RequestParameters.DATATYPE_PARAM+"="+params.getDatatype();
				return send(body);
			}
			return false;

		}

	}






	public static class Track extends Command{
		protected Track(String command,String mail,String pass){
			super(command, mail, pass);
		}

		@Override
		protected boolean doRequest(String id, RequestParameters params) {
			if(checkParams(params.getType(),params.getSequenceId(),params.getName(),params.getObfuscated())){
				String body = "id="+id+"&"+RequestParameters.TYPE_PARAM+"="+params.getType()
				+"&"+RequestParameters.SEQUENCE_ID_PARAM+"="+params.getSequenceId()
				+"&"+RequestParameters.NAME_PARAM+"="+params.getName()
				+"&"+RequestParameters.OBFUSCATED_PARAM+"="+params.getObfuscated();
				return send(body);
			}
			return false;
		}
	}




	/**
	 * return the right method for the command given
	 * @param command - the command 
	 * @param pass - the password
	 * @param mail - the mail
	 * @return
	 */
	public static Command getControl(String command, String mail, String pass) {
		if(command.equalsIgnoreCase(RequestParameters.NEW_PROJECT_COMMAND)){
			return new Project(command,mail,pass);
		} else if(command.equalsIgnoreCase(RequestParameters.ADD_SQLITE_COMMAND)){
			return new SQLite(command,mail,pass);
		} else if(command.equalsIgnoreCase(RequestParameters.ADD_TRACK_COMMAND)){
			return new Track(command,mail,pass);
		} else {
			return null;
		}
	}


	/**
	 * check if a parameter required is given or not
	 * @param params
	 * @return
	 */
	protected static boolean checkParams(String... params) {
		for(String p : params){
			if(null==p){
				System.err.println("missing param(s)");
				return false;
			}
		}
		return true;
	}
}


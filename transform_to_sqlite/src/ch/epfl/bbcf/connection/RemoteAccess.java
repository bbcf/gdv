package ch.epfl.bbcf.connection;

import ch.epfl.bbcf.conf.Configuration;

public class RemoteAccess {

	
	
	//ERRORS
	public static void sendTrackErrorMessage(String message,String type,String trackId,String filePath){
		InternetConnection.sendPOSTConnection(
				Configuration.getFeedbackUrl(),"id=track_error&track_id="+trackId+"&file="+filePath+"&type="+type+"&mess="+message);
	}

	public static void sendChromosomeErrorMessage(String message,
			String type,String trackId, String nrAssemblyId) {
		InternetConnection.sendPOSTConnection(
				Configuration.getFeedbackUrl(),"id=track_error&track_id="+trackId+"&type="+type+"&nrass="+nrAssemblyId+"&mess="+message);
		
	}
	
	//SUCCEED
	public static void sendTrackSucceed(String trackId, String database,
			String usermail, String type) {
		InternetConnection.sendPOSTConnection(
				Configuration.getFeedbackUrl(), "id=track_parsing_success&track_id="+trackId+"&db="+database+"&usermail="+usermail+"&type="+type);
		
	}
}

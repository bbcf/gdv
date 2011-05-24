package ch.epfl.bbcf.gdv.control.model;

import java.sql.SQLException;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteConstruct;
import ch.epfl.bbcf.gdv.config.UserSession;

public class SelectionControl extends Control{

	public SelectionControl(UserSession session) {
		super(session);
	}

	public static void createNewSelection(String selections, String projectId) throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		JSONArray array = new JSONArray(selections);

		if(array.length()>0){

			//TODO create a new db
			//TODO get unique filename
			//TODO get connection to default track of the project
			String db="";
			String defaultTrack="";
			
			
			
//			SQLiteConstruct constructor = SQLiteConstruct.getConnectionWithDatabase(db);
//			constructor.createNewDatabase("qualitative");
//			for(int i=0;i<array.length();i++){
//				JSONObject chrJSON = new JSONObject(array.get(i));
//				String chr=chrJSON.getString("chr");
//				constructor.newChromosome_qual(chr);
//				constructor.writeValues_qual(chr,chrJSON.getInt("start"), chrJSON.getInt("end"),0f,"", 0, "");
//			}
//
//			SQLiteAccess access = SQLiteAccess.getConnectionWithDatabase(defaultTrack);
//			Map<String,Integer> map = access.getChromosomesAndLength();
//			access.close();
//			constructor.finalizeDatabase(map, false, true, true);
//			constructor.close();
			
			
			//TODO launch a new Track processing
			
			
			
		}
	}

}

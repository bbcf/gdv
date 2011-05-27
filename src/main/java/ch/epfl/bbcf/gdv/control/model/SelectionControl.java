package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.bbcfutils.Utility;
import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteConstruct;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;

public class SelectionControl extends Control{

	public SelectionControl(UserSession session) {
		super(session);
	}

	public static boolean createNewSelection(String selections, int projectId,int nr_assembly_id) throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		JSONArray array = new JSONArray(selections);
		if(array.length()>0){
			//get tmp database
			String randomPath= Configuration.getTmp_dir()+"/"+UUID.randomUUID().toString();
			String databasePath =randomPath+"/selection.db"; 
			new File(randomPath).mkdir();
			System.out.println("db path = "+databasePath);
			//get default track
			Track t = TrackControl.getAdminTrackByNrAssemblyID(nr_assembly_id);
			String input = TrackControl.getFileFromTrackId(t.getId());
			String defaultTrackPath = Configuration.getFilesDir()+"/"+input;
			
			
			
			SQLiteConstruct constructor = SQLiteConstruct.getConnectionWithDatabase(databasePath);
			SQLiteAccess access = SQLiteAccess.getConnectionWithDatabase(defaultTrackPath);
			constructor.createNewDatabase("qualitative");
			Map<String,Integer> map = new HashMap<String, Integer>();
			for(int i=0;i<array.length();i++){
				JSONObject chrJSON = array.getJSONObject(i);
				String chr=chrJSON.getString("chr");
				int length = access.getLengthForChromosome(chr);
				map.put(chr, length);
				constructor.newChromosome_qual(chr);
				constructor.writeValues_qual(chr,chrJSON.getInt("start"), chrJSON.getInt("end"),0f,"", 0, "");
			}
			access.close();
			constructor.finalizeDatabase(map, false, true, true);
			constructor.close();
		
			Users user = UserControl.getUserByProjectId(projectId);
			return InputControl.processUserInput(user.getId(),projectId,null,null,databasePath);
			//TODO launch a new Track processing
			
			
			
		}
		return false;
	}

}

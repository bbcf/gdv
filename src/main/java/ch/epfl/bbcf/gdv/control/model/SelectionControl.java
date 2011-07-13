package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.bbcfutils.access.genrep.GenrepWrapper;
import ch.epfl.bbcf.bbcfutils.access.genrep.MethodNotFoundException;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Assembly;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Chromosome;
import ch.epfl.bbcf.bbcfutils.parsing.SQLiteExtension;
import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteConstruct;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

public class SelectionControl extends Control{

	
	public static boolean createNewSelection(int jobId,String selections, int projectId,int nr_assembly_id,String selectionName) throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		JSONArray array = new JSONArray(selections);
		if(array.length()>0){
			/* get tmp database */
			String randomPath= Configuration.getTmp_dir()+"/"+UUID.randomUUID().toString();
			String trackName = selectionName;
			String databasePath =randomPath+"/"+trackName; 
			new File(randomPath).mkdir();
			System.out.println("db path = "+databasePath);

			/* get constructor */
			SQLiteConstruct constructor = SQLiteConstruct.getConnectionWithDatabase(databasePath);
			constructor.createNewDatabase(SQLiteExtension.QUALITATIVE);



			/* get chromosomes */
			Assembly assembly;
			try {
				assembly = GenrepWrapper.getAssemblyFromNrAssemblyId(nr_assembly_id);
				List<Chromosome> chromosomes = assembly.getChromosomes();
				Map<String,Integer> map = new HashMap<String, Integer>();
				for(int i=0;i<array.length();i++){
					JSONObject chrJSON = array.getJSONObject(i);
					String chr=chrJSON.getString("chr");
					int length = getChromosomeLength(chr,chromosomes);
					map.put(chr, length);
					constructor.newChromosome_qual(chr);
					constructor.writeValues_qual(chr,chrJSON.getInt("start"), chrJSON.getInt("end"),0f,"", 0, "");
				}
				constructor.finalizeDatabase(map, false, true, true);
				constructor.close();

				Users user = UserControl.getUserByProjectId(projectId);
				return InputControl.processUserInput(jobId,user.getId(),projectId,null,null,databasePath,trackName);
			} catch (MethodNotFoundException e) {
				Application.error(e);
			}
		}
		return false;
	}
	
	
	
	
	
	
	private static int getChromosomeLength(String chromosome,List<Chromosome> chromosomes){
		for(Chromosome chr : chromosomes){
			if(chr.getChr_name().equalsIgnoreCase(chromosome)){
				return chr.getLength();
			}
		}
		return -1;
	}

}

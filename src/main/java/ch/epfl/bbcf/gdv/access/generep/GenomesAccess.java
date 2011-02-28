package ch.epfl.bbcf.gdv.access.generep;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;

public class GenomesAccess extends GeneRepAccess{

	public final static String GENOME_KEY ="genome";
	public final static String ID_KEY = "id";
	public final static String ORGANISM_ID_KEY="organism_id";

	protected static String getUrl() {
		return GeneRepAccess.URL+"genomes.json";
	}
	protected static String getShowUrl(){
		return GeneRepAccess.URL+"genomes/";
	}

	/**
	 * get the genomes belonging to a species
	 * @param id
	 * @return
	 */
	public static List<JSONObject> getGenomesBySpeciesId(int id){
		String result = InternetConnection.sendGETConnection(getUrl());
		JSONArray json = null;
		List<JSONObject> genomes = new ArrayList<JSONObject>();
		try {
			json = new JSONArray(result);
			for(int i=0;i<json.length();i++){
				JSONObject genome = (JSONObject) ((JSONObject) json.get(i)).get(GENOME_KEY);
				if(genome.get(ORGANISM_ID_KEY).toString().equalsIgnoreCase(Integer.toString(id))){
					genomes.add(genome);
				}
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return genomes;
	}


	public static JSONObject getGenomesById(String genomeId) {
		JSONObject json = null;
		String result = InternetConnection.sendGETConnection(getShowUrl()+genomeId+".json");
		try {
			json = new JSONObject(result).getJSONObject(GenomesAccess.GENOME_KEY);
		} catch (JSONException e) {
			Application.error(e);
		}
		return json;
	}
}

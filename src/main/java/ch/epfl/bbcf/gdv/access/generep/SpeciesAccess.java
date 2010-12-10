package ch.epfl.bbcf.gdv.access.generep;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

public class SpeciesAccess extends GeneRepAccess{

	public static final String ORGANISM_KEY = "organism";
	public static final String ID_KEY = "id";
	public static final String NAME_KEY = "species";
	public static final String TAXID_KEY = "tax_id";

	protected static String getUrl() {
		return GeneRepAccess.URL+"organisms.json";
	}
	protected static String getShowUrl() {
		return GeneRepAccess.URL+"organisms/";
	}


	/**
	 * Access to Generep
	 * @return the list of organism presents in Generep
	 */
	public static List<JSONObject> getOrganisms(){
		String result = InternetConnection.sendGETConnection(getUrl());
		JSONArray json = null;
		List<JSONObject> orgs = new ArrayList<JSONObject>();
		try {
			json = new JSONArray(result);
			for(int i=0;i<json.length();i++){
				JSONObject organism = (JSONObject)json.get(i);
				orgs.add((JSONObject) organism.get(ORGANISM_KEY));
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return orgs;

	}


	public static JSONObject getOrganismByAssemblyJSON(JSONObject assembly) {
		JSONObject organism = null;
		if(assembly!=null){
			try {
				JSONObject genome = GenomesAccess.getGenomesById(assembly.getString(AssembliesAccess.GENOME_ID_KEY));
				String organismId = genome.getString(GenomesAccess.ORGANISM_ID_KEY);
				String result = InternetConnection.sendGETConnection(getShowUrl()+organismId+".json");
				organism = new JSONObject(result);
			} catch (JSONException e) {
				Application.error(e);
			}
		}

		return organism;
	}


	/**
	 * Get the organism present in Genrep
	 * @return a list of selectOption of the Organism
	 */
	public static ch.epfl.bbcf.gdv.html.utility.SelectOption[] getOrganismsSelectOpt() {
		List<JSONObject> organisms = getOrganisms();
		SelectOption [] options = new SelectOption[organisms.size()];
		for(int i=0;i<organisms.size();i++){
			JSONObject org = organisms.get(i);
			try {
				options[i] = new SelectOption(org.getString(ID_KEY),org.getString(NAME_KEY));
			} catch (JSONException e) {
				Application.error(e);
			}
		}
		return options;
	}

	public static SelectOption getOrganismsSelectOptionById(String sequenceId) {
		SelectOption[] spOtions = getOrganismsSelectOpt();
		for(SelectOption so : spOtions){
			if(so.getKey().equalsIgnoreCase(sequenceId)){
				return so;
			}
		}
		return null;
	}
	/**
	 * return the species if you provide the good
	 * sequence id
	 * @param sequenceId
	 * @return
	 */
	public static JSONObject getOrganismById(String sequenceId) {
		String result = InternetConnection.sendGETConnection(getUrl());
		JSONArray json = null;
		JSONObject organism = null;
		try {
			json = new JSONArray(result);
			for(int i=0;i<json.length();i++){
				organism =  (JSONObject) ((JSONObject) json.get(i)).get(ORGANISM_KEY);
				if(organism.getString(ID_KEY).equalsIgnoreCase(sequenceId)){
					return organism;
				}
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return organism;
	}








}


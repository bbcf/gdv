package ch.epfl.bbcf.gdv.access.generep;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

public class AssembliesAccess extends GeneRepAccess{


	public static final String NAME_KEY = "name";
	public static final String ID_KEY = "id";
	public static final String MD5_KEY = "md5";
	public static final String GENOME_ID_KEY ="genome_id";
	public static final String NR_ASSEMBLY_KEY ="nr_assembly";
	public static final String ASSEMBLY_KEY ="assembly";
	public static final String CHROMOSOME_KEY = "chromosomes";
	public static final String SPECIES_KEY = "source_id";

	protected static String getUrl() {
		return GeneRepAccess.URL+"nr_assemblies.json";
	}
		protected static String getShowUrl() {
			return GeneRepAccess.URL+"assemblies/";
		}

	public static List<JSONObject> getAssembliesBySpeciesId(String speciesId) {
		List<JSONObject> genomes = GenomesAccess.getGenomesBySpeciesId(speciesId);
		String result = InternetConnection.sendGETConnection(getUrl());
		JSONArray json = null;
		List<JSONObject> assemblies = new ArrayList<JSONObject>();
		try {
			json = new JSONArray(result);
			for(int i=0;i<json.length();i++){
				JSONObject assembly = (JSONObject) ((JSONObject) json.get(i)).get(NR_ASSEMBLY_KEY);
				for(JSONObject genome : genomes){
					if(assembly.getString(GENOME_ID_KEY).equalsIgnoreCase(genome.getString(GenomesAccess.ID_KEY))){
						assemblies.add(assembly);
					}
				}
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return assemblies;
	}

	/**
	 * Return the assemblies on Genrep for the species selected
	 * @param speciesId - the species selected
	 * @return - an array of SelectOption
	 */
	public static SelectOption[] getAssembliesBySpeciesIdSelectOpt(String speciesId) {
		List<JSONObject> assemblies = getAssembliesBySpeciesId(speciesId);

		SelectOption [] options = new SelectOption[assemblies.size()];
		for(int i=0;i<assemblies.size();i++){
			JSONObject ass = assemblies.get(i);
			try {
				options[i] = new SelectOption(ass.getString(ID_KEY),ass.getString(NAME_KEY));//et oui, je fait un getString sur un ass ;)
			} catch (JSONException e) {
				Application.error(e);
			}
		}
		return options;
	}

	public static JSONObject getAssemblyById(String assemblyId) {
		try {
			//String result = InternetConnection.sendGETConnection(getShowUrl()+assemblyId+".json");
			String result = InternetConnection.sendGETConnection(getUrl());
			JSONArray assemblies = null;

			assemblies = new JSONArray(result);
			for(int i=0;i<assemblies.length();i++){
				JSONObject ass = (JSONObject) assemblies.get(i);
				JSONObject assembly = ass.getJSONObject(NR_ASSEMBLY_KEY);
				if(assembly.getString(ID_KEY).equalsIgnoreCase(assemblyId)){
					return assembly;
				}
			}
//			JSONObject assembly = null;
//			assembly =  (JSONObject) new JSONObject(result).get(ASSEMBLY_KEY);
//			return assembly;
		} catch (JSONException e) {
			Application.error(e);
		}
		return null;
	}

	public static List<JSONObject> getChromosomesByAssemblyId(String nrassemblyId) {
		String assemblyId = AssembliesAccess.getAssembliesByNRAssemblyId(nrassemblyId);
		
		String result = InternetConnection.sendGETConnection(getShowUrl()+assemblyId+".json");
		List<JSONObject> chrs = new ArrayList<JSONObject>();
		JSONObject assemblies = null;
		try {
			assemblies =  (JSONObject) new JSONObject(result);
			JSONObject assembly = assemblies.getJSONObject(ASSEMBLY_KEY);
			JSONArray chromosomes = assembly.getJSONArray(CHROMOSOME_KEY);
			for(int i=0;i<chromosomes.length();i++){
				JSONObject chromosome = (JSONObject) chromosomes.get(i);
				chrs.add(chromosome);
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return chrs;
	}
	private static String getAssembliesByNRAssemblyId(String nrassemblyId) {
		String result = InternetConnection.sendGETConnection("http://ptbbpc1.epfl.ch/genrep/assemblies.json");
		try {
			JSONArray assemblies = new JSONArray(result);
			for(int i=0;i<assemblies.length();i++){
				JSONObject ass = (JSONObject) assemblies.get(i);
				JSONObject assembly = ass.getJSONObject("assembly");
				if(assembly.getString("nr_assembly_id").equalsIgnoreCase(nrassemblyId)){
					return assembly.getString("id");
				}
			}
		} catch (JSONException e) {
			Application.debug(e);
		}
		return null;
	}
	public static String getMD5ByAssemblyId(String assemblyId) {
		String result = InternetConnection.sendGETConnection(getUrl());
		try {
			JSONArray assemblies = new JSONArray(result);
			for(int i=0;i<assemblies.length();i++){
				JSONObject ass = (JSONObject) assemblies.get(i);
				JSONObject assembly = ass.getJSONObject(NR_ASSEMBLY_KEY);
				if(assembly.getString(ID_KEY).equalsIgnoreCase(assemblyId)){
					return assembly.getString(MD5_KEY);
				}
			}
		} catch (JSONException e) {
			Application.debug(e);
		}
		return null;
	}


}

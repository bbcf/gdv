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
	public static final String BBCF_VALID ="bbcf_valid";
	public static final String GTF_FILE_KEY ="gtf_file_ftp";

	protected static String getNrUrl() {
		return GeneRepAccess.URL+"nr_assemblies.json";
	}
	protected static String getUrl() {
		return GeneRepAccess.URL+"assemblies.json";
	}
	protected static String getShowUrl() {
		return GeneRepAccess.URL+"assemblies/";
	}
	protected static String getShowNrUrl() {
		return GeneRepAccess.URL+"nr_assemblies/";
	}


	/**
	 * get the assemblies belonging to a species
	 * @param speciesId
	 * @return
	 */
	public static List<JSONObject> getAssembliesBySpeciesId(int speciesId) {
		List<JSONObject> genomes = GenomesAccess.getGenomesBySpeciesId(speciesId);
		String result = InternetConnection.sendGETConnection(getUrl());
		JSONArray json = null;
		List<JSONObject> assemblies = new ArrayList<JSONObject>();
		try {
			json = new JSONArray(result);
			for(int i=0;i<json.length();i++){
				JSONObject assembly = (JSONObject) ((JSONObject) json.get(i)).get(ASSEMBLY_KEY);
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
	public static SelectOption[] getAssembliesBySpeciesIdSelectOpt(int speciesId) {
		List<JSONObject> assemblies = getAssembliesBySpeciesId(speciesId);

		SelectOption [] options = new SelectOption[assemblies.size()];
		for(int i=0;i<assemblies.size();i++){
			JSONObject ass = assemblies.get(i);
			try {
				options[i] = new SelectOption(ass.getInt(ID_KEY),ass.getString(NAME_KEY));//et oui, je fait un getString sur un ass ;)
			} catch (JSONException e) {
				Application.error(e);
			}
		}
		return options;
	}

	/**
	 * get the assemblies from genrep
	 * @param assemblyId
	 * @return
	 */
	public static JSONObject getAssemblyById(int assemblyId) {
		try {
			String result = InternetConnection.sendGETConnection(getNrUrl());
			JSONArray assemblies = null;

			assemblies = new JSONArray(result);
			for(int i=0;i<assemblies.length();i++){
				JSONObject ass = (JSONObject) assemblies.get(i);
				JSONObject assembly = ass.getJSONObject(ASSEMBLY_KEY);
				if(assembly.getInt(ID_KEY)==assemblyId){
					return assembly;
				}
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return null;
	}
	
	/**
	 * return the chromosomes belonging to this assembly
	 * @param assemblyId
	 * @return
	 */
	public static List<JSONObject> getChromosomesByAssemblyId(int assemblyId) {
		//String assemblyId = AssembliesAccess.getAssembliesByNRAssemblyId(nrassemblyId);
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
	
	

	public static List<JSONObject> getNRAssembliesBySpeciesId(int speciesId) {
		List<JSONObject> genomes = GenomesAccess.getGenomesBySpeciesId(speciesId);
		String result = InternetConnection.sendGETConnection(getNrUrl());
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
	 * Return the nr assemblies on Genrep for the species selected
	 * @param speciesId - the species selected
	 * @return - an array of SelectOption
	 */
	public static SelectOption[] getNRAssembliesBySpeciesIdSelectOpt(int speciesId) {
		List<JSONObject> assemblies = getNRAssembliesBySpeciesId(speciesId);

		SelectOption [] options = new SelectOption[assemblies.size()];
		for(int i=0;i<assemblies.size();i++){
			JSONObject ass = assemblies.get(i);
			try {
				options[i] = new SelectOption(ass.getInt(ID_KEY),ass.getString(NAME_KEY));//et oui, je fait un getString sur un ass ;)
			} catch (JSONException e) {
				Application.error(e);
			}
		}
		return options;
	}


	/**
	 * get the nr_assembly object from Genrep
	 * @param nrAssemblyId
	 * @return
	 */
	public static JSONObject getNRAssemblyById(int nrAssemblyId) {
		try {
			String result = InternetConnection.sendGETConnection(getNrUrl());
			JSONArray assemblies = null;

			assemblies = new JSONArray(result);
			for(int i=0;i<assemblies.length();i++){
				JSONObject ass = (JSONObject) assemblies.get(i);
				JSONObject assembly = ass.getJSONObject(NR_ASSEMBLY_KEY);
				if(assembly.getInt(ID_KEY)==nrAssemblyId){
					return assembly;
				}
			}
		} catch (JSONException e) {
			Application.error(e);
		}
		return null;
	}
	
	public static String getAssemBlyGTF(int assemblyId){
		JSONObject ass = getNRAssemblyById(assemblyId);
		if(null!=ass){
			try {
				String url = ass.getString(GTF_FILE_KEY);
				if(url!=null && !url.equalsIgnoreCase("null")){
					return getShowNrUrl()+assemblyId+".gtf";
				}
			} catch (JSONException e) {
				Application.error("getAssemBlyGTF : "+e);
			}
		}
		return null;

	}
	public static List<JSONObject> getChromosomesByNRAssemblyId(int nrassemblyId) {
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

	private static String getAssembliesByNRAssemblyId(int nrassemblyId) {
		String result = InternetConnection.sendGETConnection(getUrl());
		try {
			JSONArray assemblies = new JSONArray(result);
			for(int i=0;i<assemblies.length();i++){
				JSONObject ass = (JSONObject) assemblies.get(i);
				JSONObject assembly = ass.getJSONObject("assembly");
				if(assembly.getString("nr_assembly_id").equalsIgnoreCase(Integer.toString(nrassemblyId))){
					return assembly.getString("id");
				}
			}
		} catch (JSONException e) {
			Application.debug(e);
		}
		return null;
	}
	public static String getMD5ByAssemblyId(int assemblyId) {
		String result = InternetConnection.sendGETConnection(getNrUrl());
		try {
			JSONArray assemblies = new JSONArray(result);
			for(int i=0;i<assemblies.length();i++){
				JSONObject ass = (JSONObject) assemblies.get(i);
				JSONObject assembly = ass.getJSONObject(NR_ASSEMBLY_KEY);
				if(assembly.getString(ID_KEY).equalsIgnoreCase(Integer.toString(assemblyId))){
					return assembly.getString(MD5_KEY);
				}
			}
		} catch (JSONException e) {
			Application.debug(e);
		}
		return null;
	}
	/**
	 * check if the genome isBBCFValid in Genrep
	 * @param assembly
	 * @return
	 */
	public static boolean isBBCFValid(JSONObject assembly) {
		try {
			boolean bbcfValid = assembly.getBoolean(BBCF_VALID);
			return bbcfValid;
		} catch (JSONException e) {
			Application.error("isBBCFValid "+e);
		}
		return false;
	}



}

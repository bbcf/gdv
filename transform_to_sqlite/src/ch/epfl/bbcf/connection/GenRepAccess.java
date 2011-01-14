package ch.epfl.bbcf.connection;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.daemon.Launcher;





public class GenRepAccess {
	
	public static final String URL = "http://bbcftools.vital-it.ch/genrep";
	public static final Logger logger = Launcher.initLogger(GenRepAccess.class.getName());
	
	public final static String GENOME_KEY ="genome";
	public final static String ID_KEY = "id";
	public final static String ORGANISM_ID_KEY="organism_id";
	public static final String GENOME_ID_KEY ="genome_id";
	public static final String NR_ASSEMBLY_KEY ="nr_assembly";
	public static final String ASSEMBLY_KEY ="assembly";
	public static final String CHROMOSOME_KEY = "chromosomes";
	public static final String CHROMOSOME_LENGTH_KEY = "length";
	

	protected static String getChromosomeUrl() {
		return URL+"/genrep/chromosomes.json";
	}
	protected static String getAssembliesShowUrl() {
		return URL+"/assemblies/";
	}

	public static boolean testConnection(){
		URL url;
		URLConnection urlConnection = null;
		try {
			url = new URL(GenRepAccess.URL+"/assemblies.json");
			urlConnection = url.openConnection();
		} catch (IOException e){
			logger.error(e);
		}
		return urlConnection!=null;
	}
	public static final String CHROMOSOM_KEY = "chromosome";
	public static final String CHR_NAME ="chr_name";
	public static final String CHR_ID = "chromosome_id";
	public static final String CHR_LOCUS = "refseq_locus";
	public static final String CHR_VERSION = "refseq_version";
	public static final String CHR_LENGTH = "length";


	private List<JSONObject> chromosomes;


	public  GenRepAccess(String assemblyID){
		this.chromosomes = getChromosomesByAssemblyId(assemblyID);
	}

	public int getChrLength(String chrName){
		for(JSONObject chromosome : chromosomes){
			try {
				JSONObject chr = chromosome.getJSONObject(CHROMOSOM_KEY);
				if(chr.getString(CHR_NAME).equalsIgnoreCase(chrName)){
					return chr.getInt(CHROMOSOME_LENGTH_KEY);
				}
			} catch (JSONException e) {
				logger.error(e);
			}
		}
		return 0;
	}
	
	
	public static List<String> getChromosomesListByAssemblyId(String assemblyId){
		//logger.debug("getChromosomesListByAssemblyId "+assemblyId);
		List<JSONObject> chromosomes = getChromosomesByAssemblyId(assemblyId);
		//logger.debug("chrs "+chromosomes.toString());
		List<String> chrList = new ArrayList<String>();
		for(JSONObject chromosome : chromosomes){
			JSONObject chr;
			try {
				chr = chromosome.getJSONObject(CHROMOSOM_KEY);
				chrList.add(chr.getString(CHR_NAME));
			} catch (JSONException e) {
				logger.error(e);
			}
		}
		return  chrList;
	}
	
	
	
	private static List<JSONObject> getChromosomesByAssemblyId(String nrassemblyId) {
		//logger.debug("getChromosomesListByAssemblyId (JSON)"+nrassemblyId);
		String assemblyId = getAssembliesByNRAssemblyId(nrassemblyId);
		String result = InternetConnection.sendGETConnection(getAssembliesShowUrl()+assemblyId+".json");
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
			logger.error(e);
		}
		return chrs;
	}
	
	
	
	
	
	private static String getAssembliesByNRAssemblyId(String nrassemblyId) {
		//logger.debug("getAssembliesByNRAssemblyId "+nrassemblyId);
		String result = InternetConnection.sendGETConnection(GenRepAccess.URL+"/assemblies.json");
		//logger.debug(result);
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
			logger.debug(e);
		}
		return null;
	}
	
	public static void main(String[]args){
		//System.out.println("ccc");
		List<String> list = getChromosomesListByAssemblyId("70");
	}
	
}

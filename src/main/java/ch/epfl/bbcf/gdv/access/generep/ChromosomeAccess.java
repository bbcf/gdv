package ch.epfl.bbcf.gdv.access.generep;



import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;

public class ChromosomeAccess extends GeneRepAccess{

	public static final String CHROMOSOM_KEY = "chromosome";
	public static final String CHR_NAME ="chr_name";
	public static final String CHR_ID = "chromosome_id";
	public static final String CHR_LOCUS = "refseq_locus";
	public static final String CHR_VERSION = "refseq_version";
	public static final String CHR_LENGTH = "length";

	protected static String getUrl() {
		return GeneRepAccess.URL+"chromosomes.json";
	}


	public static List<String> getChromosomesListByAssemblyId(int assemblyId){
		List<JSONObject> chromosomes = AssembliesAccess.getChromosomesByNRAssemblyId(assemblyId);
		List<String> chrList = new ArrayList<String>();
		for(JSONObject chromosome : chromosomes){
			JSONObject chr;
			try {
				chr = chromosome.getJSONObject(CHROMOSOM_KEY);
				chrList.add(chr.getString(CHR_NAME));
			} catch (JSONException e) {
				Application.error(e);
			}
		}
		return  chrList;

	}
}

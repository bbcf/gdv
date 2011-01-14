package ch.epfl.bbcf.gdv.access.generep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.InternetConnection;
import ch.epfl.bbcf.gdv.config.Application;

public class RepositoryAccess extends GeneRepAccess{

	public final static String REPOSITORY_KEY="repo_file";
	
	public final static String URL_KEY="url";
	
	protected static String getUrl(){
		return GeneRepAccess.URL+"repo_files.json";
	}

	public static String getURLFastaFileByAssemblyId(int assemblyId) {
		String md5 = AssembliesAccess.getMD5ByAssemblyId(assemblyId);
		String result = GeneRepAccess.URL+"data/nr_assemblies/fasta/"+md5+".tar.gz";
		return result;
	}
	
}

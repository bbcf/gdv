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

	public static String getURLFastaFileByAssemblyId(String assemblyId) {
		String md5 = AssembliesAccess.getMD5ByAssemblyId(assemblyId);
		String result = "http://ptbbpc1.epfl.ch/genrep/data/nr_assemblies/fasta/"+md5+".tar.gz";
		return result;
//		JSONArray json = null;
//		JSONObject repos = null;
//		try {
//			json = new JSONArray(result);
//			for(int i=0;i<json.length();i++){
//				repos =  (JSONObject) ((JSONObject) json.get(i)).get(REPOSITORY_KEY);
//				return SERV+repos.getString(URL_KEY);
//			}
//		} catch (JSONException e) {
//			Application.error(e);
//		}
//		return null;
	}
	
}

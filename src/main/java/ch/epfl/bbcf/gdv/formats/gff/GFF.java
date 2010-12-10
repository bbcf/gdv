package ch.epfl.bbcf.gdv.formats.gff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.program.gff.GFFParser;
import org.biojava.utils.ParserException;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.generep.AssembliesAccess;
import ch.epfl.bbcf.gdv.access.generep.ChromosomeAccess;
import ch.epfl.bbcf.gdv.config.Application;


public class GFF {

//	public static CustomGFFHandler processGFFfileForAnnotation(File file) {
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(
//					new FileReader(file));
//		} catch (FileNotFoundException e) {
//			Application.error(e);
//		}
//		//parsing
//		GFFParser parser = new GFFParser();
//		CustomGFFHandler handler = new CustomGFFHandler("process");
//		handler.setFileToCopyFrom(file);
//		//handler.setSession(session);
//		try {
//			parser.parse(br, handler);
//		} catch (IOException e) {
//			Application.error(e);
//		} catch (BioException e) {
//			Application.error(e);
//		} catch (ParserException e) {
//			Application.error(e);
//		}
//		return handler;
//	}

	public static String getGFFHeader(String assemblyId){
		List<JSONObject> chromosomes = AssembliesAccess.getChromosomesByAssemblyId(assemblyId);
		String header = "";
		for(JSONObject chromosome : chromosomes){
			try {
				Application.debug(chromosome.toString());
				JSONObject c = chromosome.getJSONObject(ChromosomeAccess.CHROMOSOM_KEY);
				String name = c.getString(ChromosomeAccess.CHR_NAME);
				String length = c.getString(ChromosomeAccess.CHR_LENGTH);
				header+=name+"\tgenerep\tcontig\t1\t"+length+"\t.\t.\t.\tName="+name+"\n";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Application.debug("header from gff = \n"+header);
		return header;

	}
	//	public static void copyFileWithRightChrNames(Map<String, String> chrNames,
	//			File file, File newFile) {
	//		BufferedReader br = null;
	//		try {
	//			br = new BufferedReader(
	//					new FileReader(file));
	//		} catch (FileNotFoundException e) {
	//			Application.error(e);
	//		}
	//			//parsing
	//			GFFParser parser = new GFFParser();
	//			CustomGFFHandler handler = new CustomGFFHandler("copy");
	//			handler.setFileToCopyTo(newFile);
	//			//handler.setChrNames(chrNames);
	//			try {
	//				parser.parse(br, handler);
	//			} catch (IOException e) {
	//				Application.error(e);
	//			} catch (BioException e) {
	//				Application.error(e);
	//			} catch (ParserException e) {
	//				Application.error(e);
	//			}
	//	}

	/**
	 * get the JSON String to record in file, that JBrowse need 
	 * in order to show qualitative tracks
	 * @param altsNames optionnal (the species : to replace the name of
	 * the gene by a more common name)
	 */
	public static Map<String, String> getJSONDescriptor(File file, Map<String, String> altsNames) {
		Application.debug("getJSONDESC");
		BufferedReader br = null;
		try {
			br = new BufferedReader(
					new FileReader(file));
		} catch (FileNotFoundException e) {
			Application.error(e);
		}
		CustomGFFHandler handler = new CustomGFFHandler("JSONDescriptor");
		if(null!=altsNames){
			handler.setAltNamesMap(altsNames);
		}
		GFFParser parser = new GFFParser();
		try {
			parser.parse(br, handler);
			Map<String, String> descriptors = handler.getJSONDescriptor();
			
			Map<String, String> result = new HashMap<String, String>();
			Iterator<String> it = descriptors.keySet().iterator();
			Application.debug("end parsing");
			while(it.hasNext()){
				String key = it.next();
				String params = descriptors.get(key);
				//to have the feature count in the result
				//Application.debug(params.toString());
				int index = params.indexOf("[");
			
				int featureCount = Integer.parseInt(params.substring(0,index));
				params = params.substring(index);
				
				params=params.substring(0, params.length()-1);
				params+="]";
			//	Application.debug("XXXXXXXXXXXXXXXXXXXXXX");
				//Application.debug(params.toString());
				//int featureCount = handler.getFeatureCount();
				String finalParam = "{\"headers\":[\"start\",\"end\",\"strand\",\"id\",\"name\"]," +
				"\"subfeatureClasses\":null," +
				"\"featureCount\":"+featureCount+"," +
				"\"key\":\""+file.getName()+"\"," +
				"\"featureNCList\":[" +params+
				",\"className\":\"feature2\"," +
				"\"clientConfig\":null," +
				"\"rangeMap\":[]," +
				"\"arrowheadClass\":null," +
				"\"subfeatureHeaders\":[\"start\",\"end\",\"strand\",\"id\",\"type\"]," +
				"\"type\":\"FeatureTrack\"," +
				"\"label\":\"Alignments\"," +
				"\"sublistIndex\":5}";
				result.put(key, finalParam);
			}
			return result;
		} catch (IOException e) {
			Application.error(e);
		} catch (BioException e) {
			Application.error(e);
		} catch (ParserException e) {
			Application.error(e);
		}
		return null;
	}

	public static void main(String[]args){
		System.out.println("start");
	//	System.out.println(getJSONDescriptor(new File("/Users/jarosz/Desktop/gff.gff")));

	}
//	/**
//	 * Return a boolean if the GFF has scores.
//	 * the test is just on the first 10 lines (whithout comments #)
//	 * @param file
//	 * @return
//	 * @throws IOException
//	 */
//	public static boolean hasScore(File file) throws IOException{
//		BufferedReader br = null;
//		br = new BufferedReader(new FileReader(file));
//		String line;
//		int cpt = 0;
//		while ((line=br.readLine())!=null){
//			if(!line.startsWith("#")){
//				cpt++;
//				if(cpt>10){
//					return false;
//				}
//				String[] record = line.split("\\t");
//				if(record.length>=8){
//					try {
//						double d = Double.parseDouble(record[5]);
//						return true;
//					} catch(NumberFormatException e) {
//						return false;
//					}
//				}
//			}
//		}
//		return false;
//	}


}

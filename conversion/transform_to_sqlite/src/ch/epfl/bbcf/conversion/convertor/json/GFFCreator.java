package ch.epfl.bbcf.conversion.convertor.json;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;


public class GFFCreator extends JSONHandler{

	private List<String>types;
	private String subFeatureHeaders;
	public static final int SUBLISTINDEX = 5;
	/**
	 * handle the writing of JSON 
	 * -enter at your own risks-
	 * @param inputFileFullPath - the full path to the file parsed
	 * @param fullPathDatabase - the full path to the file in sqlite format
	 * @param name - the name of the track
	 * @param outputDirectory - the ouputDir (MUST exist)
	 * @param ressourceURL - the ressource url to put in the browser which link to the track
	 */
	public GFFCreator(String inputFileFullPath,String fullPathDatabase,String name,String outputDirectory,String ressourceURL,ClientConfig config) {
		super(inputFileFullPath,fullPathDatabase,name,config,outputDirectory,ressourceURL);
	}

	public void newChromosome(String chr) throws FileNotFoundException{
		if(null!=chrOutJSON){
			write(","+previousEnd+",{\"chunk\":\""+previousChunkSize+"\"}",chrOutJSON);
		}
		super.newChromosome(chr);
	}
	
	
	
	
	
	@Override
	protected void writeHeader(String chr, OutputStream out) {
		String str = "{\"headers\":[\"start\",\"end\",\"name\",\"strand\"";
		if(null!=types &&!types.isEmpty()){
			str+=",\"subfeatures\"]," +
			"\"subfeatureClasses\":"+getSubFeatureClasses(types)+",";
		} else {
			str+="],";
		}
		if(!subFeatureHeaders.equalsIgnoreCase("")){
			str+="\"subfeatureHeaders\":"+subFeatureHeaders+",";
		}
		str+="\"sublistIndex\":"+SUBLISTINDEX+",";

		write(str,out);

	}
	
	
	
	
	
	
	
	
	
//	public void endIt() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, FileNotFoundException, JSONConversionException{
//		if(chrOutJSON!=null){
//			write(","+previousEnd+",{\"chunk\":\""+previousChunkSize+"\"}]",chrOutJSON);
//		}
//		close(chrOutJSON);
//		theChunk.closeChunk();
//		SQLiteAccess access = SQLiteAccess.getConnectionWithDatabase(databasePath);
//		for(String chr:chrs){
//			File jsonFile = new File(outputDirectory+"/"+fileName+"/"+chr+".json");
//			OutputStream out = null;
//			out = new FileOutputStream(jsonFile,true);
//			if(out!=null){
//				int featureCount = access.getFeatureCountForChromosome(chr);
//				int chrLength = access.getLengthForChromosome(chr);
//				System.out.println(featureCount+" "+chrLength+" XX");
//				double t = (chrLength*2.5)/featureCount;
//				int threshold = 0;
//				for (int i : zooms){
//					threshold = i;
//					if(i>t){
//						break;
//					}
//				}
//				if(featureCount>0 && chrLength>0){
//					writeTrackDataAndHistoFiles(
//							name,chrLength,threshold,featureCount,inputFilePath,fileName,outputDirectory+"/"+fileName+"/",ressourceURL, chr, out,clientConfig);
//				} else {
//					jsonFile.delete();
//					throw new JSONConversionException("not processed : "+chr+"(feature count = "+featureCount+" and chr length on genrep = "+chrLength+")");
//				}
//			} else {
//				throw new JSONConversionException("outputstream for write chromosome trackData = null ("+inputFilePath+")");
//			}
//		}
//		access.close();
//
//	}
	@Override
	public void writeValues(String chr, int start, int end, String name,
			int strand, JSONArray feature, int featureCount,
			boolean finish, float score) throws FileNotFoundException,
			JSONException {
		System.out.println("Write values : "+chr+" start "+start+" stop "+end+" name "+name+" strand "+strand+" feature "+feature.toString()+" feature count "+featureCount);
		previousEnd = end;
		curChunkSize+=featureCount;
		if(featureCount==0){
			curChunkSize++;
		}
		if(null==chrOutJSON){
			chrOutJSON = newChrOutput(chr);
		}
		if(firstChrOut){
			previousChunkSize = 0;
			write("["+start,chrOutJSON);
		}
		if(!firstChunkOut && (curChunkSize%CHUNK_SIZE==0 || (curChunkSize%CHUNK_SIZE)<=featureCount)){
			theChunk.closeChunk();
			theChunk = new Chunk(curChunkSize,chr);
			//write in the chrOutput
			firstChunkOut=true;
			write(","+end+",{\"chunk\":\""+previousChunkSize+"\"}]",chrOutJSON);
			previousChunkSize = curChunkSize;
			if(!finish){
				write(",["+start,chrOutJSON);
			}
		}
		String toWrite;

		if(firstChrOut || firstChunkOut){
			toWrite = "[";
			firstChrOut = false;
			firstChunkOut = false;
		} else {
			toWrite = ",";
		}
		toWrite+=feature;
		theChunk.add(feature);
	}

	
	/**
	 * return the differents subfeatures in the GFF
	 * @param types2
	 * @return
	 */
	private String getSubFeatureClasses(List<String> types2) {
		String str = "{";
		for(String type : types){
			str+="\""+type+"\":\""+getClasseFromType(type)+"\",";
		}
		str=str.substring(0,str.length()-1);
		str+="}";
		return str;
	}

	/**
	 * return the css class for the type provided
	 * @param type
	 * @return
	 */
	private String getClasseFromType(String type) {
		if(type.equalsIgnoreCase("intron")){
			return "intron";
		} else if(type.equalsIgnoreCase("exon")){
			return "exon";
		} else if(type.equalsIgnoreCase("start")){
			return "triangle";
		}else if(type.equalsIgnoreCase("start_codon")){
			return "triangle";
		}else if(type.equalsIgnoreCase("stop")){
			return "triangle";
		}else if(type.equalsIgnoreCase("stop_codon")){
			return "triangle";
		}else if(type.equalsIgnoreCase("CDS")){
			return "cds";
		}else if(type.equalsIgnoreCase("UTR")){
			return "utr";
		}else if(type.equalsIgnoreCase("est")){
			return "est";
		}else {
			return "";
		}
	}



	/**
	 * @param types the types to set
	 */
	public void setTypes(List<String> types) {
		this.types = types;
	}

	/**
	 * @return the types
	 */
	public List<String> getTypes() {
		return types;
	}

	/**
	 * @param subFeatureHeaders the subFeatureHeaders to set
	 */
	public void setSubFeatureHeaders(String subFeatureHeaders) {
		this.subFeatureHeaders = subFeatureHeaders;
	}

	/**
	 * @return the subFeatureHeaders
	 */
	public String getSubFeatureHeaders() {
		return subFeatureHeaders;
	}




}




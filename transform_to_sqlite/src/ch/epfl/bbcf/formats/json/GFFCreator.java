package ch.epfl.bbcf.formats.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import ch.epfl.bbcf.conf.Configuration;
import ch.epfl.bbcf.formats.sqlite.SQLiteAccess;

public class GFFCreator extends JSONCreator{

	private List<String>types;
	private String subFeatureHeaders;
	public static final int SUBLISTINDEX = 5;
	public GFFCreator(String database, File file, String name) {
		super(database, file, name, ClientConfig.GFF);
	}

	public void newChromosome(String chr){
		if(null!=chrOutJSON){
			write(","+previousEnd+",{\"chunk\":\""+previousChunkSize+"\"}",chrOutJSON);
		}
		super.newChromosome(chr);
	}
	public void finalize(){
		//logger.debug("finalize json output");
		//logger.debug("write ] to chunk : "+chunk.toString());
		//write("]",chunk);
		write(","+previousEnd+",{\"chunk\":\""+previousChunkSize+"\"}]",chrOutJSON);
		close(chrOutJSON);
		//close(chunk);
		for(String chr:chrs){
			File jsonFile = new File(Configuration.getJbrowseOutput()+"/"+database+"/"+chr+".json");
			OutputStream out = null;
			try {
				out = new FileOutputStream(jsonFile,true);
			} catch (FileNotFoundException e) {
				logger.error(e);
			}
			if(out!=null){
				int featureCount = SQLiteAccess.getFeatureCountForChromosome(database, chr);
				
				int chrLength = SQLiteAccess.getLengthForChromosome(database,chr);
				double t = (chrLength*2.5)/featureCount;
				int threshold = 0;
				for (int i : zooms){
					threshold = i;
					if(i>t){
						break;
					}

				}
				if(chr.equalsIgnoreCase("IImicron")){
					chr = "2micron";
				}
				if(featureCount>0 && chrLength>0){
					writeTrackDataAndHistoFiles(name,chrLength,threshold,featureCount,database,chr,out,clientConfig);
				} else {
					jsonFile.delete();
					logger.warn("not processed : "+chr+"(feature count = "+featureCount+" and chr length on genrep = "+chrLength+")");
				}



			} else {
				logger.error("outputstream for write chromosome trackData = null ("+file.getAbsolutePath()+")");
			}
		}
	}
	@Override
	public void writeValues(String chr, int start, int end, String name,
			int strand, JSONArray feature, int featureCount, boolean finish) {
		logger.debug("write values :"+chr+"("+start+","+end+") "+name+" count =  "+featureCount+"\n");
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
			//logger.debug("writing "+chr+" ["+start);
		}
		if(!firstChunkOut && (curChunkSize%CHUNK_SIZE==0 || (curChunkSize%CHUNK_SIZE)<=featureCount)){
			theChunk.closeChunk();
			theChunk = new Chunk(curChunkSize,chr);
//			write("]",chunk);
//			close(chunk);
			//write in the chrOutput
			firstChunkOut=true;
			//chunk = newChunk(chr,curChunkSize);
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
		//logger.debug("adding "+toWrite+" to chunk : "+chunk.toString());
		theChunk.add(feature);
		//write(toWrite,chunk);
	}

	@Override
	protected void writeHeader(String database2, String chr, OutputStream out) {
		String str = "{\"headers\":[\"start\",\"end\",\"name\",\"strand\"";
		if(!types.isEmpty()){
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

	@Override
	protected void writeValues(String chr, int start, int end, float score,
			String name, int strand, int id) {
		logger.error("should not be used");

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




package ch.epfl.bbcf.conversion.convertor.json;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONException;


public class BAMCreator extends JSONHandler{

	public BAMCreator(String inputFileFullPath,String fullPathDatabase,String name,String outputName,String outputDirectory,String ressourceURL,ClientConfig config) {
		super(inputFileFullPath, fullPathDatabase, name,outputName, config, outputDirectory,
				ressourceURL);
	}

	@Override
	protected void writeHeader(String chr, OutputStream out) {
		String str = "{\"headers\":[\"start\",\"end\"],";
		write(str,out);		
	}

	
	@Override
	public void writeValues(String chr, int start, int end, String name,
			int strand, JSONArray feature, int featureCount,
			boolean finish, float score) throws FileNotFoundException,
			JSONException {
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

	public void newChromosome(String chr) throws FileNotFoundException{
		if(null!=chrOutJSON){
			write(","+previousEnd+",{\"chunk\":\""+previousChunkSize+"\"}",chrOutJSON);
		}
		super.newChromosome(chr);
	}
	
	
}

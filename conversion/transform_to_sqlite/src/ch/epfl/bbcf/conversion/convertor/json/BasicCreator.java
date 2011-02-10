package ch.epfl.bbcf.conversion.convertor.json;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONException;



/**
 * Handle JSON writing for processing it by the browser.
 * 
 * CHROMOSOMENAME.JSON (TRACKDATA.JSON in jBrowse)
 * {"headers":["start","end","strand","id"],
 * "histogramMeta":
 * 		[
 * 			{
 * 				"basesPerBin":"threshold T",
 * 				"arrayParams":
 * 						{
 * 							"length":ceil(chromosome length/T),
 * 							"chunkSize":10000,
 * 							"urlTemplate":"tracks/database.db/hist-T-{chunk}.json"
 * 						}
 * 			},
 * 			{
 * 				"basesPerBin":"T*100",
 * 				"arrayParams":{"length":ceil(chromosome length/(T*100))/,"chunkSize":10000,
 * 				"urlTemplate":"tracks/database.db/hist-T*100-{chunk}.json"}
 * 			}
 * 		],
 * "lazyIndex":2,
 * "featureCount":total feature count,
 * "histStats":
 * 		[
 * 	
 * 			{"bases":200000,"max":1275,"mean":338.873846153846},
 * 			{"bases":400000,"max":2344,"mean":677.747692307692},
 * 			{"bases":1000000,"max":5815,"mean":1694.36923076923},
 * 			{"bases":2000000,"max":11309,"mean":3388.73846153846},
 * 			{"bases":4000000,"max":22073,"mean":6674.78787878788},
 * 			{"bases":10000000,"max":53557,"mean":16943.6923076923},
 * 			{"bases":20000000,"max":104671,"mean":31466.8571428571},
 * 			{"bases":40000000,"max":196439,"mean":55067},
 * 			{"bases":100000000,"max":220268,"mean":110134}
 * 		(this array is from "bases":"T" to "bases":"100000000" -here T=20000-)
 * 		]
 * "key":"name of the feature",
 * "featureNCList":
 * 		[
 * 			[3000019,3189947,{"chunk":"0"}],
 * 			[3189950,3379362,{"chunk":"1001"}],
 * 			[3379365,3563998,{"chunk":"2002"}],
 * 			...,
 * 			[43950860,44158647,{"chunk":"198198"}],
 * 			[44158650,44327543,{"chunk":"199199"}]
 * 		(this array is : 'start','end','which chunk')
 * 		],
 * "className":"name of the display in css file",
 * "clientConfig":null, ->for displaying qualitatives tracks at big zoom level
 * "arrowheadClass":null,
 * "type":"FeatureTrack",
 * "label":"name of the feature",
 * "sublistIndex":4 (???) ,
 * "lazyfeatureUrlTemplate":"tracks/database.db/lazyfeatures-{chunk}.json"}
 * 
 * 
 * Threshold T :
 * from the differents zooms levels : 1,2,5,10,20,50,100,200,500,....,10000000
 * tempT = (chromosome length*2.5)/total feature count
 * T = upperZoomLevel(tempT) (e.g if tempT = 157, T = 200)
 * 
 * 
 * 
 * 
 * HIST-T-{CHUNK}.JSON
 * [0,456,357,201,....] of length (chromosome length/T)
 * if T > chuncksize (currently 10000) length = 10001 and the last file
 * take the remaining 
 * (e.g if my chromosome length/T =  64997 and T = 2000,
 * I will have 5 file hist-2000-{1 to 5}.json with a list length = 10001
 * and one file  hist-2000-6.json with list length = 4997)
 * 
 * 
 * LAZYFEATURE-{CHUNK}.JSON
 * [
 * 	[start,end,strand,id],[start,end,strand,id],...
 * ]
 * corresponding to the featureNCList
 * 
 * @author jarosz
 *
 */
public class BasicCreator extends JSONHandler{


	/**
	 * handle the writing of JSON 
	 * -enter at your own risks-
	 * @param inputFileFullPath - the ful path to the file parsed
	 * @param name - the name of the track
	 * @param outputDirectory - the ouputDir (MUST exist)
	 * @param ressourceURL - the ressource url to put in the browser which link to the track
	 */
	public BasicCreator(String inputFileFullPath,String fullPathDatabase,String name,String outputName,String outputDirectory,String ressourceURL) {
		super(inputFileFullPath,fullPathDatabase,name,outputName,ClientConfig.BASIC,outputDirectory,ressourceURL);
	}

	@Override
	protected void writeHeader(String chr, OutputStream out) {
		String str = "{\"headers\":[\"start\",\"end\",\"score\",\"name\",\"strand\"],";
		write(str,out);		
	}

	@Override
	public void writeValues(String chr, int start, int end, String name,
			int strand, JSONArray featureString, int featureCount, boolean finish,float score) throws FileNotFoundException, JSONException {
		previousEnd = end;
		curChunkSize++;
		if(null==chrOutJSON){
			chrOutJSON = newChrOutput(chr);
		}
		if(firstChrOut){
			previousChunkSize = 0;
			write("["+start,chrOutJSON);
		}
		if(curChunkSize%CHUNK_SIZE==0){
			theChunk.closeChunk();
			theChunk = new Chunk(curChunkSize,chr);
			//write in the chrOutput
			firstChunkOut=true;
			write(","+end+",{\"chunk\":\""+previousChunkSize+"\"}]",chrOutJSON);
			previousChunkSize = curChunkSize;
			write(",["+start,chrOutJSON);
		}
		
		String toWrite;
		if(firstChrOut || firstChunkOut){
			toWrite = "["+start+","+end+","+score+",\""+name+"\","+strand+"]";
			firstChrOut = false;
			firstChunkOut = false;
		} else {
			toWrite = "["+start+","+end+","+score+",\""+name+"\","+strand+"]";
		}
		theChunk.add(new JSONArray(toWrite));
	}

}










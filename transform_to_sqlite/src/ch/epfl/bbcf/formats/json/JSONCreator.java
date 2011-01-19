package ch.epfl.bbcf.formats.json;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.plaf.ListUI;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import ch.epfl.bbcf.conf.Configuration;
import ch.epfl.bbcf.daemon.Launcher;
import ch.epfl.bbcf.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.utility.FileManagement;

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
abstract class JSONCreator {

	protected static int[]zooms = {2, 5, 10, 20, 50, 100, 200, 500, 1000,
		2000, 5000, 10000, 20000, 50000, 100000,200000,500000,1000000};
	public static final Logger logger = Launcher.initLogger(JSONCreator.class.getName());
	protected String database;
	//protected Map<String, String> altsNames;
	protected List<String> chrs;
	//protected OutputStream chunk;
	protected OutputStream chrOutJSON;
	protected boolean firstChrOut;
	protected File file;
	protected int curChunkSize;
	protected int previousChunkSize;
	protected String name;
	protected int previousEnd;
	protected boolean firstChunkOut;
	protected ClientConfig clientConfig;
	protected static final int BUFF_SIZE = 1024;
	protected static final byte[] buffer = new byte[BUFF_SIZE];
	protected static final int CHUNK_SIZE = 1001;
	protected static final int HIST_CHUNK_SIZE = 10001;
	protected enum ClientConfig {BASIC,GFF};
	protected Chunk theChunk;


	/**
	 * the class to write the json needed by the browser
	 * @param database - the database file (md5)
	 * @param file - the file being parsed
	 * @param name - the name of the track
	 * @param config - the configuration for clientConfig
	 */
	public JSONCreator(String database, File file,String name,ClientConfig config) {
		this.database = database;
		this.name = name;
		this.file = file;
		//this.altsNames = altsNames;
		this.firstChrOut = true;
		this.chrs= new ArrayList<String>();
		this.clientConfig = config;
		File directory = new File(Configuration.getJbrowseOutput()+"/"+database);
		directory.mkdir();

	}



	/**
	 * method called when there is a new chromosome 
	 * in the file.
	 * - create directory for chromosome
	 * - close chunk if any
	 * - open new chunk
	 * 
	 * @param chr - the new chromosome
	 */
	public void newChromosome(String chr){
		firstChrOut = true;
		firstChunkOut = true;
		File dir = new File(Configuration.getJbrowseOutput()+"/"+database+"/"+chr);
		dir.mkdir();
		this.curChunkSize = 0;
		if(theChunk!=null){
			//close(chunk);
			theChunk.closeChunk();
		}
		if(null!=chrOutJSON){
			write("]",chrOutJSON);
			chrOutJSON = newChrOutput(chr);
		}
		this.chrs.add(chr);
		//chunk = newChunk(chr,curChunkSize);
		theChunk=new Chunk(curChunkSize,chr);
	}




	protected abstract void writeValues(String chr,int start,int end,float score,String name,int strand,int id);
	protected abstract void writeValues(String chr, int start, int end, String name,int strand, JSONArray featureString,int featureCount, boolean fininsh);
	protected abstract void writeHeader(String database2, String chr, OutputStream out);





	/**
	 * method called by the parser when the end of file
	 * is reached
	 */
	public void finalize(){
		//logger.error("finalize => "+previousEnd+"{\"chunk\":\""+previousChunkSize+"\"}]");
		//write("]",chunk);
		theChunk.closeChunk();
		//logger.debug("write ] to chunk : "+chunk.toString());
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
				if(featureCount>0 && chrLength>0){
					writeTrackDataAndHistoFiles(name,chrLength,threshold,featureCount,database,chr,out,clientConfig);
					//logger.debug("end : "+chr+":"+chrLength);
				} else {
					jsonFile.delete();
					logger.warn("not processed : "+chr+"(feature count = "+featureCount+" and chr length on genrep = "+chrLength+")");
				}



			} else {
				logger.error("outputstream for write chromosome trackData = null ("+file.getAbsolutePath()+")");
			}
		}
	}



	/**
	 * change the output file of the chromosome
	 * @param chr
	 * @return
	 */
	protected OutputStream newChrOutput(String chr) {
		try{
			File tmp = new File(Configuration.getJbrowseOutput()+"/"+database+"/"+chr+"/featureNClist.json");
			logger.debug("new chr output : "+Configuration.getJbrowseOutput()+"/"+database+"/"+chr+"/featureNClist.json");
			return new FileOutputStream(tmp,true);
		}catch(FileNotFoundException e){
			logger.error(e);
		}
		return null;
	}



	/**
	 * change the output file of the chunks files
	 * @param chr
	 * @param curChunkSize
	 * @return
	 */
	protected OutputStream newChunk(String chr, int curChunkSize) {
		try{
			File tmp = new File(Configuration.getJbrowseOutput()+"/"+database+"/"+chr+"/lazyfeatures-"+curChunkSize+".json");
			return new FileOutputStream(tmp,true);
		}catch(FileNotFoundException e){
			logger.error(e);
		}
		return null;
	}







	/**
	 * write the histo files (hist-T-{chunk}.json)
	 * @param jsonOutput 
	 * @param chrLength
	 * @param database2
	 * @param chr
	 * @param threshold
	 */
	protected static void writeHistoFiles(OutputStream jsonOutput, int chrLength, String database2, String chr,
			int threshold) {
		List<Integer> bigArray = new ArrayList<Integer>();
		//init ouputs
		int curChunk = 0;
		int curPos = 0;
		OutputStream outT = newHistChunkOutPut(database2,threshold,curChunk,chr);
		List<Integer> counts = new ArrayList<Integer>();

		int megathreshold = threshold*100;
		int mCurPos = 0;
		int mCurChunk = 0;
		int mCount = 0;
		OutputStream outMT = newHistChunkOutPut(database2,megathreshold,mCurChunk,chr);
		List<Integer> mCounts = new ArrayList<Integer>();
		//init connections
		Connection conn = SQLiteAccess.getConnectionOnFileDirectory(database2);
		ResultSet r = SQLiteAccess.getStartEndForChromosome(conn,database2, chr);
		List<Integer> starts = new ArrayList<Integer>();
		List<Integer> ends = new ArrayList<Integer>();
		try {
			while(r.next()){
				starts.add(r.getInt(1));
				ends.add(r.getInt(2));
			}
			r.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}

		int startCursor = 0;
		int endCursor = 0;



		for(int i=1;i<=chrLength;i+=threshold){
			int start = i;
			int end = i+threshold;
			startCursor = moveStartcursor(start,end,startCursor,starts,ends);
			endCursor = moveEndCursor(start,end,startCursor,endCursor,starts,ends);
			int count = getCount(start,end,startCursor,endCursor,starts,ends);
			//			logger.debug("START : "+start+" END :"+end);
			//			logger.debug("feat start("+startCursor+") --> sc : "+starts.get(startCursor)+" ec : "+ends.get(startCursor));
			//			logger.debug("feat end ("+endCursor+")--> sc : "+starts.get(endCursor)+" ec : "+ends.get(endCursor));
			//			logger.debug("count = "+count);
			counts.add(count);
			bigArray.add(count);
			curPos++;

			mCount+=count;
			if(curPos%100==0){
				mCounts.add(mCount);
				mCount=0;
				mCurPos++;
			}


			if(curPos%HIST_CHUNK_SIZE==0){
				write(counts.toString(),outT);
				counts=new ArrayList<Integer>();
				close(outT);
				curChunk++;
				outT = newHistChunkOutPut(database2,threshold,curChunk,chr);
			}
			if(mCurPos%HIST_CHUNK_SIZE==0 && mCurPos!=0){
				write(mCounts.toString(),outMT);
				mCounts=new ArrayList<Integer>();
				close(outMT);
				mCurChunk++;
				outMT = newHistChunkOutPut(database2,megathreshold,mCurChunk,chr);
			}

		}
		//finalize
		if(outT!=null){
			write(counts.toString(),outT);
			close(outT);
		}
		if(outMT!=null){
			write(mCounts.toString(),outMT);
			close(outMT);
		}
		writeHistoStats(threshold,jsonOutput,bigArray,chrLength);


	}

	protected static int getCount(int start, int end, int startCursor,
			int endCursor, List<Integer> starts, List<Integer> ends) {
		if((starts.get(startCursor)<=end && ends.get(startCursor)>=start)){
			return endCursor -startCursor +1;
		}
		return 0;
	}



	protected static int moveEndCursor(int start, int end, int startCursor,int endCursor, List<Integer> starts,List<Integer> ends) {
		for(int i=endCursor;i<starts.size();i++){
			endCursor=i;
			if(starts.get(i)>end){
				if(endCursor==0){
					return endCursor;
				}
				return endCursor-1;
			}
		}
		return endCursor;	
	}



	protected static int moveStartcursor(int start, int end, int startCursor, List<Integer> starts,List<Integer> ends) {
		for(int i=startCursor;i<ends.size();i++){
			startCursor=i;
			if(ends.get(i)>=start){
				return startCursor;
			}
			//			if(starts.get(i)<=end && ends.get(i)>=start){
			//				return startCursor;
			//			} else if((starts.get(i)>start && ends.get(i)>start)){
			//				return startCursor;
			//			}
		}
		return startCursor;
	}




	protected static void writeHistoStats(int threshold, OutputStream jsonOutput,
			List<Integer> bigArray, int chrLength) {
		String str = "\"histStats\":[";
		boolean first=true;
		//logger.debug("big array : "+bigArray.toString());
		for(int z : zooms){
			int base = z*threshold;
			//logger.debug("BASE : "+base);
			if(base<chrLength){
				if(!first){
					str+=",";
				}
				first=false;
				List<Integer>baseArray= new ArrayList<Integer>();
				for(int i=0;i<bigArray.size()-z-1;i+=z){
					List<Integer>tmpArray=bigArray.subList(i, i+z);
					//logger.debug("tmp array : "+tmpArray.toString());
					int c=0;
					for(int j :tmpArray){
						c+=j;
					}
					baseArray.add(c);

				}
				//			logger.debug("base array length :"+baseArray.size());
				//			logger.debug("base array : "+baseArray.toString());
				int max = 0;
				int mean = 0;
				for(int k:baseArray){
					mean+=k;
					if(k>max){
						max = k;
					}
				}
				mean = mean/baseArray.size();
				str+="{\"bases\":"+base+",\"max\":"+max+",\"mean\":"+mean+"}";
			} else {
				break;
			}
		}
		str+="],";
		write(str,jsonOutput);
	}



	protected static OutputStream newHistChunkOutPut(String database2,
			int threshold, int curChunk,String chr) {
		try {
			return new FileOutputStream(
					new File(Configuration.getJbrowseOutput()+"/"+database2+"/"+chr+"/hist-"+threshold+"-"+curChunk+".json"));
		} catch (FileNotFoundException e) {
			logger.error(e);
		};
		return null;
	}



	/**
	 * METHODS FOR WRITING THE JSON OUTPUT
	 * @param featureCount 
	 * @param additionnalsHeaders2 
	 *
	 *
	 *
	 *
	 *
	 */
	protected void writeTrackDataAndHistoFiles(String name2, int chrLength, int threshold,
			int featureCount, String database2, String chr, OutputStream out,ClientConfig clientConfig2) {
		writeHeader(database2,chr,out);
		writeHistogramMeta(chrLength,threshold,database2,chr,out);
		writeHistoFiles(out,chrLength,database2,chr,threshold);
		writeFeatureNCList(database2,chr,out);
		writeAdditionalsParameters(name2,database2,chr,featureCount,out,clientConfig2);
	}

	protected static void writeAdditionalsParameters(String name,String database2, String chr,
			int featureCount, OutputStream out,ClientConfig clientConfig2) {

		String str = "\"lazyIndex\":2," +
		"\"featureCount\":"+featureCount+"," +
		"\"key\":\""+name+"\"," +
		"\"className\":\"generic_parent\","+
		"\"clientConfig\":"+getClientConfig(clientConfig2)+","+
		"\"arrowheadClass\":\"transcript-arrowhead\","+
		"\"type\":\"FeatureTrack\","+
		"\"label\":\""+name+"\","+
		//"\"sublistIndex\":4 ,"+
		"\"lazyfeatureUrlTemplate\":\""+"../"+database2+"/"+chr+"/lazyfeatures-{chunk}.json\"}";
		write(str,out);
		close(out);
	}


	protected static String getClientConfig(ClientConfig ccfg) {
		switch(ccfg){
		case BASIC :return "{\"featureCss\":\"background-color: #0000FF; " +
		"height: 5px;\",\"histCss\":\"background-color: #0000FF;\"}";
		case GFF :
			return "{\"featureCallback\":\"" +
			"(function(feat, fields, div) { " +
			"if (fields.type){ " +
			"div.style.backgroundColor=\\\"#3333D7\\\";" +
			"div.className = \\\"basic\\\"; " +
			"switch (feat[fields.type]){ " +
			"case \\\"CDS\\\": " +
			"case \\\"thick\\\": " +
			"div.style.height=\\\"20px\\\"; " +
			"div.style.marginTop =\\\"-8px\\\"; " +
			"break; " +
			
			"case \\\"exon\\\": " +
			"div.style.height=\\\"8px\\\"; " +
			"div.style.marginTop =\\\"-3px\\\"; " +
			"break ;" +
			
			"case \\\"start_codon\\\": " +
			"case \\\"stop_codon\\\":" +
			"div.style.height=\\\"20px\\\"; " +
			"div.style.marginTop =\\\"-8px\\\"; " +
			"div.style.backgroundColor=\\\"red\\\";" +
			"div.style.zIndex=\\\"10\\\";" +
			"break ;" +
			
			"case \\\"UTR\\\": " +
			"case \\\"thin\\\": " +
			"div.style.height=\\\"8px\\\"; " +
			"div.style.marginTop=\\\"-3px\\\"; " +
			"div.style.backgroundColor=\\\"black\\\";" +
			"break;" +
			"}" +
			"}}" +
			")\"}";
		default:
			return getClientConfig(ClientConfig.BASIC);
		}
	}





	protected static void writeFeatureNCList(String database2, String chr, OutputStream out) {
		String str="\"featureNCList\":[";
		write(str,out);
		File tmp = new File(Configuration.getJbrowseOutput()+"/"+database2+"/"+chr+"/featureNClist.json");
		InputStream in;
		try {
			in = new FileInputStream(tmp);
			copy(in,out);
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
		write("],",out);
	}




	protected static void writeHistogramMeta(int chrLength, int threshold,
			String database2, String chr, OutputStream out) {
		String str = "\"histogramMeta\":[" +
		"{\"basesPerBin\":\""+threshold+"\"," +
		"\"arrayParams\":{" +
		"\"length\":"+Math.ceil(chrLength/threshold)+"," +
		"\"chunkSize\":10000," +
		"\"urlTemplate\":\""+Configuration.getRessourceURL()+"/"+database2+"/"+chr+"/hist-"+threshold+"-{chunk}.json"+"\"" +
		"}}";

		int megathreshold = threshold*100;
		if(megathreshold<=zooms[zooms.length-1]){
			str+=",{\"basesPerBin\":\""+megathreshold+"\"," +
			"\"arrayParams\":{" +
			"\"length\":"+Math.ceil(chrLength/megathreshold)+"," +
			"\"chunkSize\":10000," +
			"\"urlTemplate\":\""+Configuration.getRessourceURL()+"/"+database2+"/"+chr+"/hist-"+megathreshold+"-{chunk}.json"+"\"" +
			"}}";
		}
		str+="],";
		write(str,out);
	}






	protected static void add(JSONArray feature,OutputStream out){
		
	}



	/**
	 * convenient method for writing a string to a file
	 * @param toWrite
	 * @param out
	 */
	protected static void write(String toWrite,OutputStream out){
		try{
			InputStream in = new ByteArrayInputStream(toWrite.getBytes("UTF-8"));
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead); 
				}
			} 
		}catch (IOException e){
			logger.error(e);
		}
	}

	/**
	 * convenient method for copying a strem to another
	 * @param in
	 * @param out
	 */
	protected static void copy(InputStream in, OutputStream out) {
		try {
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead); 
				}
			}
		} catch (IOException e) {
			logger.error(e);
		}finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}

	}

	/**
	 * close an outputstream
	 * @param out
	 */
	protected static void close(OutputStream out){
		if(out!=null){
			try {
				out.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * Serve the purpose to write all JSONArray
	 * in the lazyFeature files
	 * 
	 * @author Yohan Jarosz
	 *
	 */
	protected class Chunk{
		private int chunkNumber;
		private String chr;
		private JSONArray array;
		
		protected Chunk(int nb,String chr){
			//logger.debug("new chunk for chr :"+chr+"with chunk nb ="+nb);
			this.chunkNumber = nb;
			this.chr = chr;
			this.array = new JSONArray();
		}
		
		protected void add(JSONArray feature){
			this.array.put(feature);
		}
		/**
		 * write the chunk to the output and then
		 * close it
		 */
		protected void closeChunk(){
//			logger.debug("closing chunk for chr :"+chr+" with chunk nb ="+chunkNumber);
//			logger.debug("writing on lazyFeature - "+curChunkSize);
			File tmp = new File(Configuration.getJbrowseOutput()+"/"+database+"/"+this.chr+"/lazyfeatures-"+chunkNumber+".json");
			try {
				OutputStream out = new FileOutputStream(tmp,true);
				write(this.array.toString(),out);
				close(out);
			} catch (FileNotFoundException e) {
				logger.error(e);
			}
		}
		
	}
	


}
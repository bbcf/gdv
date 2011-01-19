package ch.epfl.bbcf.formats.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.biojava.bio.program.gff.GFFDocumentHandler;
import org.biojava.bio.program.gff.GFFErrorHandler;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.program.gff.IgnoreRecordException;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;
import org.json.JSONArray;
import org.json.JSONException;

import ch.epfl.bbcf.daemon.Launcher;
import ch.epfl.bbcf.formats.json.GFFCreator;
import ch.epfl.bbcf.formats.sqlite.SQLiteQualitativeHandler;
import ch.epfl.bbcf.utility.ChromosomeNameHandler;
import ch.epfl.bbcf.utility.NCList;


/**
 * Class to handle the GFF files 
 * @author jarosz
 *
 */
public class CustomGFFHandler implements GFFErrorHandler,GFFDocumentHandler {

	public static final Logger logger = Launcher.initLogger(CustomGFFHandler.class.getName());
	//the twp handlers
	private SQLiteQualitativeHandler sqliteHandler;
	private GFFCreator jsonHandler;
	//the current chromosome
	private String chromosome;
	private GFFChromosome gffChromosome;
	//the nameHandler to find if a chromosome name in the file is named differently
	//on jbrowse
	private ChromosomeNameHandler nameHandler;
	private List<String> chromosomeNameList;


	//the différents types in the GFF file (exon,intron, UTR,....)
	private List<String> types;

	private static final String subFeatureHeaders = "[\"start\",\"end\",\"strand\",\"type\"]";

	private File file;

	private String nrassemblyId;



	/**
	 * 
	 * @param handler - the handler to create SQLite database
	 * @param writer - the handler to write JSON files
	 * @param file - the file being parsed
	 * @param jbrowsorId - the jbrowsor id of the assembly
	 * @param nrAssemblyId - the genrep id of the assembly
	 * @param chrList the chromosome list to parse
	 */
	public CustomGFFHandler(SQLiteQualitativeHandler handler, 
			GFFCreator writer,File file, String nrAssemblyId, List<String> chrList) {
		this.sqliteHandler = handler;
		this.jsonHandler = writer;
		this.file = file;
		types = new ArrayList<String>();
		this.chromosomeNameList = chrList;
		this.nameHandler = new ChromosomeNameHandler(nrAssemblyId);
		this.nrassemblyId = nrAssemblyId;
	}


	public void endDocument() {
		sqliteHandler.finalizeDatabase(nrassemblyId);
		if(gffChromosome!=null){
			gffChromosome.writeJSONOutput(jsonHandler);
		}
		jsonHandler.setTypes(types);
		jsonHandler.setSubFeatureHeaders(subFeatureHeaders);
		jsonHandler.finalize();
	}



	public void recordLine(GFFRecord rec) {
		String chr = rec.getSeqName();
		if(!chromosomeNameList.contains(chr)){
			chr = nameHandler.getChromosomeAltName(chr);
		}
		if(null==chr){
			return;
		}


		int start = rec.getStart();
		int end = rec.getEnd();
		String name = "";
		String id = "";
		Map<String,List<String>> attributes = rec.getGroupAttributes();

		if (!attributes.isEmpty()){
			Iterator<String> it = attributes.keySet().iterator();
			while(it.hasNext()){
				String str = it.next();
				if(str.equalsIgnoreCase("name") || str.equalsIgnoreCase("gene_name")){
					name=attributes.get(str).get(0);
				} 
				if(str.equalsIgnoreCase("id") || str.equalsIgnoreCase("gene_id")){
					id=attributes.get(str).get(0);
				} 
			}
		}
		float score = new Float(rec.getScore());
		int strand = getStrand(rec.getStrand());
		String type = rec.getFeature();


		//SQLITE & JSON

		if(null==chromosome || !chromosome.equalsIgnoreCase(chr)){
			chromosome = chr;
			sqliteHandler.newChromosome(chromosome);
		}
		sqliteHandler.writeValues(chr, start, end, score, name, strand, attributes);
		processJson(chr, start, end, score, name,id, strand,attributes, type);

	}

	/**
	 * process a line in the GFF and check, and add a 
	 * new GFF Feature to the current or to a new chromosome
	 * @param chr
	 * @param start
	 * @param end
	 * @param score
	 * @param name
	 * @param id
	 * @param strand
	 * @param rawAttributes
	 * @param type
	 */
	private void processJson(String chr, int start, int end, float score,
			String name, String id,int strand, Map<String, List<String>> rawAttributes,
			String type) {
		if(!types.contains(type)){
			types.add(type);
		}
		if(null==gffChromosome){
			gffChromosome = new GFFChromosome(chr);
			jsonHandler.newChromosome(chr);
		} else {
			if(!chr.equalsIgnoreCase(gffChromosome.getCurChr())){
				gffChromosome.writeJSONOutput(jsonHandler);
				gffChromosome = new GFFChromosome(chr);
				jsonHandler.newChromosome(chr);
			} 
		}
		gffChromosome.addFeature(id,name, start, end, strand, type);

	}





	private int getStrand(Strand strand) {
		int s = 0;
		if(strand.getToken()==45){
			s=-1;
		} else if(strand.getToken()==43){
			s = -1;
		}
		return s;
	}
	public void startDocument(String arg0) {
	}

	public int invalidEnd(String arg0) throws ParserException,
	IgnoreRecordException {
		logger.error("invalid end : "+arg0);
		return 0;
	}

	public int invalidFrame(String arg0) throws ParserException,
	IgnoreRecordException {
		logger.error("invalid frame : "+arg0);
		return 0;
	}

	public double invalidScore(String arg0) throws ParserException,
	IgnoreRecordException {
		logger.error("invalid score : "+arg0);
		return 0;
	}

	public int invalidStart(String arg0) throws ParserException,
	IgnoreRecordException {
		logger.error("invalid start : "+arg0);
		return 0;
	}

	public Strand invalidStrand(String arg0) throws ParserException,
	IgnoreRecordException {
		logger.error("invalid strand : "+arg0);
		return null;
	}

	public void commentLine(String arg0) {
	}


	/**
	 * @return the subfeatureheaders
	 */
	public static String getSubfeatureheaders() {
		return subFeatureHeaders;
	}





	/**
	 * represent feature in the gff file
	 * - a same feature as the same name -
	 * @author jarosz
	 *
	 */
	public class GFFFeat implements Comparable{

		private int start,end,strand,featureCount;
		private String type;
		private String parentName;
		private boolean firstSubFeature;

		public JSONArray subfeatures;
		public JSONArray feature;
		private String name;


		public GFFFeat(String name, int start2, int end2,int strand,String type) {
			this.start = start2;
			this.end = end2;
			this.strand = strand;
			this.type = type;
			this.featureCount=0;
			this.firstSubFeature = true;
			subfeatures = new JSONArray();
			this.name = name;
			//	feature = new JSONArray();
		}

		public void merge(GFFFeat old) {
			this.subfeatures = old.subfeatures;
			this.featureCount = old.featureCount+1;
			try {
				this.subfeatures.put(new JSONArray("["+start+","+end+","+strand+",\""+type+"\"]"));
			} catch (JSONException e) {
				logger.error(e);
			}
			if(old.start<this.start){
				this.start=old.start;
			}
			if(old.end>this.end){
				this.end=old.end;
			}
		}

		public void init() {
			this.featureCount++;
			try {
				this.subfeatures.put(new JSONArray("["+start+","+end+","+strand+",\""+type+"\"]"));
			} catch (JSONException e) {
				logger.error(e);
			}
		}

		public void initFeature(){
			try {
				this.feature=new JSONArray("["+start+","+end+",\""+parentName+"\","+strand+"]");
				if(featureCount>0){
					this.feature.put(this.subfeatures);
				}
			} catch (JSONException e) {
				logger.error(e);
			}



		}

		public int compareTo(Object o) {
			GFFFeat f = (GFFFeat)o;
			int res = this.start - f.start;
			if(res == 0){
				return f.end - this.end;
			}
			return res;
		}
		public int getStart(){
			return start;
		}
		public int getEnd(){
			return end;
		}
		public int getFeatureCount(){
			return featureCount;
		}
		public void setFeatureCount(int i){
			this.featureCount = i;
		}

		public void nesting(GFFFeat nextFeat) {
			
			JSONArray nested = null;
			try {
				if(this.feature.length()==GFFCreator.SUBLISTINDEX){
					nested = (JSONArray) this.feature.get(GFFCreator.SUBLISTINDEX-1);
				}
				if(null!=nested){
					nested.put(nextFeat.feature);
				} else {
					nested = new JSONArray();
					nested.put(nextFeat.feature);
				}
				this.feature.put(GFFCreator.SUBLISTINDEX-1,nested);
				//update count
				this.featureCount+=nextFeat.featureCount;
			} catch (JSONException e) {
				logger.error(e);
			}
		}

	}



	/**
	 * represent a chromosome
	 * @author jarosz
	 *
	 */
	private class GFFChromosome {
		private String curChr;
		private Map<String,GFFFeat> features;
		private int randomId;

		public GFFChromosome(String chr) {
			features = new HashMap<String, GFFFeat>();
			this.curChr = chr;
			randomId = 0;
		}

		/**
		 * add a feature to the chromosome
		 * @param id
		 * @param name
		 * @param start
		 * @param end
		 * @param strand
		 * @param type
		 */
		public void addFeature(String id,String name, int start, int end, int strand, String type){
			randomId++;
			GFFFeat feat = new GFFFeat(name,start,end,strand,type);
			if(id!=null && !id.equalsIgnoreCase("")){
				if(features.containsKey(id)){
					feat.merge(features.get(id));
				} else {
					feat.init();
				}
				features.put(id, feat);
			} else {
				features.put(Integer.toString(randomId), feat);
			}
		}

		public void writeJSONOutput(GFFCreator handler){
			List<GFFFeat> list = new ArrayList<GFFFeat>();
			for(Map.Entry<String, GFFFeat> entry : features.entrySet()){
				GFFFeat f = entry.getValue();
				if(f.name!=null && !f.name.equalsIgnoreCase("")){
					f.parentName = f.name;
				} else {
					f.parentName=entry.getKey();
				}
				list.add(f);
			}
			NCList.sort(list);
			list = NCList.arrange(list);
			boolean finish = false;
			for(int i=0;i<list.size();i++){
				GFFFeat f = list.get(i);
				if(i==list.size()-1){
					finish = true;
				}
				handler.writeValues(
						this.curChr, f.start,f.end,f.parentName,f.strand,f.feature, f.featureCount,finish);
			}
		}
		/**
		 * @param curChr the curChr to set
		 */
		public void setCurChr(String curChr) {
			this.curChr = curChr;
		}

		/**
		 * @return the curChr
		 */
		public String getCurChr() {
			return curChr;
		}

	}
}

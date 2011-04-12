package ch.epfl.bbcf.conversion.convertor;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import ch.epfl.bbcf.bbcfutils.parser.feature.Track;
import ch.epfl.bbcf.conversion.convertor.Convertor.FileExtension;
import ch.epfl.bbcf.conversion.convertor.json.BAMCreator;
import ch.epfl.bbcf.conversion.convertor.json.BasicCreator;
import ch.epfl.bbcf.conversion.convertor.json.GFFCreator;
import ch.epfl.bbcf.conversion.convertor.json.JSONHandler;
import ch.epfl.bbcf.conversion.convertor.json.JSONHandler.ClientConfig;
import ch.epfl.bbcf.conversion.convertor.json.NCList;
import ch.epfl.bbcf.conversion.daemon.Launcher;
import ch.epfl.bbcf.conversion.exception.JSONConversionException;
import ch.epfl.bbcf.conversion.feature.JSONFeature;

public class JBrowseConvertor {

	public static final Logger logger = Launcher.initLogger(JBrowseConvertor.class.getName());

	private String outputDirectory;
	private FileExtension extension;
	private JSONChromosome cur_chromosome;
	private JSONHandler json_handler;
	private String inputPath;
	private String ressourceUrl;
	private String databasePath;

	List<String> types;

	public JBrowseConvertor(String inputFileFullPath,
			String fullPathDatabase,
			String outputDirectory, 
			String ressourceUrl,
			String fileName, 
			String outputName,
			FileExtension extension
	) throws JSONConversionException {
		this.databasePath = fullPathDatabase;
		this.outputDirectory = outputDirectory;
		this.inputPath = inputFileFullPath;
		this.ressourceUrl = ressourceUrl;
		this.extension = extension;
		switch(extension){
		case BED:
			json_handler = new BasicCreator(inputPath, fullPathDatabase,fileName,outputName,outputDirectory, ressourceUrl);
			break;
		case WIG:

		case GFF:
			json_handler = new GFFCreator(inputPath, fullPathDatabase,fileName,outputName,outputDirectory, ressourceUrl,ClientConfig.EXTENDED);
			break;
		case BAM :
			json_handler = new BAMCreator(inputPath, fullPathDatabase,fileName,outputName, outputDirectory, ressourceUrl,ClientConfig.BAM);
			break;
		}
	}
	public GFFCreator getExtCreator(){
		if(json_handler instanceof GFFCreator){
			return (GFFCreator) json_handler;
		}
		return null;
	}

	public void newFeature(JSONFeature feature) throws JSONException, FileNotFoundException {
		if(null==cur_chromosome){
			cur_chromosome = new JSONChromosome(feature.getChromosome());
			json_handler.newChromosome(feature.getChromosome());
		} else if(!cur_chromosome.getChromosomeName().equalsIgnoreCase(feature.getChromosome())){
			cur_chromosome.writeJSONOutput(json_handler);
			cur_chromosome = new JSONChromosome(feature.getChromosome());
			json_handler.newChromosome(feature.getChromosome());
		}
		cur_chromosome.addFeature(feature);
	}

	public void newTrack(Track track) {
		// TODO Auto-generated method stub

	}

	public void start() {
		// TODO Auto-generated method stub

	}

	public void end() {
		try {
			cur_chromosome.writeJSONOutput(json_handler);
			logger.debug("output writed");
			json_handler.endIt();
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (JSONException e) {
			logger.error(e);
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		} 
	}

	/**
	 * class which handle all the features of a same chromosome
	 * @author Yohan Jarosz
	 *
	 */
	private class JSONChromosome{
		private String chromosome_name;
		/**
		 * Map containing all the features in the chromosome
		 * key = the id of the feature, value = a list of feature with the same id
		 */
		private Map<String,JSONFeat> features;
		private int randomId;

		public JSONChromosome(String chromosome){
			features = new HashMap<String,JSONFeat>();
			this.chromosome_name = chromosome;
			randomId = 0;
		}
		/**
		 * add a feature to the chromosome and store them in the Map features.
		 * @param feature - the feature to add
		 * @throws JSONException 
		 */
		public void addFeature(JSONFeature feature) throws JSONException{
			JSONFeat feat = new JSONFeat(
					feature.getName(),feature.getStart(),feature.getEnd(),feature.getStrand(),feature.getType());
			String key;
			if(feature.getId()==null || feature.getId().equalsIgnoreCase("")){
				randomId++;
				key = Integer.toString(randomId);
			} else {
				key = feature.getId();
			}
			if(features.containsKey(key)){
				feat.merge(features.get(key));
			} else {
				feat.init();
			}
			features.put(key, feat);
		}


		public void writeJSONOutput(JSONHandler handler) throws JSONException, FileNotFoundException{
			List<JSONFeat> list = new ArrayList<JSONFeat>();
			for(Map.Entry<String, JSONFeat> entry : features.entrySet()){
				JSONFeat f = entry.getValue();
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
				JSONFeat f = list.get(i);
				if(i==list.size()-1){
					finish = true;
				}
				handler.writeValues(
						this.chromosome_name, f.start,f.end,f.parentName,f.strand,f.feature, f.featureCount,finish,0);
			}
		}

		public String getChromosomeName(){
			return chromosome_name;
		}

	}






	/**
	 * class wich handler a single feature
	 * @author Yohan Jarosz
	 *
	 */
	public class JSONFeat implements Comparable{
		private int start,end,strand,featureCount;
		private String type;
		private String parentName;
		private boolean firstSubFeature;

		public JSONArray subfeatures;
		public JSONArray feature;
		private String name;


		public JSONFeat(String name, int start2, int end2,int strand,String type) {
			this.start = start2;
			this.end = end2;
			this.strand = strand;
			this.type = type;
			this.featureCount=0;
			this.firstSubFeature = true;
			subfeatures = new JSONArray();
			this.name = name;
		}

		/**
		 * merge the features
		 * for instance, if we have two features with the same id A(start,stop,strand,type) :
		 * A(0,50,1,exon) & A(60,70,1,CDS)
		 * we have to merge and it's become [0,70,A,1,[[0,50,1,exon],[60,70,1,CDS]]]
		 * @param old - the feature to merge with the same id
		 * @throws JSONException
		 */
		public void merge(JSONFeat old) throws JSONException {
			this.subfeatures = old.subfeatures;
			this.featureCount = old.featureCount+1;
			switch(extension){
			case BAM:
				this.subfeatures.put(new JSONArray("["+start+","+end+"]"));
				break;
			case GFF :
				this.subfeatures.put(new JSONArray("["+start+","+end+","+strand+",\""+type+"\"]"));
				break;
			}

			if(old.start<this.start){
				this.start=old.start;
			}
			if(old.end>this.end){
				this.end=old.end;
			}
		}
		/**
		 * initialize the feature if there is no existing id for it
		 * @throws JSONException
		 */
		public void init() throws JSONException {
			this.featureCount++;
			switch(extension){
			case BAM:
				this.subfeatures.put(new JSONArray("["+start+","+end+"]"));
				break;
			case GFF :
				this.subfeatures.put(new JSONArray("["+start+","+end+","+strand+",\""+type+"\"]"));
				break;
			}
		}





		public void initFeature() throws JSONException{
			this.feature=new JSONArray("["+start+","+end+",\""+parentName+"\","+strand+"]");
			if(featureCount>0){
				this.feature.put(this.subfeatures);
			}
		}

		/**
		 * method to sort the features
		 */
		public int compareTo(Object o) {
			JSONFeat f = (JSONFeat)o;
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

		/**
		 * nest a feature into another
		 * @param nextFeat
		 * @throws JSONException
		 */
		public void nesting(JSONFeat nextFeat) throws JSONException {
			JSONArray nested = null;
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
		}

	}
}

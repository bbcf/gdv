package ch.epfl.bbcf.conversion.convertor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import ch.epfl.bbcf.access.GenRepAccess;
import ch.epfl.bbcf.conversion.convertor.Convertor.FileExtension;
import ch.epfl.bbcf.conversion.daemon.Launcher;
import ch.epfl.bbcf.conversion.feature.GFFFeature;
import ch.epfl.bbcf.feature.BAMFeature;
import ch.epfl.bbcf.feature.BEDFeature;
import ch.epfl.bbcf.feature.Feature;
import ch.epfl.bbcf.feature.Track;
import ch.epfl.bbcf.feature.WIGFeature;
import ch.epfl.bbcf.sqlite.SQLiteAccess;

/**
 * convenient class which will parse a file
 * @author Yohan Jarosz
 *
 */
public class SQLiteConvertor{

	private static Logger log = Launcher.initLogger(SQLiteConvertor.class.getName());
	private SQLiteAccess handler;
	private Track track;
	private FileExtension extension;
	private String assemblyId;

	public SQLiteConvertor(String inputPath, FileExtension extension,String assemblyId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		handler = SQLiteAccess.getConnectionWithDatabase(inputPath);
		this.extension = extension;
		this.assemblyId = assemblyId;
	}
	public SQLiteConvertor(String inputPath, FileExtension extension, int limitQueriesSize,String assemblyId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		handler = SQLiteAccess.getConnectionWithDatabase(inputPath,limitQueriesSize);
		this.extension = extension;
		this.assemblyId = assemblyId;
	}


	public void newTrack(Track track) throws SQLException  {
		if(null!=track){
			//TODO create a new connection, a new database and close old one
		}
		this.setTrack(track);
		switch(extension){
		case WIG:
			handler.createNewDatabase("quantitative");
			break;
		case BED:case BAM:case GFF:
			handler.createNewDatabase("qualitative");
			break;
		}

	}





	public void newFeature(Feature feature) throws SQLException {
		
		switch(extension){
		case WIG:
			WIGFeature wig_feat = (WIGFeature)feature;
			if(!handler.isCromosomeCreated(wig_feat.getChromosome())){
				handler.newChromosome_quant(wig_feat.getChromosome());
			}
			handler.writeValues_quant(wig_feat.getChromosome(), wig_feat.getStart(), 
					wig_feat.getEnd(), wig_feat.getScore());
			break;
		case BED:
			BEDFeature bed_feat = (BEDFeature)feature;
			if(!handler.isCromosomeCreated(bed_feat.getChromosome())){
				handler.newChromosome_qual(bed_feat.getChromosome());
			}
			handler.writeValues_qual(
					bed_feat.getChromosome(), bed_feat.getStart(), bed_feat.getEnd(), 
					bed_feat.getScore(), bed_feat.getName(), bed_feat.getStrand());
			break;
		case BAM : 
			BAMFeature bam_feat = (BAMFeature)feature;
			if(!handler.isCromosomeCreated(bam_feat.getRefName())){
				handler.newChromosome_qual(bam_feat.getRefName());
			}
			handler.writeValues_qual(
					bam_feat.getRefName(), bam_feat.getStart(), 
					bam_feat.getStop(),0, bam_feat.getReadName(),0);
			break;
		case GFF : 
			GFFFeature gfff_feat = (GFFFeature)feature;
			if(!handler.isCromosomeCreated(gfff_feat.getChromosome())){
				handler.newChromosome_qual(gfff_feat.getChromosome());
			}
			handler.writeValues_qual(
					gfff_feat.getChromosome(), gfff_feat.getStart(), 
					gfff_feat.getEnd(),gfff_feat.getScore(), gfff_feat.getName(),gfff_feat.getStrand());
			break;
		}
	}



	public void start() {

	}

	public void end() throws SQLException {
		handler.commit();
		List<String> chrNames = handler.getChromosomesNames();
		GenRepAccess ga = null;
		try {
			ga = new GenRepAccess(assemblyId);
		} catch (JSONException e) {
			for (StackTraceElement el : e.getStackTrace()){
				log.error(el.getLineNumber()+" "+el.getMethodName()+" "+el.getClassName());
			}
			log.error(e);
		} catch (IOException e) {
			for (StackTraceElement el : e.getStackTrace()){
				log.error("--"+el.getLineNumber()+" "+el.getMethodName()+" "+el.getClassName());
			}
			log.error(e);
		}
		Map<String,Integer> map = new HashMap<String, Integer>();
		for(String chr : chrNames){
			int length = 0;
			try {
				if(null!=ga){
					length = ga.getChrLength(chr);
				} else {
					log.error("no connection to Genrep");
				}
			} catch (JSONException e) {
				for (StackTraceElement el : e.getStackTrace()){
					log.error("++"+el.getLineNumber()+" "+el.getMethodName()+" "+el.getClassName());
				}
				log.error(e);
			}
			if(length!=0){
				map.put(chr,length);
			}
		}
		log.debug("finalize db");
		handler.finalizeDatabase(map);
		log.debug("finalized");
		handler.close();
	}

	public void finalize(){
		try {
			handler.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			handler.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void setTrack(Track track) {
		this.track = track;
	}
	public Track getTrack() {
		return track;
	}
}

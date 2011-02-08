package ch.epfl.bbcf.conversion.convertor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.bbcf.conversion.convertor.Convertor.FileExtension;
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

	private SQLiteAccess handler;
	private Track track;
	private FileExtension extension;
	
	public SQLiteConvertor(String inputPath, FileExtension extension) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		handler = SQLiteAccess.getConnectionWithDatabase(inputPath);
		this.extension = extension;
	}
	public SQLiteConvertor(String inputPath, FileExtension extension, int limitQueriesSize) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		handler = SQLiteAccess.getConnectionWithDatabase(inputPath,limitQueriesSize);
		this.extension = extension;
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
				System.out.println("create new db");
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
		Map<String,Integer> map = new HashMap<String, Integer>();
		for(String chr : chrNames){
			map.put(chr,100000);
		}
		handler.finalizeDatabase(map);
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

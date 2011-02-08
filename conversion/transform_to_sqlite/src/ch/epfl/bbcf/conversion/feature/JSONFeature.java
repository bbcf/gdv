package ch.epfl.bbcf.conversion.feature;

import ch.epfl.bbcf.feature.BAMFeature;
import ch.epfl.bbcf.feature.BEDFeature;
import ch.epfl.bbcf.feature.Feature;


public class JSONFeature {

	private String chromosome,id,name,type;
	private int start,end,strand;
	
	
	
	
	
	public JSONFeature(Feature feature) {
		if(feature instanceof GFFFeature){
			GFFFeature feat = (GFFFeature)feature;
			setChromosome(feat.getChromosome());
			setId(feat.getId());
			setName(feat.getName());
			setType(feat.getType());
			setStart(feat.getStart());
			setEnd(feat.getEnd());
			setStrand(feat.getStrand());
		} else if(feature instanceof BAMFeature){
			BAMFeature feat = (BAMFeature)feature;
			setName(feat.getRefName());
			setStart(feat.getStart());
			setEnd(feat.getStop());
			setId(feat.getReadName());
		} else if(feature instanceof BEDFeature){
			BEDFeature feat = (BEDFeature)feature;
			setName(feat.getName());
			setStart(feat.getStart());
			setEnd(feat.getEnd());
			setStrand(feat.getStrand());
		}
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}
	public String getChromosome() {
		return chromosome;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getStart() {
		return start;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public int getEnd() {
		return end;
	}
	public void setStrand(int strand) {
		this.strand = strand;
	}
	public int getStrand() {
		return strand;
	}
	
	
	
}

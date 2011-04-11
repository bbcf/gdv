package ch.epfl.bbcf.conversion.feature;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.parser.feature.Feature;


public class GFFFeature implements Feature{

	private String chromosome;
	private int start;
	private int end;
	private String name;
	private String id;
	private float score;
	private int strand;
	private String type;

	public GFFFeature(String chromosome, int start, int end, String name, String id,
			float score, int strand, String type) {
		this.setChromosome(chromosome);
		this.setStart(start);
		this.setEnd(end);
		this.setName(name);
		this.setId(id);
		this.setScore(score);
		this.setStrand(strand);
		this.setType(type);
	}

	@Override
	public String detail() {
		return "GFFFeature : chr: "+chromosome+" " +
				"start "+start+" stop "+end+" name "+
				name+" id "+id+" "+id+" score "+
				score+" strand "+strand+" type "+type;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public String getChromosome() {
		return chromosome;
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

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public float getScore() {
		return score;
	}

	public void setStrand(int strand) {
		this.strand = strand;
	}

	public int getStrand() {
		return strand;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	

}

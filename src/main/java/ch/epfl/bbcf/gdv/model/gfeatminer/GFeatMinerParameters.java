package ch.epfl.bbcf.gdv.model.gfeatminer;

import java.util.HashMap;
import java.util.Map;


public class GFeatMinerParameters {

	public static Map<String,String> getGFeatMinerParameters(){
		Map<String,String>map = new HashMap<String, String>();
		map.put("version","0.1.0");
		map.put("track1","/home/sinclair/gMiner/data/yeast/all_yeast_genes.bed");
		map.put("track1_name","hg19 refSeq genome-wide from UCSC");
		map.put("track2","/home/sinclair/gMiner/data/yeast/ribosome_proteins.bed");
		map.put("track2_name","hg19 HIV integration sites from liftOver");
		map.put("selected_regions","chr2:0:300000;chr5:0:200000");
		map.put("operation_type","desc_stat");
		map.put("characteristic","number_of_features");
		map.put("per_chromosome","True");
		map.put("compare_parents","False");
		map.put("wanted_chromosomes","chr1;chr2;chr5;chr6;chr7;chr8;chr9;chr10;chr11;hr12;chr13;chr14;chr15;chr16;chr17;chr18;chr19;chr20;chr21;chr22;chrX;chrY");
		return map;
	}
}

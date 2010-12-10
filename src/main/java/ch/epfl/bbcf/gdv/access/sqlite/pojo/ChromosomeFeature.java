package ch.epfl.bbcf.gdv.access.sqlite.pojo;

import java.util.ArrayList;
import java.util.List;

public class ChromosomeFeature {

	private String chr;
	private List<Integer> start,stop;
	private  List<Float> score;
	private String name;
	
	public ChromosomeFeature(){
		start = new ArrayList<Integer>();
		stop = new ArrayList<Integer>();
		score = new ArrayList<Float>();
	}
	public void addScore(Float score){
		this.score.add(score);
	}
	public void addStart(int start){
		this.start.add(start);
	}
	public void addStop(int stop){
		this.stop.add(stop);
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(List<Integer> start) {
		this.start = start;
	}
	/**
	 * @return the start
	 */
	public List<Integer> getStart() {
		return start;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(List<Float> score) {
		this.score = score;
	}
	/**
	 * @return the score
	 */
	public List<Float> getScore() {
		return score;
	}
	/**
	 * @param stop the stop to set
	 */
	public void setStop(List<Integer> stop) {
		this.stop = stop;
	}
	/**
	 * @return the stop
	 */
	public List<Integer> getStop() {
		return stop;
	}
	/**
	 * @param chr the chr to set
	 */
	public void setChr(String chr) {
		this.chr = chr;
	}
	/**
	 * @return the chr
	 */
	public String getChr() {
		return chr;
	}
	public String toString(){
		String str="";
		for(int i=0;i<score.size();i++){
			str+="s- "+start.get(i)+" e- "+stop.get(i)+" sc- "+score.get(i)+"\n";
		}
		return "chr :"+chr+"\n"+str;
		
	}
	public void setName(String string) {
		this.name = string;
	}
	public String getName(){
		return this.name;
	}
}

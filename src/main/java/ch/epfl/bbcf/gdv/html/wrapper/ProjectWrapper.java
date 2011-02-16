package ch.epfl.bbcf.gdv.html.wrapper;

import java.io.Serializable;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

public class ProjectWrapper implements Serializable{
	
	private Project project;
	private String speciesName;
	private int speciesId;
	private List<SelectOption> sequences;
	private int tracksNumber;
	private List<TrackWrapper> tracks;
	private boolean listOpen;//if the list of track is open or not on the view
	private String groupName;
	
	
	public ProjectWrapper(Project p){
		this.project = p;
		this.setListOpen(false);
	}

	public int getId(){
		return this.project.getId();
	}
	public int getSequenceId(){
		return this.project.getSequenceId();
	}
	public void setSequenceId(int seqId){
		this.project.setCurrentSequenceId(seqId);
	}
	public String getDescription(){
		return this.project.getName();
	}

	/**
	 * @param species the species to set
	 */
	public void setSpeciesId(int speciesId) {
		this.speciesId = speciesId;
	}

	/**
	 * @return the species
	 */
	public int getSpeciesId() {
		return speciesId;
	}

	/**
	 * @param tracksNumber the tracksNumber to set
	 */
	public void setTracksNumber(int tracksNumber) {
		this.tracksNumber = tracksNumber;
	}

	/**
	 * @return the tracksNumber
	 */
	public int getTracksNumber() {
		return tracksNumber;
	}

	/**
	 * @param tracks the tracks to set
	 */
	public void setTracks(List<TrackWrapper> tracks) {
		this.tracks = tracks;
	}

	/**
	 * @return the tracks
	 */
	public List<TrackWrapper> getTracks() {
		return tracks;
	}

	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public void setSequences(List<SelectOption> sequences) {
		this.sequences = sequences;
	}

	public List<SelectOption> getSequences() {
		return sequences;
	}

	public void setListOpen(boolean listOpen) {
		this.listOpen = listOpen;
	}

	public boolean isListOpen() {
		return listOpen;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupName() {
		return groupName;
	}

}

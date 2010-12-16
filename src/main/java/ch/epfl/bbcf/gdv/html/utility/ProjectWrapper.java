package ch.epfl.bbcf.gdv.html.utility;

import java.io.Serializable;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.pojo.Project;

public class ProjectWrapper implements Serializable{
	
	private Project project;
	private String species;
	private String version;
	private String versionId;
	private int tracksNumber;
	private List<TrackWrapper> tracks;
	
	
	public ProjectWrapper(Project p){
		this.project = p;
	}

	public int getId(){
		return this.project.getId();
	}
	public int getSequenceId(){
		return this.project.getSequenceId();
	}
	public String getDescription(){
		return this.project.getName();
	}

	/**
	 * @param species the species to set
	 */
	public void setSpecies(String species) {
		this.species = species;
	}

	/**
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
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

	/**
	 * @param versionId the versionId to set
	 */
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	/**
	 * @return the versionId
	 */
	public String getVersionId() {
		return versionId;
	}
	
}

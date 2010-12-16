package ch.epfl.bbcf.gdv.html.utility;

import java.util.Comparator;
import java.util.Date;

import ch.epfl.bbcf.gdv.access.database.pojo.Track;

public class TrackWrapper {

	private Track track;
	private Date date;

	public TrackWrapper(Track t) {
		this.track= t;
	}
	public Track getTrackInstance() {
		return track;
	}
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(String parameters) {
		this.track.setParameters(parameters);
	}
	/**
	 * @return the parameters
	 */
	public String getParameters() {
		return this.track.getParameters();
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.track.setName(name);
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return this.track.getName();
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.track.setStatus(status);
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		if(track!=null){
			return this.track.getStatus();
		}
		return "";
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.track.setId(id);
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return this.track.getId();
	}
	@Override
	public boolean equals(Object o){
		if(o instanceof Track){
			Track t = (Track)o;
			return this.getId()==t.getId();
		}
		return false;

	}
	/**
	 * @param filetype the filetype to set
	 */
	public void setType(String filetype) {
		this.track.setType(filetype);
	}
	/**
	 * @return the filetype
	 */
	public String getType() {
		return this.track.getType();
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}




	public static class SortByTrackName implements Comparator<TrackWrapper>{
		public int compare(TrackWrapper o1, TrackWrapper o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
	public static class SortByDate implements Comparator<TrackWrapper>{
		public int compare(TrackWrapper o1, TrackWrapper o2) {
			if(o1.getDate()!=null && o2.getDate()!=null){
				return o1.getDate().compareTo(o2.getDate());
			}
			return 0;
		}
	}


}


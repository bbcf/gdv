package ch.epfl.bbcf.gdv.model.pojos.json;

import ch.epfl.bbcf.bbcfutils.parsing.SQLiteExtension;
import ch.epfl.bbcf.gdv.config.Application;

public class TrackInfo {

	private String url,label,key,type,color,args;
	private boolean admin;
	/**
	 * 	String params = "{\n\"url\" : \"../"+directory+"/{refseq}.json\",\n" +
				"\"label\" : \""+protect(t.getName())+"\",\n"+
				"\"type\" : \""+imageType+"\",\n"+
				"\"key\" : \""+protect(t.getName())+"\"\n}";
	 * @param directory
	 * @param protect
	 * @param imageType
	 * @param protect2
	 */
	public TrackInfo(String directory, String label, SQLiteExtension type,
			String key) {
		this.url = "../"+directory+"/{refseq}.json";
		this.label = label;
		this.key = key;
		switch(type){
		case QUALITATIVE:
		case QUALITATIVE_EXTENDED:
			this.type="FeatureTrack";
			break;
		case QUANTITATIVE :
			this.type="ImageTrack";
			this.color = "red";
			break;
		}
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getKey() {
		return key;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getColor() {
		return color;
	}
}

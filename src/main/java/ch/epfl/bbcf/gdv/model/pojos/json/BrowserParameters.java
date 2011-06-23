package ch.epfl.bbcf.gdv.model.pojos.json;


public class BrowserParameters {

	
	private String containerID,refSeqs,browserRoot,dataRoot,styleRoot,trackData,defaultTracks;

	public BrowserParameters(String containerID, String refSeqs,
			String jb_browser_root, String jb_data_root, String styleroot,
			String trackData, String tracksNames) {
		this.containerID = containerID;
		this.refSeqs = refSeqs;
		this.browserRoot = jb_browser_root;
		this.dataRoot = jb_data_root;
		this.styleRoot = styleroot;
		this.trackData = trackData;
		this.defaultTracks = tracksNames;
	}

	public void setRefSeqs(String refSeqs) {
		this.refSeqs = refSeqs;
	}

	public String getRefSeqs() {
		return refSeqs;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public String getContainerID() {
		return containerID;
	}

	public void setTrackData(String trackData) {
		this.trackData = trackData;
	}

	public String getTrackData() {
		return trackData;
	}

	public void setStyleRoot(String styleRoot) {
		this.styleRoot = styleRoot;
	}

	public String getStyleRoot() {
		return styleRoot;
	}

	public void setBrowserRoot(String browserRoot) {
		this.browserRoot = browserRoot;
	}

	public String getBrowserRoot() {
		return browserRoot;
	}

	public void setDefaultTracks(String defaultTracks) {
		this.defaultTracks = defaultTracks;
	}

	public String getDefaultTracks() {
		return defaultTracks;
	}

	public void setDataRoot(String dataRoot) {
		this.dataRoot = dataRoot;
	}

	public String getDataRoot() {
		return dataRoot;
	}
}

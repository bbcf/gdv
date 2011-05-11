package ch.epfl.bbcf.conversion.daemon;

public class Job {

	private String 
	indb,inPath,outdb,outPath,
	mail,feedbackUrl,tmpDir;

	private int trackId,rapidity;
	
	public void setTrackId(int trackId) {
		this.trackId = trackId;
	}

	public int getTrackId() {
		return trackId;
	}

	public void setOutdb(String outdb) {
		this.outdb = outdb;
	}

	public String getOutdb() {
		return outdb;
	}

	public void setInPath(String inPath) {
		this.inPath = inPath;
	}

	public String getInPath() {
		return inPath;
	}

	public void setIndb(String indb) {
		this.indb = indb;
	}

	public String getIndb() {
		return indb;
	}

	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}

	public String getOutPath() {
		return outPath;
	}

	public void setFeedbackUrl(String feedbackUrl) {
		this.feedbackUrl = feedbackUrl;
	}

	public String getFeedbackUrl() {
		return feedbackUrl;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMail() {
		return mail;
	}

	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}

	public String getTmpDir() {
		return tmpDir;
	}

	public void setRapidity(int rapidity) {
		this.rapidity = rapidity;
	}

	public int getRapidity() {
		return rapidity;
	}
	public String toString(){
		return "trackId = "+trackId+" -input db "+indb+" with path : "+inPath+"\n" +
			" -output db : "+outdb+" with path : "+outPath+"\n"+
			" -fast:slow ? "+rapidity+" -mail : "+mail+"\n" +
					"tmp dir : "+tmpDir +" feedback : "+feedbackUrl;
	}
}

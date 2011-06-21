package ch.epfl.bbcf.conversion.daemon;

public class Job {

	private String file,tmpdir,extension,mail;
	private int trackId,nrAssemblyId;
	private String outputDirectory;
	private String jbrowseOutputDirectory;
	private String feedbackUrl;
	private String jbrowseRessourcesUrl;
	
	private boolean doit;
	
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getExtension() {
		return extension;
	}
	public void setTmpdir(String tmpdir) {
		this.tmpdir = tmpdir;
	}
	public String getTmpdir() {
		return tmpdir;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getFile() {
		return file;
	}
	public void setTrackId(int trackId) {
		this.trackId = trackId;
	}
	public int getTrackId() {
		return trackId;
	}
	public void setNrAssemblyId(int nrAssemblyId) {
		this.nrAssemblyId = nrAssemblyId;
	}
	public int getNrAssemblyId() {
		return nrAssemblyId;
	}
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	public String getOutputDirectory() {
		return outputDirectory;
	}
	public void setJbrowseOutputDirectory(String jbrowseOutputDirectory) {
		this.jbrowseOutputDirectory = jbrowseOutputDirectory;
	}
	public String getJbrowseOutputDirectory() {
		return jbrowseOutputDirectory;
	}
	
	public void setFeedbackUrl(String feedbackUrl) {
		this.feedbackUrl = feedbackUrl;
	}
	public String getFeedbackUrl() {
		return feedbackUrl;
	}
	public void setJbrowseRessourcesUrl(String jbrowseRessourcesUrl) {
		this.jbrowseRessourcesUrl = jbrowseRessourcesUrl;
	}
	public String getJbrowseRessourcesUrl() {
		return jbrowseRessourcesUrl;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getMail() {
		return mail;
	}
	public String toString(){
		return "file("+file+"),trackId("+trackId+"),tmpdir("+tmpdir+"),extension("+extension+")," +
						"mail("+mail+"),nrassemblyid("+nrAssemblyId+"),outputDirectory("+outputDirectory+")," +
								"jbrowseOutputDirectory("+jbrowseOutputDirectory+"),feedbackUrl("+feedbackUrl+")," +
										"jbrowseRessourcesUrl("+jbrowseRessourcesUrl+")";
	}
	public void setDoit(boolean doit) {
		this.doit = doit;
	}
	public boolean isRunnable() {
		return (trackId!=-1 && file!=null && outputDirectory!=null);
		
	}
}

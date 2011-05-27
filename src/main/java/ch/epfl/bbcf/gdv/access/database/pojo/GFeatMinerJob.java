package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class GFeatMinerJob implements Serializable{

	private int id,projectId,status;
	private String result;
	
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public int getProjectId() {
		return projectId;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getStatus() {
		return status;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getResult() {
		return result;
	}
}

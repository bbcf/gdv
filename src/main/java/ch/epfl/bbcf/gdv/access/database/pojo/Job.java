package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;

public class Job implements Serializable{

	public enum JOB_TYPE {new_selection,gfeatminer,new_track};

	public enum JOB_OUTPUT {reload,image} ;

	private int id,status,projectId;
	private String data;

	private JOB_TYPE type;
	private JOB_OUTPUT output;

	public void setStatus(int status) {
		this.status = status;
	}
	public int getStatus() {
		return status;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getData() {
		return data;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public int getProjectId() {
		return projectId;
	}
	public void setType(String t) {
		if(t.equalsIgnoreCase(JOB_TYPE.gfeatminer.toString())){
			this.type = JOB_TYPE.gfeatminer;
		} else if(t.equalsIgnoreCase(JOB_TYPE.new_selection.toString())){
			this.type = JOB_TYPE.new_selection;
		} else if(t.equalsIgnoreCase(JOB_TYPE.new_track.toString())){
			this.type = JOB_TYPE.new_track;
		}

	}
	
	public JOB_TYPE getType() {
		return type;
	}
	
	public void setOutput(String o) {
		if(o.equalsIgnoreCase(JOB_OUTPUT.image.toString())){
			this.output=JOB_OUTPUT.image;
		} else if(o.equalsIgnoreCase(JOB_OUTPUT.reload.toString())){
			this.output=JOB_OUTPUT.reload;
		}
	}
	
	public JOB_OUTPUT getOutput() {
		return output;
	}

}

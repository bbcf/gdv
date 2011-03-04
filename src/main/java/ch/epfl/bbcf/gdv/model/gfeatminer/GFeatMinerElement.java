package ch.epfl.bbcf.gdv.model.gfeatminer;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;

import ch.epfl.bbcf.gdv.access.gfeatminer.GFeatMinerAccess;
import ch.epfl.bbcf.gdv.config.Application;


public class GFeatMinerElement implements Serializable{

	private String label;
	private Map<String, String> params;

	public GFeatMinerElement(String label,Map<String,String>params) {
		this.label = label;
		this.params = params;
	}



	public void doJob(AjaxRequestTarget target){
		if(null!=params){
			String result = GFeatMinerAccess.sendReauest(params);
			Application.debug(result);
		}
	}


	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}



}

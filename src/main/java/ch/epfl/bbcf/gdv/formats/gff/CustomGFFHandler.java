package ch.epfl.bbcf.gdv.formats.gff;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.program.gff.GFFDocumentHandler;
import org.biojava.bio.program.gff.GFFErrorHandler;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.program.gff.IgnoreRecordException;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.dao.ChromosomesNamesAssociationDAO;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;

/**
 * Class to handle the GFF files 
 * @author jarosz
 *
 */
public class CustomGFFHandler implements GFFErrorHandler,GFFDocumentHandler {

	private String tag;
	private Map<String,String> jSONDescriptor;
	private int idDescriptor;
	private Map<String, String> altsNames;

	public CustomGFFHandler(String tag){
		this.tag = tag;
		if(tag.equalsIgnoreCase("JSONDescriptor")){
			jSONDescriptor=new HashMap<String, String>();
			idDescriptor=0;
		}

	}
	public void endDocument() {
	}

	//PROCESS WHILE READING THE FILE
	public void recordLine(GFFRecord rec) {
		if(tag.equalsIgnoreCase("JSONDescriptor")){
			idDescriptor++;
			//strand
			int strand =1;
			if(rec.getStrand().getToken()==45){
				strand=-1;
			}
			//name
			String name="";
			Map map = rec.getGroupAttributes();
			Map attributes = new Hashtable();
			if (!map.isEmpty()){
				Iterator it = map.keySet().iterator();
				while(it.hasNext()){
					String str = (String) it.next();
					String[] tmp = str.split("=");
					if(tmp[0].equalsIgnoreCase("name")){
						name=tmp[1];
					}
				}
			}
			//update the name if there is alt one
			if(null!=altsNames){
				if(altsNames.get(name)!=null){
					String[] names = altsNames.get(name).split(",");
					name = names[0];
					Application.debug("alt name : "+name);
				}
			}
			//update the right JSONDescriptor
			String descriptor =null;
			if(jSONDescriptor.get(rec.getSeqName())!=null){
				descriptor = jSONDescriptor.get(rec.getSeqName());
				//Application.debug("descriptor : "+descriptor);
				//to have the feature count in the result
				int index = descriptor.indexOf("[");
				int featureCount = Integer.parseInt(descriptor.substring(0,index));
				featureCount++;
				descriptor = featureCount+descriptor.substring(index);
				
				
				
				descriptor+="["+rec.getStart()+","+
				rec.getEnd()+","+strand+","+idDescriptor+",\""+name+"\"],";
			} else {
				descriptor ="1["+rec.getStart()+","+
				rec.getEnd()+","+strand+","+idDescriptor+",\""+name+"\"],";
			}
			jSONDescriptor.put(rec.getSeqName(), descriptor);
			

		} 
		


		else {
//			Map map = rec.getGroupAttributes();
//			Map attributes = new Hashtable();
//			if (!map.isEmpty()){
//				Iterator it = map.keySet().iterator();
//				while(it.hasNext()){
//					String str = (String) it.next();
//					String[] tmp = str.split("=");
//					attributes.put(tmp[0], tmp[1]);
//				}
			}

	}

	public void startDocument(String arg0) {
	}

	public int invalidEnd(String arg0) throws ParserException,
	IgnoreRecordException {
		Application.error("invalid end : "+arg0);
		return 0;
	}

	public int invalidFrame(String arg0) throws ParserException,
	IgnoreRecordException {
		Application.error("invalid frame : "+arg0);
		return 0;
	}

	public double invalidScore(String arg0) throws ParserException,
	IgnoreRecordException {
		Application.error("invalid score : "+arg0);
		return 0;
	}

	public int invalidStart(String arg0) throws ParserException,
	IgnoreRecordException {
		Application.error("invalid start : "+arg0);
		return 0;
	}

	public Strand invalidStrand(String arg0) throws ParserException,
	IgnoreRecordException {
		Application.error("invalid strand : "+arg0);
		return null;
	}

	public void commentLine(String arg0) {
	}


	/**
	 * @param jSONDescriptor the jSONDescriptor to set
	 */
	public void setJSONDescriptor(String jSONDescriptor) {
		jSONDescriptor = jSONDescriptor;
	}


	/** get the JSONDescriptor HashMap featureName: featureCount[[start,end,strand,id,geneName],....]
	 * @return the jSONDescriptor
	 */
	public Map<String, String> getJSONDescriptor() {
		return jSONDescriptor;
	}
	public int getFeatureCount() {
		return idDescriptor;
	}


	public void setAltNamesMap(Map<String, String> altsNames) {
		this.altsNames = altsNames;
		
	}


}



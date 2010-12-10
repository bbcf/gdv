package ch.epfl.bbcf.gdv.control.model;

import java.util.List;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.dao.SequenceDAO;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.generep.AssembliesAccess;
import ch.epfl.bbcf.gdv.access.generep.ChromosomeAccess;
import ch.epfl.bbcf.gdv.access.generep.GenomesAccess;
import ch.epfl.bbcf.gdv.access.generep.RepositoryAccess;
import ch.epfl.bbcf.gdv.access.generep.SpeciesAccess;
import ch.epfl.bbcf.gdv.access.jbrowsor.JbrowsoRAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;

public class SequenceControl extends Control{

	public SequenceControl(UserSession session) {
		super(session);
	}

	/**
	 * look if the sequence with the assemblyId is created on JBrowsoR
	 * @param assemblyId
	 * @param value
	 * @return
	 */
	public boolean isCreatedOnJBrowsoR(String assemblyId, String value) {
		SequenceDAO gDAO = new SequenceDAO(Connect.getConnection(session));
		int jbid = gDAO.getJBGenomeId(Integer.valueOf(assemblyId));
		if(jbid!=-1){
			return JbrowsoRAccess.checkGenomeCreation(jbid);
		}
		return false;
	}

	public Sequence getSequenceFromId(int sequenceId) {
		SequenceDAO gDAO = new SequenceDAO(Connect.getConnection(session));
		return gDAO.getSequenceFromId(sequenceId);
	}

	
	public boolean createGenome(String assemblyId, String speciesId, FeedbackPanel feedback) {
		JSONObject assembly = AssembliesAccess.getAssemblyById(assemblyId);
		if(null==assembly){
			Application.error("error in retriving assembly from JBrowsoR", session.getUserId());
			feedback.error("An error occurs : something went wrong with JBrowsoR (assembly)");
			return false;
		}
		JSONObject species = SpeciesAccess.getOrganismById(speciesId);
		if(null==species){
			Application.error("error in retriving species from JBrowsoR", session.getUserId());
			feedback.error("An error occurs : something went wrong with JBrowsoR (species)");
			return false;
		}
		
		String version = null;
		String taxId = null;
		String speciesName = null;
		JSONArray chrArray = new JSONArray();
		try {
			version = assembly.getString(AssembliesAccess.NAME_KEY);
			taxId = species.getString(SpeciesAccess.TAXID_KEY);
			speciesName = species.getString(SpeciesAccess.NAME_KEY);
			List<JSONObject> chromosomes = AssembliesAccess.getChromosomesByAssemblyId(assemblyId);
			for(JSONObject chromosome : chromosomes){
				JSONObject chrEq = buildChrEquivalence(chromosome);
				chrArray.put(chrEq);
			}
		} catch (JSONException e) {
			Application.error(e, session.getUserId());
			feedback.error("An error occurs : something went wrong with JBrowsoR (JSON)");
			return false;
		}
		String url = RepositoryAccess.getURLFastaFileByAssemblyId(assemblyId);
		if(null==url){
			Application.error("error in retriving fasta file from Generep", session.getUserId());
			feedback.error("An error occurs : something went wrong with JBrowsoR (url)");
			return false;
		}
		Application.debug("version : "+version+" taxId : "+taxId+" speciesName "+speciesName+" chrArray "+chrArray);
		if(null!=version && null!=taxId && null!= speciesName && null!=chrArray){
			//create genome on JBrowsoR
			int jbId = JbrowsoRAccess.createGenome(version, taxId, speciesName, chrArray.toString(), url);
			if(jbId!=-1){
				//check the status of the genome created
				boolean success = true;
				if(success){
					//create equivalence on gdv_prod
					SequenceDAO sdao = new SequenceDAO(Connect.getConnection(session));
					int genomeId = new Integer(assemblyId);
					boolean created = sdao.createGenome(genomeId,jbId,"generep");
					if(created){
						Application.debug("genome succefully created");
						return true;
					}
				}
			}
		}
		feedback.error("An error occurs : something went wrong with JBrowsoR");
		return false;

		
	}
	
	
	
	
	private JSONObject buildChrEquivalence(JSONObject chromosomeObject) {
		Object name;
		Object id;
		Object locus;
		Object version;
		JSONObject chr = new JSONObject(); 
		try {
			JSONObject chromosome = chromosomeObject.getJSONObject(ChromosomeAccess.CHROMOSOM_KEY);
			name = chromosome.get(ChromosomeAccess.CHR_NAME);
			id = chromosome.get(ChromosomeAccess.CHR_ID);
			locus = chromosome.get(ChromosomeAccess.CHR_LOCUS);
			version = chromosome.get(ChromosomeAccess.CHR_VERSION);
			chr.put(id+"_"+locus+"."+version, name);
		} catch (JSONException e) {
			Application.error(e, session.getUserId());
		}
		return chr;
	}


	public String[] getSpeciesNameAndAssemblyNameFromAssemblyId(int sequenceId) {
		Application.debug("getSpeciesNameAndAssemblyNameFromAssemblyId", session.getUserId());
		String[] tmp = new String[2];
		JSONObject assembly = AssembliesAccess.getAssemblyById(Integer.toString(sequenceId));
		try {
			String assemblyName = assembly.getString(AssembliesAccess.NAME_KEY);
			tmp[1] = assemblyName;
			String genome_id = assembly.getString(AssembliesAccess.GENOME_ID_KEY);
			JSONObject genome = GenomesAccess.getGenomesById(genome_id);
			String organism_id = genome.getString(GenomesAccess.ORGANISM_ID_KEY);
			JSONObject species = SpeciesAccess.getOrganismById(organism_id);
			String speciesName = species.getString(SpeciesAccess.NAME_KEY);
			tmp[0]=speciesName;
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Application.debug("species name : "+tmp[0]+"  assembly name : "+tmp[1],session.getUserId());
		return tmp;
	}

	public static int getJbrowsorIdFromSequenceId(String sequenceId) {
		SequenceDAO sdao = new SequenceDAO(Connect.getConnection());
		return sdao.getJBGenomeId(Integer.parseInt(sequenceId));
	}

	
}

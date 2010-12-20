package ch.epfl.bbcf.gdv.control.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.SequenceDAO;
import ch.epfl.bbcf.gdv.access.database.dao.SpeciesDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.database.pojo.Species;
import ch.epfl.bbcf.gdv.access.generep.AssembliesAccess;
import ch.epfl.bbcf.gdv.access.generep.ChromosomeAccess;
import ch.epfl.bbcf.gdv.access.generep.GenomesAccess;
import ch.epfl.bbcf.gdv.access.generep.RepositoryAccess;
import ch.epfl.bbcf.gdv.access.generep.SpeciesAccess;
import ch.epfl.bbcf.gdv.access.jbrowsor.JbrowsoRAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

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
		int jbid = gDAO.getJBGenomeIdFromGenrepId(Integer.valueOf(assemblyId));
		if(jbid!=-1){
			return JbrowsoRAccess.checkGenomeCreation(jbid);
		}
		return false;
	}
	
	/**
	 * get the equivalent jbrowsoR id from genrep Id
	 * @param sequenceId
	 * @return
	 */
	public static int getJbrowsorIdFromSequenceId(String sequenceId) {
		SequenceDAO sdao = new SequenceDAO(Connect.getConnection());
		return sdao.getJBGenomeIdFromGenrepId(Integer.parseInt(sequenceId));
	}

	/**
	 * get a sequence from it's id
	 * @param sequenceId
	 * @return
	 */
	public Sequence getSequenceFromId(int sequenceId) {
		SequenceDAO gDAO = new SequenceDAO(Connect.getConnection(session));
		return gDAO.getSequenceFromId(sequenceId);
	}

	/**
	 * method to create a new sequence from html
	 * @param assemblyId
	 * @param speciesId
	 * @param feedback
	 * @return
	 */
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
					//get the species in GDV or create it
					SpeciesDAO spdao = new SpeciesDAO(Connect.getConnection(session));
					int spId = -1;
					if(spdao.exist(speciesName)){
						spId = spdao.getSpeciesIdByName(speciesName);
					} else {
						spId = spdao.createSpecies(speciesName);
					}
					//create the sequence on GDV
					SequenceDAO sdao = new SequenceDAO(Connect.getConnection(session));
					int genomeId = new Integer(assemblyId);
					boolean created = sdao.createSequence(genomeId,jbId,"generep",version,spId);
					
					
					//boolean created = sdao.createGenome(genomeId,jbId,"generep");
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
	
	
	
	/**
	 * needed for jbrowsoR
	 * @param chromosomeObject
	 * @return
	 */
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
//
//
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
//
//	
	public static List<SelectOption> getSpeciesSO(){
		SpeciesDAO spdao = new SpeciesDAO(Connect.getConnection());
		List<Species> species = spdao.getAllSpecies();
		List<SelectOption> tab = new ArrayList<SelectOption>();
		for(Species sp : species){
			tab.add(new SelectOption(Integer.toString(sp.getId()),sp.getName()));
		}
		return tab;
	}

	public static List<SelectOption> getSequencesFromSpeciesIdSO(String speciesId) {
		SequenceDAO sdao = new SequenceDAO(Connect.getConnection());
		List<Sequence> seqs = sdao.getSequencesFromSpeciesId(Integer.parseInt(speciesId));
		List<SelectOption> tab = new ArrayList<SelectOption>();
		for(Sequence seq : seqs){
			Application.debug("adding :"+seq.getId()+" "+seq.getName());
			tab.add(new SelectOption(Integer.toString(seq.getId()),seq.getName()));
		}
		return tab;
	}
	
}

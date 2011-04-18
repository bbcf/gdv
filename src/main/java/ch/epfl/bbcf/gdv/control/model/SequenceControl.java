package ch.epfl.bbcf.gdv.control.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Chromosome;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.NR_Assembly;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Organism;
import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.SequenceDAO;
import ch.epfl.bbcf.gdv.access.database.dao.SpeciesDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.database.pojo.Species;
import ch.epfl.bbcf.gdv.access.genrep.GenrepWrapper;
import ch.epfl.bbcf.gdv.access.jbrowsor.JbrowsoRAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.InputControl.InputType;
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
	public boolean isCreatedOnJBrowsoR(int assemblyId, String value) {
		SequenceDAO gDAO = new SequenceDAO(Connect.getConnection(session));
		int jbid = gDAO.getJBGenomeIdFromGenrepId(assemblyId);
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
	 * method to create a new sequence in GDV database :
	 * create the species if not exist
	 * create the sequence
	 * add an admin track for this sequence (GTF from Genrep)
	 * @param nr_assemblyId
	 * @param speciesId
	 * @param feedback
	 * @return
	 */
	public boolean createGenome(int nr_assemblyId, int speciesId, FeedbackPanel feedback) {
		Application.debug("create genome with assembly id = "+nr_assemblyId);
		NR_Assembly nr_assembly = GenrepWrapper.getNRAssemblyById(nr_assemblyId);
		//JSONObject assembly = AssembliesAccess.getNRAssemblyById(nr_assemblyId);
		if(null==nr_assembly){
			Application.error("error in retriving nr_assembly from JBrowsoR", session.getUserId());
			feedback.error("An error occurs : something went wrong with JBrowsoR (nr_assembly)");
			return false;
		}
		Application.debug("assembly :"+nr_assembly.toString());
		//		boolean bbcfValid = AssembliesAccess.isBBCFValid(assembly);
		//		if(!bbcfValid){
		//			feedback.error("the assembly is not BBCF valid, aborting creation");
		//			return false;
		//		}
		Organism organism = GenrepWrapper.getOrganismsById(speciesId);
		//JSONObject species = SpeciesAccess.getOrganismById(speciesId);
		if(null==organism){
			Application.error("error in retriving species from JBrowsoR", session.getUserId());
			feedback.error("An error occurs : something went wrong with JBrowsoR (species)");
			return false;
		}
		String version = null;
		int taxId = 0;
		String speciesName = null;
		JSONArray chrArray = new JSONArray();
		version =nr_assembly.getName();// assembly.getString(AssembliesAccess.NAME_KEY);
		taxId = organism.getTax_id();//species.getString(SpeciesAccess.TAXID_KEY);
		speciesName = organism.getSpecies();//species.getString(SpeciesAccess.NAME_KEY);
		List<Chromosome> chromosomes = GenrepWrapper.getChromosomesByNRassemblyId(nr_assemblyId);
		//List<JSONObject> chromosomes = AssembliesAccess.getChromosomesByNRAssemblyId(nr_assemblyId);
		for(Chromosome chromosome : chromosomes){
			JSONObject chrEq = buildChrEquivalence(chromosome);
			chrArray.put(chrEq);
		}
		String url = GenrepWrapper.getURLFastaFileByNRAssemblyId(nr_assemblyId);
		if(null==url){
			Application.error("error in retriving fasta file from Generep", session.getUserId());
			feedback.error("An error occurs : something went wrong with JBrowsoR (url)");
			return false;
		}
		Application.debug("version : "+version+" taxId : "+taxId+" speciesName "+speciesName+" chrArray "+chrArray+" url :"+url);
		if(null!=version && 0!=taxId && null!= speciesName && null!=chrArray){
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
					int seqId = sdao.createSequence(nr_assembly.getId(),jbId,"generep",version,spId);
					//create an admin track
					if(seqId!=-1){
						//TODO
						if(null!=nr_assembly.getGtf_file_ftp() || !nr_assembly.getGtf_file_ftp().equalsIgnoreCase("")){
							String gftUrl = GenrepWrapper.getGtfUrlByNrAssemBly(nr_assembly.getId());
							boolean result = InputControl.processInputs(session.getUser(),
									-1,gftUrl,null,seqId,spId,false,true,
									new ArrayList<Group>(),InputType.NEW_FILE,null,"Ensembl");
						} else {
							feedback.error("GTF doesn't exist for this assembly in Genrep. Add it manually");
						}
						return true;
					}
				}
			}
		}
		feedback.error("An error occurs : something went wrong with JBrowsoR : " +
				"parameters fetched for this species are : " +
				"version : "+version+" taxId : "+taxId+" speciesName "+speciesName+" chrArray "+chrArray+" url :"+url);
		return false;


	}



	/**
	 * needed for jbrowsoR
	 * @param chromosomeObject
	 * @return
	 */
	private JSONObject buildChrEquivalence(Chromosome chromosome) {
		JSONObject chr = new JSONObject(); 
		try {
			//JSONObject chromosome = chromosomeObject.getJSONObject(ChromosomeAccess.CHROMOSOM_KEY);
			//			name = chromosome.get(ChromosomeAccess.CHR_NAME);
			//			id = chromosome.get(ChromosomeAccess.CHR_ID);
			//			locus = chromosome.get(ChromosomeAccess.CHR_LOCUS);
			//			version = chromosome.get(ChromosomeAccess.CHR_VERSION);
			chr.put(chromosome.getId()+"_"+chromosome.getRefseq_locus()+"."+chromosome.getRefseq_version(), chromosome.getChr_name());
		} catch (JSONException e) {
			Application.error(e, session.getUserId());
		}
		return chr;
	}
	//
	//
//	public String[] getSpeciesNameAndAssemblyNameFromAssemblyId(int sequenceId) {
//		Application.debug("getSpeciesNameAndAssemblyNameFromAssemblyId", session.getUserId());
//		String[] tmp = new String[2];
//		JSONObject assembly = AssembliesAccess.getNRAssemblyById(sequenceId);
//		try {
//			String assemblyName = assembly.getString(AssembliesAccess.NAME_KEY);
//			tmp[1] = assemblyName;
//			String genome_id = assembly.getString(AssembliesAccess.GENOME_ID_KEY);
//			JSONObject genome = GenomesAccess.getGenomesById(genome_id);
//			int organism_id = genome.getInt(GenomesAccess.ORGANISM_ID_KEY);
//			JSONObject species = SpeciesAccess.getOrganismById(organism_id);
//			String speciesName = species.getString(SpeciesAccess.NAME_KEY);
//			tmp[0]=speciesName;
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return tmp;
//	}
//	//
	//	
	/**
	 * get a list of SelectOption representing all species in gdv
	 */
	public static List<SelectOption> getSpeciesSO(){
		SpeciesDAO spdao = new SpeciesDAO(Connect.getConnection());
		List<Species> species = spdao.getAllSpecies();
		List<SelectOption> tab = new ArrayList<SelectOption>();
		for(Species sp : species){
			tab.add(new SelectOption(sp.getId(),sp.getName()));
		}
		return tab;
	}

	/**
	 * get a list of SelectOption representing the differents 
	 * assemblies available for this species
	 * @param speciesId
	 * @return
	 */
	public static List<SelectOption> getSequencesFromSpeciesIdSO(int speciesId) {
		SequenceDAO sdao = new SequenceDAO(Connect.getConnection());
		List<Sequence> seqs = sdao.getSequencesFromSpeciesId(speciesId);
		List<SelectOption> tab = new ArrayList<SelectOption>();
		for(Sequence seq : seqs){
			tab.add(new SelectOption(seq.getId(),seq.getName()));
		}
		return tab;
	}

	/**
	 * get the assemblies list not created on GDV and JBrowsoR
	 * @return
	 */
	public List<SelectOption> getNRAssembliesNotCreated(int speciesId) {
		List<SelectOption> allAssemblies = Arrays.asList(GenrepWrapper.getNRAssembliesByOrganismIdSO(speciesId));
		List<SelectOption> nonAddedAssemblies = new ArrayList<SelectOption>();
		SequenceControl sc = new SequenceControl(session);
		for (SelectOption so : allAssemblies){
			if(!sc.isCreatedOnJBrowsoR(so.getKey(),so.getValue())){
				nonAddedAssemblies.add(so);
			}
		}
		return nonAddedAssemblies;
	}

}

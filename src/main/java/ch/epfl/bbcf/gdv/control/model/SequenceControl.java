package ch.epfl.bbcf.gdv.control.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Chromosome;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.NR_Assembly;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Organism;
import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.SequenceDAO;
import ch.epfl.bbcf.gdv.access.database.dao.SpeciesDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.database.pojo.Species;
import ch.epfl.bbcf.gdv.access.genrep.GenrepWrapper;
import ch.epfl.bbcf.gdv.access.jbrowsor.JbrowsoRAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;

public class SequenceControl extends Control{


	/**
	 * look if the sequence with the assemblyId is created on JBrowsoR
	 * @param assemblyId
	 * @param value
	 * @return
	 */
	public static boolean isCreatedOnJBrowsoR(int assemblyId, String value) {
		SequenceDAO gDAO = new SequenceDAO(Conn.get());
		int jbid = gDAO.getJBGenomeIdFromGenrepId(assemblyId);
		if(jbid!=-1){
			return JbrowsoRAccess.checkGenomeCreation(jbid);
		}
		return false;
	}
	public static Sequence getSequence(int id){
		SequenceDAO dao = new SequenceDAO(Conn.get());
		return dao.getSequenceFromId(id);
	}

	/**
	 * get the equivalent jbrowsoR id from genrep Id
	 * @param sequenceId
	 * @return
	 */
	public static int getJbrowsorIdFromSequenceId(String sequenceId) {
		SequenceDAO sdao = new SequenceDAO(Conn.get());
		return sdao.getJBGenomeIdFromGenrepId(Integer.parseInt(sequenceId));
	}

	/**
	 * get a sequence from it's id
	 * @param sequenceId
	 * @return
	 */
	public static Sequence getSequenceFromId(int sequenceId) {
		SequenceDAO gDAO = new SequenceDAO(Conn.get());
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
	public static boolean createGenome(int nr_assemblyId, int speciesId, FeedbackPanel feedback) {
		Application.debug("create genome with assembly id = "+nr_assemblyId);
		NR_Assembly nr_assembly = GenrepWrapper.getNRAssemblyById(nr_assemblyId);
		//JSONObject assembly = AssembliesAccess.getNRAssemblyById(nr_assemblyId);
		if(null==nr_assembly){
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
			feedback.error("An error occurs : something went wrong with JBrowsoR (url)");
			return false;
		}
		Application.debug("version : "+version+" taxId : "+taxId+" speciesName "+speciesName+" chrArray "+chrArray+" url :"+url);



		if(null==version || 0==taxId || null== speciesName || null==chrArray){
			feedback.error("An error occurs : something went wrong with JBrowsoR : " +
					"parameters fetched for this species are : " +
					"version : "+version+" taxId : "+taxId+" speciesName "+speciesName+" chrArray "+chrArray+" url :"+url);
			return false;

		}
		//create genome on JBrowsoR
		int jbId = JbrowsoRAccess.createGenome(version, taxId, speciesName, chrArray.toString(), url);
		if(jbId==-1){
			feedback.error("Something went wrong with JBrowsoR genome creation : jb_id = "+jbId);
			return false;
		}
		//get the species in GDV or create it
		SpeciesDAO spdao = new SpeciesDAO(Conn.get());
		int spId = -1;
		if(spdao.exist(speciesName)){
			spId = spdao.getSpeciesIdByName(speciesName);
		} else {
			spId = spdao.createSpecies(speciesName);
		}
		//create the sequence on GDV
		SequenceDAO sdao = new SequenceDAO(Conn.get());
		int seqId = sdao.createSequence(nr_assembly.getId(),jbId,"generep",version,spId);
		//create an admin track
		if(seqId==-1){
			feedback.error("Something went wront with GDV : sequence cannot be created : "+seqId);
			return false;
		}
		Application.debug("sequence id : "+seqId);
		Application.debug("nr_assembly "+nr_assembly.getGtf_file_ftp());
		if(null==nr_assembly.getGtf_file_ftp()){
			feedback.error("GTF doesn't exist for this assembly in Genrep. Add it manually");
			return true;
		}
		if(nr_assembly.getGtf_file_ftp().equalsIgnoreCase("")){
			feedback.error("GTF doesn't exist for this assembly in Genrep. Add it manually");
			return true;
		}
		String gftUrl = GenrepWrapper.getGtfUrlByNrAssemBly(nr_assembly.getId());
		URL u;
		try {
			u = new URL(gftUrl);
		} catch (MalformedURLException e) {
			feedback.error("GTF URL doesn't exist for this assembly in Genrep. Add it manually");
			return true;
		}
		
		return JobControl.newAdminTrack(seqId, u,null,null,"Genes");
	}


/**
 * needed for jbrowsoR
 * @param chromosomeObject
 * @return
 */
private static JSONObject buildChrEquivalence(Chromosome chromosome) {
	JSONObject chr = new JSONObject(); 
	try {
		//JSONObject chromosome = chromosomeObject.getJSONObject(ChromosomeAccess.CHROMOSOM_KEY);
		//			name = chromosome.get(ChromosomeAccess.CHR_NAME);
		//			id = chromosome.get(ChromosomeAccess.CHR_ID);
		//			locus = chromosome.get(ChromosomeAccess.CHR_LOCUS);
		//			version = chromosome.get(ChromosomeAccess.CHR_VERSION);
		chr.put(chromosome.getId()+"_"+chromosome.getRefseq_locus()+"."+chromosome.getRefseq_version(), chromosome.getName());
	} catch (JSONException e) {
		Application.error(e);
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
	SpeciesDAO spdao = new SpeciesDAO(Conn.get());
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
	SequenceDAO sdao = new SequenceDAO(Conn.get());
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
public static List<SelectOption> getNRAssembliesNotCreated(int speciesId) {
	List<SelectOption> allAssemblies = Arrays.asList(GenrepWrapper.getNRAssembliesByOrganismIdSO(speciesId));
	List<SelectOption> nonAddedAssemblies = new ArrayList<SelectOption>();
	SequenceControl sc = new SequenceControl();
	for (SelectOption so : allAssemblies){
		if(!sc.isCreatedOnJBrowsoR(so.getKey(),so.getValue())){
			nonAddedAssemblies.add(so);
		}
	}
	return nonAddedAssemblies;
}
/**
 * get all sequences use in GDV
 * @return
 */
public static List<Sequence> getAllSequences() {
	SequenceDAO dao = new SequenceDAO(Conn.get());
	return dao.getAllSequences();
}

}

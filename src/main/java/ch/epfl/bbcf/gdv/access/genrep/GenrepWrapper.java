package ch.epfl.bbcf.gdv.access.genrep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;



import ch.epfl.bbcf.bbcfutils.access.genrep.Constants;
import ch.epfl.bbcf.bbcfutils.access.genrep.Constants.FORMAT;
import ch.epfl.bbcf.bbcfutils.access.genrep.Constants.KEY;
import ch.epfl.bbcf.bbcfutils.access.genrep.Constants.METHOD;
import ch.epfl.bbcf.bbcfutils.access.genrep.GenRepAccess;
import ch.epfl.bbcf.bbcfutils.access.genrep.MethodNotFoundException;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Assembly;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Chromosome;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Genome;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.GenrepObject;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.NR_Assembly;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Organism;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.html.utility.SelectOption;


public class GenrepWrapper {

	/**
	 * Get all organisms from Genrep
	 * @return a List of Organism
	 */
	@SuppressWarnings("unchecked")
	public static List<Organism> getOrganisms(){
		try {
			return (List<Organism>) GenRepAccess.doQueryList(
					Constants.URL, METHOD.ALL, FORMAT.json,
					new TypeReference<List<GenrepObject>>() {},KEY.organisms,null);
		} catch (MethodNotFoundException e) {
			Application.error(e);
		} catch (IOException e) {
			Application.error(e);
		};
		return null;
	}

	/**
	 * Get all NR_Assemblies from Genrep
	 * @return a list of NR Assemblies
	 */
	@SuppressWarnings("unchecked")
	public static List<NR_Assembly>getNRAssemblies(){
		try {
			return  (List<NR_Assembly>) GenRepAccess.doQueryList(
					Constants.URL, METHOD.ALL, FORMAT.json, new TypeReference<List<GenrepObject>>() {},KEY.nr_assemblies,null);
		} catch (MethodNotFoundException e) {
			Application.error(e);
		} catch (IOException e) {
			Application.error(e);
		};
		return null;

	}

	/**
	 * Get all Genomes from Genrep
	 * @return return a list of Genomes
	 */
	@SuppressWarnings("unchecked")
	public static List<Genome>getGenomes(){
		try {
			return  (List<Genome>) GenRepAccess.doQueryList(
					Constants.URL, METHOD.ALL, FORMAT.json, new TypeReference<List<GenrepObject>>() {},KEY.genomes,null);
		} catch (MethodNotFoundException e) {
			Application.error(e);
		} catch (IOException e) {
			Application.error(e);
		};
		return null;

	}

	public static List<NR_Assembly> getNRAssembliesByOrganismId(int organismId){
		List<Genome> genomes = getGenomesByOrganismId(organismId);
		List<NR_Assembly> nrAssemblies = getNRAssemblies();
		List<NR_Assembly> result = new ArrayList<NR_Assembly>();
		for(Genome genome : genomes){
			for(NR_Assembly nr_assembly : nrAssemblies){
				if(nr_assembly.getGenome_id()==genome.getId()){
					result.add(nr_assembly);
				}
			}
		}
		return result;
	}

	/**
	 * Get from Genrep all Nr assemblies
	 * from an organism
	 * @param organismId the organism id
	 * @return a table of SelectOption
	 */
	public static SelectOption[] getNRAssembliesByOrganismIdSO(int organismId){
		List<NR_Assembly> result = getNRAssembliesByOrganismId(organismId);
		return transformNRAssembliesToSO(result);
	}

	/**
	 * Get from Genrep all genomes
	 * with a particular organism Id
	 * @param organismId the organism id
	 * @return a list of Genomes
	 */
	public static List<Genome> getGenomesByOrganismId(int organismId){
		List<Genome> result = new ArrayList<Genome>();
		List<Genome> genomes = getGenomes();
		for(Genome genome : genomes){
			if(genome.getOrganism_id()==organismId){
				result.add(genome);
			}
		}
		return result;
	}


	/**
	 * Get an NRAssembly from Genrep
	 * @param nrAssemblyId the nrAssemblyId
	 * @return
	 */
	public static NR_Assembly getNRAssemblyById(int nrAssemblyId){
		try {
			return (NR_Assembly) GenRepAccess.doQuery(
					Constants.URL, METHOD.SHOW, FORMAT.json, NR_Assembly.class,KEY.nr_assemblies, nrAssemblyId);
		} catch (IOException e) {
			Application.error(e);
		} catch (MethodNotFoundException e) {
			Application.error(e);
		} catch (ClassNotFoundException e) {
			Application.error(e);
		}
		return null;
	}

	/**
	 * Get an Organism from Genrep
	 * @param organismId the organism id
	 * @return an Organism
	 */
	public static Organism getOrganismsById(int organismId) {
		try {
			return (Organism) GenRepAccess.doQuery(
					Constants.URL, METHOD.SHOW, FORMAT.json, Organism.class,KEY.organisms, organismId);
		} catch (IOException e) {
			Application.error(e);
		} catch (MethodNotFoundException e) {
			Application.error(e);
		} catch (ClassNotFoundException e) {
			Application.error(e);
		}
		return null;
	}
	/**
	 * Get an Assembly from Genrep
	 * @param nrAssemblyId the nrAssemblyId
	 * @return an Assembly
	 */
	@SuppressWarnings("unchecked")
	public static Assembly getAssemblyByNRAssemblyId(int nrAssemblyId){
		List<Assembly> assemblies;
		try {
			assemblies = (List<Assembly>) GenRepAccess.doQueryList(
					Constants.URL, METHOD.ALL, FORMAT.json, 
					new TypeReference<List<GenrepObject>>() {},KEY.assemblies,null);
			for(Assembly assembly : assemblies){
				if(assembly.getNr_assembly_id()==nrAssemblyId){
					return (Assembly) GenRepAccess.doQuery(
							Constants.URL, METHOD.SHOW, FORMAT.json, Assembly.class,KEY.assemblies, assembly.getId());
				}
			}
		} catch (MethodNotFoundException e) {
			Application.error(e);
		} catch (IOException e) {
			Application.error(e);
		} catch (ClassNotFoundException e) {
			Application.error(e);
		}
		return null;
	}
	/**
	 * Get the url of a fasta file from Genrep
	 * @param nr_assemblyId the nr_assemblyId
	 * @return an URL
	 */
	public static String getURLFastaFileByNRAssemblyId(int nr_assemblyId) {
		NR_Assembly nrAssembly = getNRAssemblyById(nr_assemblyId);
		String md5 = nrAssembly.getMd5();
		String result = Constants.URL+"/data/nr_assemblies/fasta/"+md5+".tar.gz";
		return result;
	}
	
	/**
	 * Get chromosomes from Genrep with from a
	 * nr_assembly
	 * @param nrAssemblyId the nrAssembly id
	 * @return a List of Chromosomes
	 */
	public static List<Chromosome> getChromosomesByNRassemblyId(int nrAssemblyId){
		Assembly assembly = getAssemblyByNRAssemblyId(nrAssemblyId);
		return assembly.getChromosomes();
	}


	/**
	 * Get the Url where to fetch the GTF file
	 * from an nr assembly
	 * @param nrAssemblyId the identifier
	 * @return the URL
	 */
	public static String getGtfUrlByNrAssemBly(int nrAssemblyId) {
		return Constants.URL+"/"+KEY.nr_assemblies+"/"+nrAssemblyId+"."+FORMAT.gtf;
	}


	
	
	
	
	



	/**
	 * Get all Organisms from Genrep
	 * in a SelectOption table
	 * @return a table of SelectOption
	 */
	public static SelectOption[] getOrganismsSO() {
		List<Organism> organisms = getOrganisms();
		return transformOrganismsToSO(organisms);
	}
	/**
	 * Transform a list of Organisms from Genrep 
	 * in a SelecOption table (id/speciesName)
	 * @param the list to transform
	 * @return a table of SelectOption
	 */
	private static SelectOption[] transformOrganismsToSO(List<Organism> organisms){
		SelectOption[] result = new SelectOption[organisms.size()];
		for(int i=0;i<organisms.size();i++){
			Organism current = organisms.get(i);
			result[i]= new SelectOption(current.getId(), current.getSpecies());
		}
		return result;

	}

	/**
	 * Transform a list of NRAssemblies from Genrep 
	 * in a SelecOption table (id/name)
	 * @param the list to transform
	 * @return a table of SelectOption
	 */
	private static SelectOption[] transformNRAssembliesToSO(List<NR_Assembly> assemblies){
		SelectOption[] result = new SelectOption[assemblies.size()];
		for(int i=0;i<assemblies.size();i++){
			NR_Assembly current = assemblies.get(i);
			result[i]= new SelectOption(current.getId(), current.getName());
		}
		return result;

	}

	
	









}

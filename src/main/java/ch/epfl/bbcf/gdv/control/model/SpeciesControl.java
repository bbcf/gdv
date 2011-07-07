package ch.epfl.bbcf.gdv.control.model;

import org.apache.wicket.Session;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.SpeciesDAO;
import ch.epfl.bbcf.gdv.config.UserSession;

public class SpeciesControl extends Control{


	public static String getSpeciesNameBySpeciesId(int speciesId) {
		SpeciesDAO spDAO = new SpeciesDAO();
		String s = spDAO.getSpeciesNameById(speciesId);
		spDAO.release();
		return s;
	}

	/**
	 * take the nrAssemblyId from genrep with the species used
	 * WARNING : it can hapend that a species has two assembly id, so two
	 * nrAssemblyId, but as the chromosome list should be
	 * the same for one entire species and all it's assemblies, we just take the
	 * first nrAssemblyId for the result
	 * @param speciesId
	 * @return
	 */
	public static int getNrAssemblyBySpeciesIdForBuildingChrList(int speciesId) {
		SpeciesDAO spDAO = new SpeciesDAO();
		int i = spDAO.getNrAssemblyBySpeciesIdForBuildingChrList(speciesId);
		spDAO.release();
		return i;
	}

}

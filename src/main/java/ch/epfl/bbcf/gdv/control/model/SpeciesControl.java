package ch.epfl.bbcf.gdv.control.model;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.SpeciesDAO;
import ch.epfl.bbcf.gdv.config.UserSession;

public class SpeciesControl extends Control{

	public SpeciesControl(UserSession session) {
		super(session);
	}

	public static String getSpeciesNameBySpeciesId(String speciesId) {
		SpeciesDAO spDAO = new SpeciesDAO(Connect.getConnection());
		return spDAO.getSpeciesNameById(speciesId);
	}

}

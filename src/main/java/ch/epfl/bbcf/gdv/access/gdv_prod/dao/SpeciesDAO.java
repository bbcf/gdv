package ch.epfl.bbcf.gdv.access.gdv_prod.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Species;
import ch.epfl.bbcf.gdv.config.Application;

public class SpeciesDAO extends DAO<Species>{

	private final static String tableName = "species";
	private final static String[] fields = {"taxId","speciesCommonNames"};
	
	public SpeciesDAO(Connect connection) {
		super(connection);
	}

	
	private Species getSpecies(ResultSet resultSet) {
		Species species = new Species();
		List<String> names = new ArrayList<String>();
		try {
			while (resultSet.next()) {
				try {
					names.add(resultSet.getString(fields[1]));
				} catch (SQLException e) {
					logger.error(e);
				}
				try {
					species.setTaxId(resultSet.getString(fields[0]));
				} catch (SQLException e) {
					logger.error(e);
				}
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		species.setNames(names);
		return species;
	}
}

package ch.epfl.bbcf.gdv.access.gdv_prod.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.ChrNameAssociation;
import ch.epfl.bbcf.gdv.config.Application;

public class ChromosomesNamesAssociationDAO extends DAO<ChrNameAssociation>{

	public ChromosomesNamesAssociationDAO(Connect connection) {
		super(connection);
	}

	private ChrNameAssociation getChrNameAssociation(ResultSet resultSet) {
		ChrNameAssociation nameAssociation = new ChrNameAssociation();
		if(this.databaseConnected()){
			try {
				nameAssociation.setOne(resultSet.getString("one"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				nameAssociation.setTwo(resultSet.getString("two"));
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return nameAssociation;
	}

	private List<ChrNameAssociation> getChrNameAssociations(ResultSet resultSet) {
		List<ChrNameAssociation> nameAssociations = new ArrayList<ChrNameAssociation>();
		try {
			while (resultSet.next()) {
				nameAssociations.add(getChrNameAssociation(resultSet));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return nameAssociations;
	}

	public List<String> getAssociations(String chr){
		List<String> assocs = new ArrayList<String>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "(select t1.two from chrnamesassociation as t1 " +
				"where one = ? ) " +
				"union " +
				"(select t1.one from chrnamesassociation as t1 " +
				"where two = ? ); ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, chr);
				statement.setString(2, chr);
				ResultSet resultSet = this.executeQuery(statement);
				try {
					while (resultSet.next()) {
						assocs.add(resultSet.getString(1));
					}
				} catch (SQLException e) {
					logger.error(e);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return assocs;
	}

}


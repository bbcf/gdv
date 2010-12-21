package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.database.pojo.Species;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;

public class SpeciesDAO extends DAO<Species>{

	private final static String tableName = "species";
	private final static String[] fields = {"id","name"};

	public SpeciesDAO(Connect connection) {
		super(connection);
	}





	public int createSpecies(String speciesName) {
		int speciesId = -1;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into species values (default , " +
				" ?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, speciesName);
				this.executeUpdate(statement);
				query = "select currval('species_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                              
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					speciesId = resultSet.getInt(1);
				}
				this.endQuery(true);
				return speciesId;
			} catch (SQLException e) {
				logger.error("createSpecies "+e);
			}
		}
		this.endQuery(false);
		return speciesId;

	}


	public boolean exist(String name) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select 1 from species where name = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,name);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					return true;
				}
				this.endQuery(true);
				return false;
			} catch (SQLException e) {
				logger.error("exist : "+e);
				this.endQuery(false);
			}
		}
		return false;
	}



	/**
	 * get a species name from it's id
	 * @param speciesId
	 * @return
	 */
	public String getSpeciesNameById(String speciesId) {
		String result=null;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select name from species where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,speciesId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					result = resultSet.getString("0");
				}
			} catch (SQLException e) {
				logger.error("getSpeciesNameById : "+e);
				this.endQuery(false);
			}
		}
		return result;
	}
	/**
	 * get a species id from it's name
	 * @param speciesName
	 * @return
	 */
	public int getSpeciesIdByName(String speciesName) {
		int result=-1;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select id from species where name = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,speciesName);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					this.endQuery(true);
					result = resultSet.getInt(1);
				}
			} catch (SQLException e) {
				logger.error("getSpeciesIdByName : "+e);
				this.endQuery(false);
			}
		}
		return result;
	}

	private List<Species> getSpeciess(ResultSet resultSet) {
		List<Species> species = new ArrayList<Species>();
		try {
			while(resultSet.next()){
				species.add(getSpecies(resultSet));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return species;
	}
	private Species getSpecies(ResultSet resultSet) {
		Species species = new Species();
		if(this.databaseConnected()){
			try {
				species.setId(resultSet.getInt(fields[0]));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				species.setName(resultSet.getString(fields[1]));
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return species;
	}

	/**
	 * get a list of all species in GDV
	 * @return
	 */
	public List<Species> getAllSpecies() {
		List<Species> species=new ArrayList<Species>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from species;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = this.executeQuery(statement);
				species = getSpeciess(resultSet);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("all species : "+e);
				this.endQuery(false);
			}
		}
		return species;
	}




	/**
	 * get the species belonging
	 * to this project projectId
	 * @param id
	 * @return
	 */
	public Species getSpeciesFromProjectId(int projectId) {
		Species species = null;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from species as t1 " +
				"inner join sequences as t2 on t1.id = t2.species_id " +
				"inner join projects as t3 on t2.id = t3.cur_seq_id " +
				"where t3.id = ? limit 1;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, projectId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					species = getSpecies(resultSet);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error("getSpeciesFromProjectId : "+e);
				this.endQuery(false);
			}
		}
		return species;
	}









}





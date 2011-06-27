package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.config.Application;

public class SequenceDAO extends DAO<Sequence>{

	private final static String tableName = "sequences";
	private final static String[] fields = {"id","jbrowse_id","type","name","species_id"};

	public SequenceDAO(Connection connection) {
		super(connection);
	}

	private ArrayList<Sequence> getSequences(final ResultSet resultSet){
		ArrayList<Sequence> genomes = new ArrayList<Sequence>();
		try {
			while (resultSet.next()) {
				genomes.add(getSequence(resultSet));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return genomes;
	}

	private Sequence getSequence(ResultSet resultSet) {
		Sequence genome = new Sequence();
		try {
			genome.setId(resultSet.getInt(fields[0]));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			genome.setJbrowsoRId(resultSet.getInt(fields[1]));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			genome.setType(resultSet.getString(fields[2]));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			genome.setName(resultSet.getString(fields[3]));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			genome.setSpeciesId(resultSet.getInt(fields[4]));
		} catch (SQLException e) {
			logger.error(e);
		}
		return genome;
	}

	public int getJBGenomeIdFromGenrepId(Integer assemblyId) {
		Sequence genome = new Sequence();
		genome.setJbrowsoRId(-1);
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from "+tableName+" as t1 " +
				"where t1.id = ? and t1.type='generep';";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, assemblyId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.next()){
					genome = getSequence(resultSet);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return genome.getJbrowsoRId();
	}
	public Sequence getSequenceFromId(int sequenceId) {
		Sequence genome = new Sequence();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from "+tableName+" as t1 " +
				"where t1.id = ?;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, sequenceId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.next()){
					genome = getSequence(resultSet);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return genome;
	}
	/**
	 *  create a new sequence on GDV database
	 * @param genomeId - the id choosen (normally from genrep)
	 * @param jbId - the id of equivalence on jbrowsoR
	 * @param type - the type (e.g genrep, other)
	 * @param version - the name of the version
	 * @param spId - the species id
	 * @return
	 */
	public int createSequence(int genomeId, int jbId, String type,
			String version, int spId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into "+tableName+" values (" +
				" ?, ?, ? ,? ,? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, genomeId);
				statement.setInt(2,jbId);
				statement.setString(3, type);
				statement.setString(4, version);
				statement.setInt(5, spId);
				this.executeUpdate(statement);
				return genomeId;
			} catch (SQLException e) {
				logger.error(e);
			}
			this.endQuery(false);
		}

		return -1;
	}

	/**
	 * return all sequence belonging to 
	 * the same species id
	 * @param speciesId
	 * @return
	 */
	public List<Sequence> getSequencesFromSpeciesId(int speciesId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from sequences where species_id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, speciesId);
				ResultSet r = this.executeQuery(statement);
				List<Sequence> seqs = getSequences(r);
				this.endQuery(true);
				return seqs;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}

	public List<Sequence> getAllSequences() {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from sequences;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet r = this.executeQuery(statement);
				List<Sequence> seqs = getSequences(r);
				this.endQuery(true);
				return seqs;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}

}
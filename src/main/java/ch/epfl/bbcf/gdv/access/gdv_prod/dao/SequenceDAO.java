package ch.epfl.bbcf.gdv.access.gdv_prod.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Sequence;
import ch.epfl.bbcf.gdv.config.Application;

public class SequenceDAO extends DAO<Sequence>{

	private final static String tableName = "sequences";
	private final static String[] fields = {"id","jbrowse_id","type"};

	public SequenceDAO(Connect connection) {
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
		return genome;
	}

	public boolean createGenome(int genomeId, int jbId,String type) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into "+tableName+" values (" +
				" ?, ?, ?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, genomeId);
				statement.setInt(2,jbId);
				statement.setString(3, type);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		this.endQuery(false);
		return false;
	}



	public int getSequenceIdFromGenrepAssemblyId(Integer assemblyId) {
		Sequence genome = new Sequence();
		genome.setJbrowsoRId(-1);
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from "+tableName+" as t1 " +
				"where t1.id = ? and t1.type = 'generep' ;";
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
		return genome.getId();
	}

	public boolean isCreatedOnJBrowsoR(Integer assemblyId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select 1 from genomes where id = ? limit 1;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, assemblyId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					return true;
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return false;
	}

	public int getJBGenomeId(Integer assemblyId) {
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
				"where t1.id = ? and t1.type='generep';";
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
	public int getSequenceIDFromTrackId(int trackId) {
		int seqId=-1;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.id from sequences as t1 " +
						"inner join sequencetouserinput as t2 on t2.seq_id = t1.id " +
						"inner join annotationtouserinput as t3 on t3.userinput_id = t2.userinput_id " +
						"inner join viewtracktoannotation as t4 on t4.annotation_id = t3.annotation_id " +
						"where t4.viewtrack_id = ? " +
						"limit 1 ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, trackId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.next()){
					seqId = resultSet.getInt(1);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return seqId;
	}

	public int getSequenceIDAutoAnnotation(int trackId) {
		int seqId=-1;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.seq_id from auto_annotations as t1 " +
						"inner join viewtracktoannotation as t2 on t1.annotation_id = t2.annotation_id " +
						"where t2.viewtrack_id = ? " +
						"limit 1 ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, trackId);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.next()){
					seqId = resultSet.getInt(1);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return seqId;
	}

	
}
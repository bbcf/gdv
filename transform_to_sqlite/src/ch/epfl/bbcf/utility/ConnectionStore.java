package ch.epfl.bbcf.utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.daemon.Launcher;

public class ConnectionStore {

	private Map<String,Integer> indexes;
	private List<PreparedStatement> statements;
	private List<Integer> queries;
	protected static Logger logger = Launcher.initLogger(ConnectionStore.class.getName());
	
	public ConnectionStore() {
		indexes = new HashMap<String, Integer>();
		statements = new ArrayList<PreparedStatement>();
		queries = new ArrayList<Integer>();
	}

	public void addChromosome(String chromosome,
			PreparedStatement prep) {
		statements.add(prep);
		queries.add(0);
		indexes.put(chromosome,queries.size()-1);
	}
	
	
	public PreparedStatement getPreparedStatement(String chromosome){
//		logger.debug(chromosome);
//		logger.debug(statements);
//		logger.debug(indexes);
		return statements.get(indexes.get(chromosome));
	}
	public int getNbQueries(String chromosome){
		return queries.get(indexes.get(chromosome));
	}

	public void setNbQueries(String chromosome, int i) {
		queries.set(indexes.get(chromosome), i);
	}
	
}

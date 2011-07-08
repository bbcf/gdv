package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.pojo.Style;
import ch.epfl.bbcf.gdv.access.database.pojo.Type;

public class TypeDAO extends DAO<Type>{

	public TypeDAO() {
		super();
	}

	private Type getType(ResultSet r){
		Type ty = new Type();
		if(this.databaseConnected()){
			try {
				ty.setId(r.getInt("id"));
			} catch (SQLException e) {
				logger.error(e);
			}
			try {
				ty.setName(r.getString("name"));
			} catch (SQLException e) {
				logger.error(e);
			}
			return ty;
		}
		return null;
	}

	private List<Type> getTypess(ResultSet r){
		List<Type> types = new ArrayList<Type>();
		try {
			while(r.next()){
				types.add(getType(r));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return types;
	}

	public Type getType(String type){
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from types " +
				"where name = ? limit 1;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, type);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.next()){
					Type t = getType(resultSet);
					this.endQuery(true);
					return t;
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}


	public Type createType(String type){
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into types values (default, ? ) ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, type);
				this.executer(statement);
				query = "select currval('types_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                              
				ResultSet resultSet = this.executeQuery(statement);
				int typeId = -1;
				if(resultSet.first()){
					typeId = resultSet.getInt(1);
				}
				this.endQuery(true);
				return new Type(typeId,type);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}
	public Type getTypeById(int type_id) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from style " +
				"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, type_id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.next()){
					Type s =  getType(resultSet);
					this.endQuery(true);
					return s;
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}


	public List<Type> getTrackTypes(int trackId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from types as t1 " +
				"inner join trackToType as t2 on t1.id = t2.type_id " +
				"where t2.track_id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, trackId);
				ResultSet resultSet = this.executeQuery(statement);
				List<Type> types = getTypess(resultSet);
				this.endQuery(true);
				return types;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}

	public boolean setTrackTypes(int trackId, List<Type> types) {
		if(this.databaseConnected()){
			this.startQuery();
			try {

				String query = "insert into trackToType values ( ? , ? ) ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				for(Type t : types){
					statement.setInt(1,trackId);
					statement.setInt(2,t.getId());
					this.executer(statement);
				}
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return false;
	}



}

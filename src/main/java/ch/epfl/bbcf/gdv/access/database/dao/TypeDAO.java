package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.bbcf.gdv.access.database.pojo.Type;

public class TypeDAO extends DAO<Type>{

	public TypeDAO(Connection connection) {
		super(connection);
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
	
}

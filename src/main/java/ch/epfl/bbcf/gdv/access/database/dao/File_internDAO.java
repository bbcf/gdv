package ch.epfl.bbcf.gdv.access.database.dao;
//package ch.epfl.bbcf.gdv.access.gdv_prod.dao;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Date;
//
//import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
//import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.UserInput;
//import ch.epfl.bbcf.gdv.config.Application;
//
//public class File_internDAO extends DAO<UserInput>{
//
//	public File_internDAO(Connect connection) {
//		super(connection);
//	}
//
//	private UserInput getFile(final ResultSet resultSet) {
//		UserInput f = new UserInput();
//		try {
//			f.setName(resultSet.getString("name"));
//		} catch (SQLException e) {
//			logger.error(e);
//		}
//		try {
//			f.setType(resultSet.getString("type"));
//		} catch (SQLException e) {
//			logger.error(e);
//		}
//		return f;
//	}
//
//	public int create(String url, boolean isLocal, String filetype, String datatype) {
//		int repId = -1;
//		if(this.databaseConnected()){
//			this.startQuery();
//			try {
//				String query = "insert into repository values (" +
//				"default, ? , ? , ? , ? ) ; ";
//				PreparedStatement statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				statement.setString(1,url);
//				statement.setBoolean(2, isLocal);
//				statement.setString(3, filetype);
//				statement.setString(4, datatype);
//				this.execute(statement);
//				//get the default value
//				query = "select currval('repository_repositoryid_seq') ; ";
//				statement = this.prepareStatement(query,
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	                               
//				ResultSet resultSet = this.executeQuery(statement);
//				if(resultSet.first()){
//					repId = resultSet.getInt(1);
//				}
//				this.endQuery(true);
//				return repId;
//			} catch (SQLException e) {
//				logger.error(e);
//			}
//		}
//		return repId;
//	}
//}

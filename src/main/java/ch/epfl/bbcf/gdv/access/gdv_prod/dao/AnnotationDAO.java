package ch.epfl.bbcf.gdv.access.gdv_prod.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Annotation;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.UserInput;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.html.utility.AnnotationWrapper;

public class AnnotationDAO extends DAO<Annotation>{

	private static final String tableName ="annotations";
	private static final String[] fields = {
		"id","name"
	};

	public AnnotationDAO(Connect connection) {
		super(connection);
	}


	private Annotation getAnnotation(ResultSet resultSet) {
		Annotation annotation = new Annotation();
		try {
			annotation.setId(resultSet.getInt(fields[0]));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			annotation.setName(resultSet.getString(fields[1]));
		} catch (SQLException e) {
			logger.error(e);
		}
		return annotation;
	}

	private List<Annotation> getAnnotations(ResultSet resultSet) {
		List<Annotation> annotations = new ArrayList<Annotation>();
		try {
			while (resultSet.next()) {
				annotations.add(getAnnotation(resultSet));
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return annotations;
	}

	private Annotation getAnnotationById(int annotationId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select * from "+tableName+" where id  = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,annotationId);
				ResultSet resultSet = this.executeQuery(statement);
				if (resultSet.first()) {
					this.endQuery(true);
					return getAnnotation(resultSet);
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}

	private List<AnnotationWrapper> getAnnotationWrappers(ResultSet resultSet) {
		List<AnnotationWrapper> annotations = new ArrayList<AnnotationWrapper>();
		try {
			while (resultSet.next()) {
				AnnotationWrapper w = new AnnotationWrapper();
				try {
					w.setId(resultSet.getInt("id"));
				} catch (SQLException e) {
					logger.error(e);
				}
				try {
					w.setName(resultSet.getString("name"));
				} catch (SQLException e) {
					logger.error(e);
				}
				try {
					w.setFileName(resultSet.getString("file"));
				} catch (SQLException e) {
					logger.error(e);
				}
				try {
					w.setSequenceId(resultSet.getInt("seq_id"));
				} catch (SQLException e) {
					logger.error(e);
				}
				annotations.add(w);
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return annotations;
	}
	public boolean exist(Users user, int assemblyId,String annotationName) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select 1 from "+tableName+" as t1 " +
				"inner join userToAnnotation as t2 on t2.annotationId = t1.annotationId " +
				"inner join genomeToAnnotation as t3 on t3.annotationId = t1.annotationId " +
				"where "+fields[1]+" = ? and t2.userId = ? and t3.genomeId = ? " +
				"limit 1 ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,annotationName);
				statement.setInt(2,user.getId());
				statement.setInt(3,assemblyId);
				ResultSet resultSet = this.executeQuery(statement);
				if (resultSet.first()) {
					this.endQuery(true);
					return true;
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return false;
	}


	public int createAnnotation(String annotationName, int repositoryId, boolean isPublic,
			boolean isLocal, Date date) {
		int annotId = -1;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into annotations values (" +
				"default, ? , ? , ? , ? , ?) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,annotationName);
				statement.setInt(2, repositoryId);
				statement.setBoolean(3, isPublic);
				statement.setBoolean(4, isLocal);
				statement.setDate(5, date);
				this.executeUpdate(statement);
				//get the default value
				query = "select currval('annotations_annotationid_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	   
				ResultSet resultSet = this.executeQuery(statement);
						if(resultSet.first()){
							annotId = resultSet.getInt(1);
						}
						this.endQuery(true);
						return annotId;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return annotId;
	}


	public boolean linkToUser(int userId, int annotIdParent) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into userToAnnotation values (" +
				" ?, ? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, userId);
				statement.setInt(2,annotIdParent);
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

	public boolean linkToGenome(int assemblyId, int annotId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into genomeToAnnotation values (" +
				" ?, ? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, assemblyId);
				statement.setInt(2,annotId);
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

	public List<Annotation> getAnnotationsFromUser(int userId) {
		List<Annotation> annots = new ArrayList<Annotation>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from "+tableName+" as t1 " +
				"inner join userToAnnotation as t2 on t2.annotationId = t1.annotationId " +
				"where t2.userId = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, userId);
				ResultSet resultSet = this.executeQuery(statement);
				annots = getAnnotations(resultSet);
				this.endQuery(true);
				return annots;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}

	public List<AnnotationWrapper> getAnnotationsWrapperFromUser(int userId) {
		List<AnnotationWrapper> annots = new ArrayList<AnnotationWrapper>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.*, t4.file from annotations as t1 " +
				"inner join annotationtouserinput as t2 on t2.annotation_id = t1.id " +
				"inner join usertoinput as t3 on t3.input_id = t2.userinput_id " +
				"inner join userinput as t4 on t3.input_id = t4.id " +
				"where t3.user_id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, userId);
				ResultSet resultSet = this.executeQuery(statement);
				annots = getAnnotationWrappers(resultSet);
				this.endQuery(true);
				return annots;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}
	public List<AnnotationWrapper> getDefaultAnnotationsWrapper() {
		List<AnnotationWrapper> annots = new ArrayList<AnnotationWrapper>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* , t2.seq_id from annotations as t1 " +
				"inner join auto_annotations as t2 on t2.annotation_id = t1.id ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = this.executeQuery(statement);
				annots = getAnnotationWrappers(resultSet);
				this.endQuery(true);
				return annots;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return annots;
	}



	public List<Annotation> getAnnotationsFromUserIdAndAssemblyId(int userId,
			Integer assemblyId) {
		List<Annotation> annots = new ArrayList<Annotation>();
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select t1.* from "+tableName+" as t1 " +
				"inner join userToAnnotation as t2 on t2.annotationId = t1.annotationId " +
				"inner join genometoannotation as t3 on t3.annotationId = t1.annotationId " +
				"where t2.userId = ? and t3.genomeId = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, userId);
				statement.setInt(2, assemblyId);
				ResultSet resultSet = this.executeQuery(statement);
				annots = getAnnotations(resultSet);
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return annots;
	}





	public int createAnnotation(String fileName) {
		int annotId = -1;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into annotations values ( default, " +
				" ? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,fileName);
				this.executeUpdate(statement);
				//get the default value
				query = "select currval('annotations_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); 	             
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					annotId = resultSet.getInt(1);
				}
				this.endQuery(true);
				return annotId;
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return annotId;
	}

	public boolean linkToAutoAnnotation(int annotId, int seqId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into auto_annotations values (" +
				" ?, ? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1,seqId);
				statement.setInt(2, annotId);
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

	public boolean linkToUserInput(int annotId, int inputId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into annotationToUserInput values (" +
				" ?, ? ) ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, annotId);
				statement.setInt(2,inputId);
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


	public boolean deleteAllreference(int annotationId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				//viewtrack to annotations
				String query = "delete from annotations "+
				" where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, annotationId);
				this.executeUpdate(statement);
				this.endQuery(true);
				return true;
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			} 
		}
		return false;
	}


	public String getDefaultAnnotationNameById(int annotationId) {
		return getAnnotationById(annotationId).getName();
	}


	public int getDefaultAnnotationIdByFileName(String name) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "select id from annotations as t1 " +
						"inner join auto_annotations as t2 on t1.id = t2.annotation_id " +
						"where name = ? ; ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1,name);
				ResultSet resultSet = this.executeQuery(statement);
				if (resultSet.first()) {
					this.endQuery(true);
					return getAnnotation(resultSet).getId();
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return -1;
	}









}
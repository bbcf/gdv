package ch.epfl.bbcf.gdv.access.gdv_prod.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.Roles;

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Track;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;

/**
 * DAO for the User in the database
 * @author Yohan Jarosz
 *
 */
public class UsersDAO extends DAO<Users>{

	public UsersDAO(Connect connection) {
		super(connection);
	}

	/**
	 * Create a new user in the database
	 * @param user the user
	 * @return created
	 */
	public int createUser(Users user) {
		int userId = -1;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "insert into users values (" +
				"default, ? , ? , ? , ? , ? , ? ,? );";

				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, user.getMail());
				statement.setString(2, user.getName());
				statement.setString(3, user.getFirstname());
				statement.setString(4, user.getTitle());
				statement.setString(5, user.getPhone());
				statement.setString(6, user.getOffice());
				statement.setString(7, user.getType());
				this.executeUpdate(statement);
				query = "select currval('users_id_seq') ; ";
				statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					userId = resultSet.getInt(1);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return userId;
	}

	//	public boolean createGenome(Users user, Genome genome){
	//		if(this.databaseConnected()){
	//			this.startQuery();
	//			try {
	//				String query = "insert into userToGenomes values (" +
	//				" ? , ? ); ";
	//
	//				PreparedStatement statement = this.prepareStatement(query,
	//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	//				statement.setInt(1, user.getId());
	//				statement.setInt(2, genome.getGenomeId());
	//				this.execute(statement);
	//				this.endQuery(true);
	//				return true;
	//			} catch (SQLException e) {
	//				logger.error(e);
	//				this.endQuery(false);
	//			}
	//		}
	//		return false;
	//	}
	//	/**
	//	 * Put in relation the track and the user
	//	 * @param track
	//	 * @return
	//	 */
	//	public boolean createTrack(Users user,JBTrack track) {
	//		if(this.databaseConnected()){
	//			this.startQuery();
	//			try {
	//				String query = "insert into userToTracks values (" +
	//				" ? , ? ); ";
	//
	//				PreparedStatement statement = this.prepareStatement(query,
	//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	//				statement.setInt(1, user.getId());
	//				statement.setInt(2, track.getId());
	//				this.execute(statement);
	//				this.endQuery(true);
	//				return true;
	//			} catch (SQLException e) {
	//				logger.error(e);
	//				this.endQuery(false);
	//			}
	//		}
	//		return false;
	//	}

	//	/**
	//	 * reference the track created to the user
	//	 * @param userId
	//	 * @param annotId
	//	 * @return
	//	 */
	//	public boolean createTrack(int userId, int annotId) {
	//		if(this.databaseConnected()){
	//			this.startQuery();
	//			try {
	//				String query = "insert into usertoannotations values (" +
	//				" ? , ? ); ";
	//
	//				PreparedStatement statement = this.prepareStatement(query,
	//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	//				statement.setInt(1, annotId);
	//				statement.setInt(2, userId);
	//				this.execute(statement);
	//				this.endQuery(true);
	//				return true;
	//			} catch (SQLException e) {
	//				logger.error(e);
	//				this.endQuery(false);
	//			}
	//		}
	//		return false;
	//	}

	/**
	 * find if the username exist or not
	 * @param username the username
	 * @return exist
	 */
	public boolean usernameExists(String username) {
		boolean result = false;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "Select 1 from users as t1 " +
				"where t1.username = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, username);
				ResultSet resultSet = this.executeQuery(statement);
				if (resultSet.first()) {
					result = true;
					this.endQuery(true);
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return result;
	}

	public Users getUserByEmail(String email) {
		Users user = null;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "Select t1.* from users as t1 " +
				"where t1.mail = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, email);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					user = this.getUser(resultSet);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return user;
	}
	/**
	 * Find an user by his id
	 * @param id the id
	 * @return the user
	 */
	public Users getUserById(int id) {
		Users user = null;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "Select t1.* from users as t1 " +
				"where t1.id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, id);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					user = this.getUser(resultSet);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return user;
	}

	//	public Users getUserFromTrackId(int trackId) {
	//		if(this.databaseConnected()){
	//			this.startQuery();
	//			try {
	//				String query = "Select t1.* from users as t1 " +
	//				"inner join userToInput as t2 on t1.id = t2.user_id " +
	//				"inner join annotationToUserInput as t3 on t2.input_id = t3.userInput_id " +
	//				"inner join viewTrackToAnnotation as t4 on t3.annotation_id = t4.annotation_id " +
	//				"where t4.viewtrack_id = ? limit 1;";
	//				PreparedStatement statement = this.prepareStatement(query,
	//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	//				statement.setInt(1, trackId);
	//				ResultSet resultSet = this.executeQuery(statement);
	//				Users user =  this.getUser(resultSet);
	//				this.endQuery(true);
	//				return user;
	//			} catch (SQLException e) {
	//				logger.error(e);
	//				this.endQuery(false);
	//			}
	//		}
	//		return null;
	//	}

	/** 
	 * find user by name
	 * @param username
	 * @return the user
	 */
	public Users getUserByName(String username) {
		Users user = null;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "Select t1.* from users as t1 " +
				"where t1.username = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, username);
				ResultSet resultSet = this.executeQuery(statement);
				if(resultSet.first()){
					user = this.getUser(resultSet);
				}
				this.endQuery(true);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return user;
	}

	public boolean changeUsername(Users user,String newUsername) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update users set username = ? " +
				"where id= ? ;";

				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				statement.setString(1, newUsername);
				statement.setInt(2, user.getId());
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


	public boolean changeMail(Users user, String mail) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update users set mail = ? " +
				"where id= ? ;";

				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				statement.setString(1, mail);
				statement.setInt(2, user.getId());
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
	public boolean changePassword(Users user, byte[] encrypt) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "update users set passwd = ? " +
				"where id= ? ;";

				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				statement.setBytes(1, encrypt);
				statement.setInt(2, user.getId());
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

	private List<Users> getUsers(ResultSet resultSet) {
		List<Users> users = new ArrayList<Users>();
		try {
			while(resultSet.next()){
				users.add(getUser(resultSet));
			}
			return users;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * the constructor of POJO
	 * @param result the result of the query
	 * @return the POJO
	 */
	private Users getUser(ResultSet result) {
		Users user = new Users();
		try {
			user.setId(result.getInt("id"));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			user.setName(result.getString("name"));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			user.setMail(result.getString("mail"));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			user.setFirstname(result.getString("firstname"));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			user.setTitle(result.getString("title"));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			user.setPhone(result.getString("phone"));
		} catch (SQLException e) {
			logger.error(e);
		}
		try {
			user.setOffice(result.getString("office"));
		} catch (SQLException e) {
			logger.error(e);
		}
		return user;
	}


	public boolean emailExist(String email) {
		boolean result = false;
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "Select 1 from users as t1 " +
				"where t1.mail = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, email);
				ResultSet resultSet = this.executeQuery(statement);
				if (resultSet.first()) {
					result = true;
					this.endQuery(true);
				}
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return result;
	}

	public boolean isAdmin(int user) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "Select 1 from admin " +
				"where id = ? ;";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, user);
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



	/**
	 * get the users from a trackId 
	 * @param trackId
	 * @return
	 */
	public List<Users> getUserFromTrackId(int trackId) {
		if(this.databaseConnected()){
			this.startQuery();
			try {
				String query = "Select t1.* from users as t1 " +
				"inner join usertotrack as t2 on t1.id = t2.user_id " +
				"where t2.track_id = ? ";
				PreparedStatement statement = this.prepareStatement(query,
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, trackId);
				ResultSet resultSet = this.executeQuery(statement);
				this.endQuery(true);
				return this.getUsers(resultSet);
			} catch (SQLException e) {
				logger.error(e);
				this.endQuery(false);
			}
		}
		return null;
	}

	public Users getUserByProjectId(int projectId) {
			Users user = null;
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "Select t1.* from users as t1 " +
							"inner join usertoproject as t2 on t1.id = t2.user_id " +
					"where t2.project_id = ? ;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					statement.setInt(1, projectId);
					ResultSet resultSet = this.executeQuery(statement);
					if(resultSet.first()){
						user = this.getUser(resultSet);
					}
					this.endQuery(true);
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return user;
		}
	}






	//	private Person[] getPersons(ResultSet resultSet) {
	//		Person[] persons = null;
	//		try {
	//			int size =0;  
	//			if (resultSet != null)  {  
	//				resultSet.beforeFirst();  
	//				resultSet.last();  
	//				size = resultSet.getRow(); 
	//				resultSet.beforeFirst(); 
	//				persons = new Person[size];
	//				int i = 0;
	//				while (resultSet.next()){
	//					persons[i] = this.getPerson(resultSet);
	//					i++;
	//				}
	//			}
	//		} catch (SQLException e) {
	//			logger.error(e);
	//		}
	//		return persons;
	//	}







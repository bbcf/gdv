package ch.epfl.bbcf.gdv.access.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;



import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

public class Conn {

	public static final String DRIVER = "org.postgresql.Driver";
	static final int MAX_POOL_SIZE = 50;
	static final int MAX_LOOP = 1000;
	private String user;
	private String passwd;
	private String base;


	private static Vector<Connection> connectionPool = new Vector<Connection>();

	private static Conn instance;

	/**
	 * create if not a singleton of a Conn object
	 * @return a connection
	 */
	public static Connection get(){
		if(null==instance){
			synchronized(Conn.class){
				instance = new Conn();
			}
		}
		return instance.getFromPool();
	}



	/**
	 * init the Conn Object
	 * init the connection pool
	 */
	private Conn(){
		this.user = Configuration.getPsql_user();
		this.passwd = Configuration.getPsql_pwd();
		this.base = "jdbc:postgresql://127.0.0.1/"+Configuration.getPsql_db();
		initConnectionPool(base,user,passwd);



	}



	/**
	 * initialize the connection pool
	 * @param base the base
	 * @param user the user
	 * @param passwd the password
	 */
	private void initConnectionPool(String base, String user, String passwd) {
		while(!checkIfConnectionPoolIsFull()){
			connectionPool.addElement(createNewConnection(base,user,passwd));
		}
	}


	/**
	 * create a new connection
	 * @param base the base
	 * @param user the user
	 * @param passwd the passwd
	 * @return a Connection
	 */
	private Connection createNewConnection(String base, String user, String passwd) {
		Connection connection = null;
		try{
			Class.forName(DRIVER);
			connection = DriverManager.getConnection(base, user, passwd);
		}
		catch(SQLException sqle){
			Application.fatal(sqle);
		}
		catch(ClassNotFoundException cnfe){
			Application.fatal(cnfe);
		}
		return connection;
	}


	/**
	 * check if the pool is full
	 * @return true if full
	 */
	private boolean checkIfConnectionPoolIsFull() {
		if(connectionPool.size() < MAX_POOL_SIZE){
			return false;
		}
		return true;
	}







	public synchronized Connection getFromPool(){
		if(connectionPool.size()> 0){
			Connection connection = (Connection) connectionPool.firstElement();
			connectionPool.removeElementAt(0);
			try {
				if(null==connection || connection.isClosed()){
					connection = createNewConnection(base, user, passwd);
				}
			} catch (SQLException e) {
				Application.error(e);
			}
			return connection;
		} else {//loop until connection available
			return getFromPool(1);
		}
	}

	private Connection getFromPool(int i) {
		if(i<MAX_LOOP){
			if(connectionPool.size()> 0){
				Connection connection = (Connection) connectionPool.firstElement();
				connectionPool.removeElementAt(0);
				try {
					if(null==connection || connection.isClosed()){
						connection = createNewConnection(base, user, passwd);
					}
				} catch (SQLException e) {
					Application.error(e);
				}
				return connection;
			} else {//loop until connection available
				return getFromPool(i+1);
			}
		} else {
			Application.fatal("no more connection in the pool ");
			return null;
		}
	}



	public static synchronized void returnToPool(Connection connection){
		connectionPool.addElement(connection);
	}





	/**
	 * close all connections
	 */
	public static void destroy(){
		while(connectionPool.size()>0){
			Connection connection = (Connection) connectionPool.firstElement();
			connectionPool.removeElementAt(0);
			try {
				connection.close();
			} catch (SQLException e) {
				Application.warn(e);
			}
		}
	}


}

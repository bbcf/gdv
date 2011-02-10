package ch.epfl.bbcf.gdv.access.database;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.wicket.Request;
import org.apache.wicket.Session;




import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;

/**
 * Manage the connection with the pgsql database
 * @author Yohan Jarosz
 *
 */
public class Connect implements Connection{

	public static final String DRIVER = "org.postgresql.Driver";
	public static final String BASE = "gdv_dev";
	public static final String	URL = "jdbc:postgresql://127.0.0.1/"+BASE;
	public static final String USER = "java";
	public static final String PASSWD ="java_gdv_bbcf";
	/**
	 * The next serial number to be assigned
	 */

	/**
	 * contains all connection to the database
	 */
	private  static HashMap<String, Connect> databasePool; 
	/**
	 * status of the connection
	 * false : disconnected
	 * true : connected
	 */
	private static boolean isConnected;

	/**
	 * debug level : 
	 * 0: no display
	 * 1: display failed and not forced queries
	 * 2: display all failed queries
	 * 3: display slow and failed queries
	 * 4: display slow queries
	 * 5: display all queries
	 */
	private int         debugLevel;





	private static Connect instance;
	private Connection connection;
	private UserSession session;

	private  Connect(UserSession session,String driver, String url, String user, String passwd, String base){
		try {
			Class.forName(driver).newInstance();
		}catch (ClassNotFoundException e){
			Application.error("Error loading JDBC driver: " + e, session.getUserId());
			return;
		} catch (InstantiationException e) {
			Application.error("Error loading JDBC driver: " + e, session.getUserId());
		} catch (IllegalAccessException e) {
			Application.error("Error loading JDBC driver: " + e, session.getUserId());
		}
		try {
			connection = DriverManager.getConnection(url, user, passwd);
			this.setConnected(true);
		} catch (SQLException e) {
			Application.error("Error getting the connection " + e, session.getUserId());
			return;
		}
		this.setSession(session);
	}	

	//	public void finalize(){
	//		int identifier = new Integer(Connect.getIdentifier());
	//		Application.debug("finalize connection : "+identifier, 1);
	//		HashMap<String,Connect> dataBaseCollection = databasePool.get(identifier);
	//		dataBaseCollection.clear();
	//	}

	public static Connect getConnection() {
		String driver = DRIVER;
		String	url = URL;
		String user = USER;
		String passwd = PASSWD;
		String base = BASE;
		return Connect.getConnection(null,driver,url,user,passwd,base);
	}
	public static Connect getConnection(UserSession session){
		String driver = DRIVER;
		String	url = URL;
		String user = USER;
		String passwd = PASSWD;
		String base = BASE;
		return Connect.getConnection(session,driver,url,user,passwd,base);

	}

	private static Connect getConnection(UserSession session,String driver, String url,
			String user, String passwd, String base) {
		String identifier = null;
		if(null==databasePool){
			synchronized(Connect.class){
				databasePool = new HashMap<String, Connect>();
			}
		}
		if(null==session){
			identifier = "admin";
		}
		else {
			identifier = session.getId();
		}
		//	Application.debug("get connection with id : "+identifier);
		if(null!=session){
			//Application.debug(Connect.class+" get database connection : "+identifier);
		}
		Connect connection  = 
			Connect.databasePool.get(identifier);

		String key = user + base;
		if (connection != null) {
			instance = connection;
		} else {
			synchronized(Connect.class){
				instance = new Connect(session,driver,url,user,passwd,base);
			}
			Connect.databasePool.put(identifier, instance);
		}
		return instance;
	}

	public static void removeConnection(UserSession userSession) {
		Application.debug("REMOVING CONNECTION "+userSession.getUserId());
		if(Connect.databasePool.containsKey(userSession.getId())){
			Connect conn = Connect.databasePool.get(userSession.getId());
			try {
				conn.close();
			} catch (SQLException e) {
				Application.error(e);
			}
			Connect.databasePool.remove(userSession.getId());
			Application.debug("removing connection : "+userSession.getId());
			//			if(Connect.databasePool.isEmpty()){
			//				try {
			//					Driver d = DriverManager.getDriver(URL);
			//					DriverManager.deregisterDriver(d);
			//				} catch (SQLException e) {
			//					Application.error(e);
			//				}
			//			}
		}

	}
	public static void removeAllConnection() {
		Iterator<String> it = Connect.databasePool.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			Connect conn = Connect.databasePool.get(key);
			try {
				if(null!=conn){
					conn.close();
				}
			} catch (SQLException e) {
				Application.error(e);
			}
		}
		Connect.databasePool = new HashMap<String, Connect>();

	}


	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	public void close() throws SQLException {
		this.setConnected(false);
		if(connection!=null){
			connection.close();
		}
	}

	public void commit() throws SQLException {
		connection.commit();
	}

	public Statement createStatement() throws SQLException {
		return connection.createStatement();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
	throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
	throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
	throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
	throws SQLException {
		return connection.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
	throws SQLException {
		return connection.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
	throws SQLException {
		return connection.prepareStatement(sql, columnNames);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
	throws SQLException {
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);		
	}

	public void rollback() throws SQLException {
		connection.rollback();		
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);		
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);		
	}

	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);		
	}

	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);		
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);		
	}

	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);		
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connection.setTypeMap(map);		
	}

	public ResultSet executeQuery(PreparedStatement statement) throws SQLException {
		ResultSet resultSet = (ResultSet) statement.executeQuery();
		return resultSet;
	}
	/**
	 * WARNING : like this it's just work if there is just one update to do
	 * @param statement
	 * @throws SQLException
	 */
	public void executeUpdate(PreparedStatement statement) throws SQLException {
		//Application.debug(statement.toString());
		connection.setAutoCommit(false);
		statement.executeUpdate();
		//Application.debug(statement.getUpdateCount());
		if((statement.getUpdateCount()!=-1)){
			connection.commit();
			connection.setAutoCommit(true);
		}
		else {
			connection.rollback();
			connection.setAutoCommit(true);
			Application.error("ROLLBACK");
		}
	}

	public void execute(PreparedStatement statement) throws SQLException {
		statement.execute();

	}


	public Array createArrayOf(String typeName, Object[] elements)
	throws SQLException {
		return connection.createArrayOf(typeName, elements);
	}


	public Blob createBlob() throws SQLException {
		return connection.createBlob();
	}


	public Clob createClob() throws SQLException {
		return connection.createClob();
	}


	public NClob createNClob() throws SQLException {
		return connection.createNClob();
	}


	public SQLXML createSQLXML() throws SQLException {
		return connection.createSQLXML();
	}


	public Struct createStruct(String typeName, Object[] attributes)
	throws SQLException {
		return connection.createStruct(typeName, attributes);
	}


	public Properties getClientInfo() throws SQLException {
		return connection.getClientInfo();
	}


	public String getClientInfo(String name) throws SQLException {
		return connection.getClientInfo(name);
	}


	public boolean isValid(int timeout) throws SQLException {
		return connection.isValid(timeout);
	}


	public void setClientInfo(Properties properties)
	throws SQLClientInfoException {
		connection.setClientInfo(properties);
	}


	public void setClientInfo(String name, String value)
	throws SQLClientInfoException {
		connection.setClientInfo(name, value);
	}


	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return connection.isWrapperFor(iface);
	}


	public <T> T unwrap(Class<T> iface) throws SQLException {
		return connection.unwrap(iface);
	}

	/**
	 * @param isConnected the isConnected to set
	 */
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	/**
	 * @return the isConnected
	 */
	public static boolean isConnected() {
		return isConnected;
	}

	public void startTransaction() {
		try {
			this.setAutoCommit(false);
		} catch (SQLException e) {
			Application.error(e, session.getUserId());
		}
	}

	public void endTransaction(boolean b){
		try {
			if(b){
				this.commit();
			}
			else {
				this.rollback();
			}
		} catch (SQLException e) {
			Application.error(e, session.getUserId());
		}
	}

		/**
		 * @param session the session to set
		 */
		public void setSession(UserSession session) {
			this.session = session;
		}

		/**
		 * @return the session
		 */
		public UserSession getSession() {
			return session;
		}






	}


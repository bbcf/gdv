package ch.epfl.bbcf.conversion.daemon;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.bbcfutils.access.genrep.GenRepAccess;
import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
import ch.epfl.bbcf.conversion.conf.Configuration;



/**
 * @author Jarosz Yohan
 *
 */
public class Daemon {


	private static final int ARGS_LENGTH = 6;
	private static final int shutdownTime = 2;
	public static final Logger logger = Launcher.initLogger(Daemon.class.getName());


	public static void main (String[] args) {
		if(Configuration.init()){
			if(initialization()){
				logger.info("transform to sqlite daemon started");
//				try {
//					if(!GenRepAccess.testConnection()){
//						logger.error("no connection on Jbrowsor or genrep");
//						shutdownHook();
//					}
//				} catch (IOException e) {
//					logger.error(e.getMessage()+" - no connection on Jbrowsor or genrep");
//					shutdownHook();
//				}


				Thread runtimeHookThread = new Thread() {
					public void run() {
						shutdownHook(); 
					}
				};
				Runtime.getRuntime().addShutdownHook (runtimeHookThread);
				try {
					while (true) {
						Thread.sleep (5000);
						doJob();
					}
				}
				catch (Throwable t) {
					logger.debug ("ThreadException: "+t.toString()); 
				}
			}
		}
	}

	private static boolean initialization() {
		String curDir = System.getProperty("user.dir");
		File jobs = new File(curDir+Configuration.JOBS_FILE);
		if(jobs.exists()){
			logger.debug("already initialized");
			return true;
		}
		logger.info("initializations of database");
		jobs.delete();
		try {
			if(!SQLiteAccess.initJobsDatabase(jobs)){
				logger.error("couldn't create database "+jobs.getAbsolutePath());
				return false;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return true;

	}

	private static void shutdownHook() {
		logger.info ("ShutdownHook started");
		long t0 = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep (500); 
			}
			catch (Exception e) {
				logger.error ("Exception: "+e.toString());
				break; 
			}
			if (System.currentTimeMillis() - t0 > shutdownTime*1000) break;
		}
		ManagerService.destruct();
		logger.info ("ShutdownHook completed");
		String udir = System.getProperty("user.dir");
		if(new File(udir+Configuration.DAEMON_FILE).exists())
			if(new File(udir+Configuration.DAEMON_FILE).delete())
				logger.info (udir+Configuration.DAEMON_FILE+" file deleted");
	}




	/*JOBS METHODS*/
	public static void doJob(){
		boolean doJob = true;
		try {
			String args[] = getjob();
			if(args.length!=ARGS_LENGTH){
				doJob=false;
				logger.error("bad args length: "+args.length+" provided (need "+ARGS_LENGTH+")");
			}
			for(String str:args){
				if(null==str){
					doJob = false;
				}
			}



			if(doJob){
				logger.debug("executing : file("+args[0]+"),trackId("+args[1]+"),tmpdir("+args[2]+"),extension("+args[3]+")," +
						"mail("+args[4]+"),nrassemblyid("+args[5]+")");
				Launcher l = new Launcher();
				l.process(args[0],args[1],args[2],args[3],args[4],args[5]);

			}
		} catch (ClassNotFoundException e) {
			logger.debug("class not found:"+e.getMessage());
		} catch (SQLException e) {
			for(StackTraceElement el : e.getStackTrace()){
				logger.debug(e.getMessage());
				logger.debug(el.getFileName()+"."+el.getMethodName()+"."+el.getMethodName()+" (l."+el.getLineNumber()+")");
			}

		}
	}

	/*SQLITE METHODS*/
	public static Connection getConnection() throws ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"+Configuration.getJobsFile());
		return conn;
	}
	public static ResultSet getResultSet(Statement stat,String query) throws SQLException{
		return stat.executeQuery(query);
	}

	/**
	 * The boolean sendMail is used to know if the track will be 
	 * admin
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static String[] getjob() throws ClassNotFoundException, SQLException{
		String[] args = new String[ARGS_LENGTH];
		Connection conn = getConnection();
		//getting first job
		String query = "select * from jobs limit 1;";
		PreparedStatement prep = conn.prepareStatement(query);
		prep.execute();
		ResultSet rs = prep.getResultSet();
		while (rs.next()) {
			args[0] = rs.getString("file");
			args[1] = rs.getString("trackid");
			args[2] = rs.getString("tmpdir");
			args[3] = rs.getString("extension");
			args[4] = rs.getString("mail");
			args[5] = rs.getString("nrassemblyid");
		}
		rs.close();
		//delete from list
		prep = conn.prepareStatement("" +
				"delete from jobs " +
				"where file = '" +args[0]+ "' " +
				"and trackid = '"+args[1]+ "' " +
				"and extension = '"+args[3]+ "' " +
				"and mail = '"+args[4]+ "'; ");
		prep.executeUpdate();
		conn.close();
		return args;
	}

}

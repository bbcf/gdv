package ch.epfl.bbcf.conversion.daemon;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.conf.Configuration;

/**
 * This class take input from DATABASE and run them
 * database indb | inpath | outdb | outpath | rapidity
 * @author Yohan Jarosz
 *
 */
public class Daemon {

	public static final Logger logger = Configuration.initLogger(Daemon.class.getName());

	private static final int shutdownTime = 2;

	/*DAEMON METHODS*/
	public static void main (String[] args) {
		if(Configuration.init()){
			if(initialization()){
				Thread runtimeHookThread = new Thread() {
					public void run() {
						shutdownHook(); 
					}
				};
				Runtime.getRuntime().addShutdownHook (runtimeHookThread);
				logger.info("running ... ");
				try {
					while (true) {
						Thread.sleep (5000);
						if(ManagerService.canExecute()){
							doJob();
						}
					}
				}
				catch (Throwable t) {
					logger.error("Exception: "+t.toString()); 
				}
				
			}
		}
	}

	private static boolean initialization() {
		File jobs = new File(Configuration.getWorkingDirectory()+"/"+Configuration.JOBS_DATABASE);
		if(jobs.exists()){
			logger.debug("jobs database already initialized");
			return true;
		}
		logger.info("initializations of jobs database");
		jobs.delete();
		try {
			if(!Configuration.initJobsDatabase(jobs)){
				logger.error("couldn't create database "+jobs.getAbsolutePath());
				return false;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		logger.debug("ok");
		return true;

	}
	
	private static void shutdownHook() {
		logger.debug("ShutdownHook started");
		long t0 = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep (500); 
			}
			catch (Exception e) {
				logger.error("Exception: "+e.toString());
				break; 
			}
			if (System.currentTimeMillis() - t0 > shutdownTime*1000) break;
		}
		ManagerService.shutdown();
		logger.info("ShutdownHook completed");
		if(new File(Configuration.DAEMON_FILE).exists())
			if(new File(Configuration.DAEMON_FILE).delete())
				logger.info(Configuration.DAEMON_FILE+" file deleted");
	}


	/*JOBS METHODS*/
	public static void doJob(){
		boolean doJob = true;
		try {
			String args[] = getjob();
			if(args.length!=7){
				doJob=false;
				logger.error("bad args length. "+args.length+" args provided (need 7)");
			}
			for(String str:args){
				if(null==str){
					doJob = false;
				}
			}

			if(doJob){
				logger.info("executing : for trackId = "+args[0]+" -input db "+args[1]+" with path : "+args[2]+" " +
						" -output directory : "+args[3]+" with path : "+args[4]+
						" -fast:slow ? "+args[5]+" -mail : "+args[6]);
				Launcher l = new Launcher();
				if(Integer.valueOf(args[5])==ManagerService.FAST){
					l.process(args[0],args[1],args[2],args[3],args[4],ManagerService.FAST,args[6]);
				} else if (Integer.valueOf(args[5])==ManagerService.SLOW){
					l.process(args[0],args[1],args[2],args[3],args[4],ManagerService.SLOW,args[6]);
				}
				else {
					logger.error("bad arg : "+args[5]);
				}

			}
		} catch (ClassNotFoundException e) {
			logger.error("class not found:"+e.getMessage());
		} catch (SQLException e) {
			logger.error("SQL error : "+e.getMessage());
		}
	}

	/*SQLITE METHODS*/
	public static Connection getConnection() throws ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"+Configuration.getWorkingDirectory()+"/"+Configuration.JOBS_DATABASE);
		return conn;
	}
	public static ResultSet getResultSet(Statement stat,String query) throws SQLException{
		return stat.executeQuery(query);
	}

	public static String[] getjob() throws ClassNotFoundException, SQLException{
		String[] args = new String[7];
		Connection conn = getConnection();
		//getting first job
		String query = "select * from jobs limit 1;";
		PreparedStatement prep = conn.prepareStatement(query);
		prep.execute();
		ResultSet rs = prep.getResultSet();
		while (rs.next()) {
			args[0] = rs.getString("trackId");
			args[1] = rs.getString("indb");
			args[2] = rs.getString("inpath");
			args[3] = rs.getString("outdb");
			args[4] = rs.getString("outpath");
			args[5] = rs.getString("rapidity");
			args[6] = rs.getString("mail");
		}
		rs.close();
		//delete from list
		prep = conn.prepareStatement("" +
				"delete from jobs " +
				"where indb = '"+args[1]+ "'" +
				"and inPath = '"+args[2]+ "' " +
				"and outdb = '"+args[3]+ "' " +
				"and outPath = '"+args[4]+ "' " +
				"and rapidity = '"+args[5]+ "';");
		prep.executeUpdate();
		conn.close();
		return args;
	}

}

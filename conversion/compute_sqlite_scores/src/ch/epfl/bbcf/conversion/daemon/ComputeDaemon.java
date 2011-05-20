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
public class ComputeDaemon {


	private static final int shutdownTime = 2;

	/*DAEMON METHODS*/
	public static void main (String[] args) {
		if(args.length<1){
			System.err.println("no args : the first args must be how you call the PARAMETER GDV_HOME");
		}
		if(Configuration.init(args[0])){
			if(initialization()){
				Thread runtimeHookThread = new Thread() {
					public void run() {
						shutdownHook(); 
					}
				};
				Runtime.getRuntime().addShutdownHook (runtimeHookThread);
				Configuration.getLoggerInstance().info("running ... ");
				try {
					while (true) {
						Thread.sleep (5000);
						if(ManagerService.canExecute()){
							doJob();
						}
					}
				}
				catch (Throwable t) {
					Configuration.getLoggerInstance().error("Exception: "+t.toString()); 
				}

			}
		}
	}

	private static boolean initialization() {
		File jobs = new File(Configuration.getWorkingDirectory()+"/"+Configuration.JOBS_DATABASE);
		Configuration.getLoggerInstance().info("initializations of jobs database");
		jobs.delete();
		try {
			if(!Configuration.initJobsDatabase(jobs)){
				Configuration.getLoggerInstance().error("couldn't create database "+jobs.getAbsolutePath());
				return false;
			}
		} catch (Exception e) {
			Configuration.getLoggerInstance().error(e);
		}
		Configuration.getLoggerInstance().debug("ok");
		return true;

	}

	private static void shutdownHook() {
		Configuration.getLoggerInstance().debug("ShutdownHook started");
		long t0 = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep (500); 
			}
			catch (Exception e) {
				Configuration.getLoggerInstance().error("Exception: "+e.toString());
				break; 
			}
			if (System.currentTimeMillis() - t0 > shutdownTime*1000) break;
		}
		ManagerService.shutdown();
		Configuration.getLoggerInstance().info("ShutdownHook completed");
		if(new File(Configuration.DAEMON_FILE).exists())
			if(new File(Configuration.DAEMON_FILE).delete())
				Configuration.getLoggerInstance().info(Configuration.DAEMON_FILE+" file deleted");
	}


	/*JOBS METHODS*/
	public static void doJob(){
		boolean doJob = true;
		try {
			Job job = getjob();
			if(job.isRunnable()){
				Configuration.getLoggerInstance().info("executing : "+job.toString());
				Launcher l = new Launcher();
				l.process(job);
			}
		} catch (ClassNotFoundException e) {
			Configuration.getLoggerInstance().error("class not found:"+e.getMessage());
		} catch (SQLException e) {
			Configuration.getLoggerInstance().error("SQL error : "+e.getMessage());
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

	public static Job getjob() throws ClassNotFoundException, SQLException{
		Connection conn = getConnection();
		Job job = new Job();
		//getting first job
		String query = "select * from jobs limit 1;";
		PreparedStatement prep = conn.prepareStatement(query);
		prep.execute();
		ResultSet rs = prep.getResultSet();
		if(rs.next()) {
			job.setTrackId(rs.getInt("trackId"));
			job.setIndb(rs.getString("indb"));
			job.setInPath(rs.getString("inpath"));
			job.setOutdb(rs.getString("outdb"));
			job.setOutPath(rs.getString("outpath"));
			job.setRapidity(rs.getInt("rapidity"));
			job.setMail(rs.getString("mail"));
			job.setFeedbackUrl(rs.getString("feedback_url"));
			job.setTmpDir(rs.getString("tmp_dir"));
		}
		rs.close();
		//delete from list
		prep = conn.prepareStatement("" +
				"delete from jobs " +
				"where indb = '"+job.getIndb()+ "'" +
				"and inPath = '"+job.getInPath()+ "' " +
				"and outdb = '"+job.getOutdb()+ "' " +
				"and outPath = '"+job.getOutPath()+ "' " +
				"and trackId = '"+job.getTrackId()+ "';");
		prep.executeUpdate();
		conn.close();
		return job;
	}

}

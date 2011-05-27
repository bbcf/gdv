package ch.epfl.bbcf.conversion.daemon;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteConstruct;
import ch.epfl.bbcf.conversion.conf.Configuration;



/**
 * @author Jarosz Yohan
 *
 */
public class TransformDaemon {


	private static final int shutdownTime = 2;


	public static void main (String[] args) {
		if(args.length<1){
			System.err.println("no args : the first args must be how you call the PARAMETER GDV_HOME");
		}
		if(Configuration.init(args[0])){
			if(initialization()){
				Configuration.getLoggerInstance().info("transform to sqlite daemon started");
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
					Configuration.getLoggerInstance().debug ("ThreadException: "+t.toString()); 
				}
			}
		}
	}

	private static boolean initialization() {
		File jobs = new File(Configuration.getJobsFile());
		Configuration.getLoggerInstance().info("initializations of database");
		jobs.delete();
		try {
			if(!Configuration.initJobsDatabase(jobs)){
				Configuration.getLoggerInstance().error("couldn't create database "+jobs.getAbsolutePath());
				return false;
			}
		} catch (Exception e) {
			Configuration.getLoggerInstance().error(e);
		}
		return true;

	}

	private static void shutdownHook() {
		Configuration.getLoggerInstance().info ("ShutdownHook started");
		long t0 = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep (500); 
			}
			catch (Exception e) {
				Configuration.getLoggerInstance().error ("Exception: "+e.toString());
				break; 
			}
			if (System.currentTimeMillis() - t0 > shutdownTime*1000) break;
		}
		ManagerService.destruct();
		Configuration.getLoggerInstance().info ("ShutdownHook completed");
		if(new File(Configuration.getWorkingDirectory()+"/"+Configuration.DAEMON_FILE).exists())
			if(new File(Configuration.getWorkingDirectory()+"/"+Configuration.DAEMON_FILE).delete())
				Configuration.getLoggerInstance().info (Configuration.getWorkingDirectory()+"/"+Configuration.DAEMON_FILE+" file deleted");
	}




	/*JOBS METHODS*/
	public static void doJob(){
		try {
			Job job = getjob();
			if(job.isRunnable()){
				Configuration.getLoggerInstance().debug("executing : "+job.toString());
				Launcher l = new Launcher();
				l.process(job);
			}
		} catch (ClassNotFoundException e) {
			Configuration.getLoggerInstance().debug("class not found:"+e.getMessage());
		} catch (SQLException e) {
			for(StackTraceElement el : e.getStackTrace()){
				Configuration.getLoggerInstance().debug(e.getMessage());
				Configuration.getLoggerInstance().debug(el.getFileName()+"."+el.getMethodName()+"."+el.getMethodName()+" (l."+el.getLineNumber()+")");
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
	public static Job getjob() throws ClassNotFoundException, SQLException{
		Job job = new Job();
		Connection conn = getConnection();
		//getting first job
		String query = "select * from jobs limit 1;";
		PreparedStatement prep = conn.prepareStatement(query);
		prep.execute();
		ResultSet rs = prep.getResultSet();
		if (rs.next()) {
			job.setFile(rs.getString("file"));
			job.setTrackId(rs.getInt("trackid"));
			job.setTmpdir(rs.getString("tmpdir"));
			job.setExtension(rs.getString("extension"));
			job.setMail(rs.getString("mail"));
			job.setNrAssemblyId(rs.getInt("nrassemblyid"));
			job.setFeedbackUrl(rs.getString("feedback_url"));
			job.setOutputDirectory(rs.getString("outdir"));
			job.setJbrowseOutputDirectory(rs.getString("jbrowse_outdir"));
			job.setJbrowseRessourcesUrl(rs.getString("jbrowse_ressource_url"));

		}
		rs.close();
		//delete from list
		prep = conn.prepareStatement("" +
				"delete from jobs " +
				"where file = '" +job.getFile()+ "' " +
				"and trackid = '"+job.getTrackId()+ "' " +
				"and extension = '"+job.getExtension()+ "' " +
				"and mail = '"+job.getMail()+ "'; ");
		prep.executeUpdate();
		conn.close();
		return job;
	}

}

package ch.epfl.bbcf.gdv.config;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import ch.epfl.bbcf.gdv.access.database.dao.DAO;
import ch.epfl.bbcf.gdv.access.gfeatminer.GFeatMinerAccess;
import ch.epfl.bbcf.gdv.control.http.QueriesFilter;
import ch.epfl.bbcf.gdv.formats.das.DAS;
import ch.epfl.bbcf.gdv.html.PostPage;

public class Logs {

	private static Level debugLevel = Level.DEBUG;



	public static Logger initUserLogger(int userId){
		Logger out = Logger.getLogger(Integer.toString(userId));
		out.setLevel(debugLevel);
		out.setAdditivity(false);
		SimpleLayout layout = new SimpleLayout();
		RollingFileAppender appender = null;
		try {
			appender = new RollingFileAppender(layout,Configuration.getLog_dir()+"/"+userId+".log",true);
			appender.setName(Integer.toString(userId));
		} catch (IOException e) {
			e.printStackTrace();
		}
		appender.setMaxFileSize("10KB");
		out.addAppender(appender);
		return out;
	}

	public static Logger initDASLogger() {
		Logger out = Logger.getLogger(DAS.class.getName());
		out.setAdditivity(false);
		out.setLevel(debugLevel);
		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
		RollingFileAppender appender = null;
		try {
			appender = new RollingFileAppender(layout,Configuration.getLog_dir()+"/das.log",true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.addAppender(appender);
		return out;
	}

	public static Logger initSQLLogger() {
		Logger out = Logger.getLogger(DAO.class.getName());
		out.setAdditivity(false);
		out.setLevel(debugLevel);
		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
		RollingFileAppender appender = null;
		try {
			appender = new RollingFileAppender(layout,Configuration.getLog_dir()+"/sql.log",true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.addAppender(appender);
		return out;
	}
	public static Logger initGFeatMinerLogger() {
		Logger out = Logger.getLogger(GFeatMinerAccess.class.getName());
		out.setAdditivity(false);
		out.setLevel(debugLevel);
		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
		RollingFileAppender appender = null;
		try {
			if(out.getAppender(GFeatMinerAccess.class.getName())==null){
				appender = new RollingFileAppender(layout,Configuration.getLog_dir()+"/gFeatMiner.log",true);
				appender.setName(GFeatMinerAccess.class.getName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.addAppender(appender);
		return out;
	}

	public static Logger initPOSTLogger(String name){
		Logger out = Logger.getLogger(name);
		out.setAdditivity(false);
		out.setLevel(debugLevel);
		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
		RollingFileAppender appender = null;
		try {
			if(out.getAppender(name)==null){
				appender = new RollingFileAppender(layout,Configuration.getLog_dir()+"/post.log",true);
				appender.setName(name);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.addAppender(appender);
		return out;
	}
	public static Logger init(String directory) {
		Logger out = Logger.getLogger(Application.class.getName());
		out.setAdditivity(false);
		out.setLevel(debugLevel);
		PatternLayout layout = new PatternLayout("[%-5p] %c - %m%n");
		RollingFileAppender appender = null;
		try {
			appender = new RollingFileAppender(layout,directory+"/logs/gdv.log",true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.addAppender(appender);
		return out;
	}
	/**
	 * 0 : debug all
	 * 1 : debug errors only
	 * 2 : debug SQL only
	 * 3 : debug all but not SQL 
	 */

	public static Logger initLogger(String name) {
		Logger out = Logger.getLogger(name);
		out.setAdditivity(false);
		out.setLevel(Level.DEBUG);
		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
		RollingFileAppender appender = null;
		try {
			appender = new RollingFileAppender(layout,Configuration.getLog_dir()+"/gdv.log",true);
		} catch (IOException e) {
			Application.error(e);
		}
		out.addAppender(appender);
		return out;
	}

	private class myAppender implements Appender {

		public void addFilter(Filter newFilter) {
			// TODO Auto-generated method stub

		}

		public void clearFilters() {
			// TODO Auto-generated method stub

		}

		public void close() {
			// TODO Auto-generated method stub

		}

		public void doAppend(LoggingEvent event) {
			// TODO Auto-generated method stub

		}

		public ErrorHandler getErrorHandler() {
			// TODO Auto-generated method stub
			return null;
		}

		public Filter getFilter() {
			// TODO Auto-generated method stub
			return null;
		}

		public Layout getLayout() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean requiresLayout() {
			// TODO Auto-generated method stub
			return false;
		}

		public void setErrorHandler(ErrorHandler errorHandler) {
			// TODO Auto-generated method stub

		}

		public void setLayout(Layout layout) {
			// TODO Auto-generated method stub

		}

		public void setName(String name) {
			// TODO Auto-generated method stub

		}

	}
	//	public static void debug(Map<String, Logger> logs, int debuglevel, Object message,
	//			int level) {
	//		if(debuglevel==0){
	//			if(level!=2){
	//				logs.get("out").info(message);
	//			}
	//			else {
	//				logs.get("sql").info(message);
	//			}
	//		}
	//		else if(debuglevel==1){
	//
	//		}
	//		else if (debuglevel==2){
	//			if(level==2){
	//				logs.get("sql").info(message);
	//			}
	//		}
	//		else if (debuglevel==3){
	//			if(level!=2){
	//				logs.get("out").info(message);
	//			}
	//		}
	//
	//	}
	//
	//	public static void error(Map<String, Logger> logs, int debuglevel, Object message) {
	//		logs.get("err").info(message);
	//	}
	public static void debug(Logger logs, int debuglevel, Object message,
			int level) {
		if(debuglevel==0){
			if(level!=2){
				logs.info(message);
			}
			else {
				logs.info(message);
			}
		}
		else if(debuglevel==1){

		}
		else if (debuglevel==2){
			if(level==2){
				logs.info(message);
			}
		}
		else if (debuglevel==3){
			if(level!=2){
				logs.info(message);
			}
		}

	}

	public static void error(Logger logs, int debuglevel, Object message) {
		logs.error(message);
	}

	public static Logger initQueriesLogger() {
		Logger out = Logger.getLogger(QueriesFilter.class.getName());
		out.setAdditivity(false);
		out.setLevel(debugLevel);
		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
		RollingFileAppender appender = null;
		try {
			if(out.getAppender(QueriesFilter.class.getName()) != null){
				appender = (RollingFileAppender) out.getAppender(QueriesFilter.class.getName());
			} else {
				appender = new RollingFileAppender(layout,Configuration.getLog_dir()+"/browser_queries.log",true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.addAppender(appender);
		return out;
	}








}

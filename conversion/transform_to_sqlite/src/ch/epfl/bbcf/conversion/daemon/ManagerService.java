package ch.epfl.bbcf.conversion.daemon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


import ch.epfl.bbcf.conversion.conf.Configuration;



public class ManagerService {

	
	private static ExecutorService sqliteInstance;


	/**
	 * will process minor jobs for accessing & 
	 * inserting in sqlite databases 
	 * @param r - the job
	 * @return
	 */
	public static Future<?> submitSQLiteProcess(Runnable r) {
		ExecutorService es = getSQLiteInstance();
		return es.submit(r);
	}






	////// GET INSTANCES //////



	private static ExecutorService getSQLiteInstance() {
		if(null==sqliteInstance){
			sqliteInstance = Executors.newCachedThreadPool();
		}
		return sqliteInstance;
	}





	private static void destructExecutorService(ExecutorService es){
		Configuration.getLoggerInstance().info("trying destruction of executor service : "+es+".");
		if(es!=null && !es.isShutdown()){
			try {
				if (!es.awaitTermination(1, TimeUnit.SECONDS)) {
					es.shutdownNow(); // Cancel currently executing tasks
				}
				// Wait a while for existing tasks to terminate
				if (!es.awaitTermination(10, TimeUnit.SECONDS)) {
					es.shutdownNow(); // Cancel currently executing tasks
					// Wait a while for tasks to respond to being cancelled
					if (!es.awaitTermination(10, TimeUnit.SECONDS))
						Configuration.getLoggerInstance().error("Thread manager did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				es.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
				Configuration.getLoggerInstance().error(ie);
			}
			Configuration.getLoggerInstance().info("Executor service : "+es+" destroyed ");
		}
	}


	/**
	 * destruct all executor service and thread pools
	 */
	public static void destruct() {
		destructExecutorService(getSQLiteInstance());
	}

}






package ch.epfl.bbcf.gdv.utility.thread;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ch.epfl.bbcf.gdv.config.Application;

public class ManagerService {

	private static final int PRINCIPAL_THREAD_POOL = 5;
	private static final int SQLITE_CALCULATED_THREAD_POOL = 5;
	private static ExecutorService inputFileInstance,sqliteInstance,calculatedSqliteInstance;

	private static Set<String> dbAccessed;



	////// SUBMIT JOBS //////
	/**
	 * will process jobs from SQLiteProcessor & GFFProcessor
	 * @return
	 */
	public static Future submitPricipalProcess(Runnable r){
		ExecutorService es = getPrincipalInstance();
		return es.submit(r);

	}
	/**
	 * will process minor jobs for accessing & 
	 * inserting in sqlite databases 
	 * @param r - the job
	 * @return
	 */
	public static Future submitSQLiteProcess(Runnable r,String database) {
		ExecutorService es = getSQLiteInstance();
		if(!dbAccessed.contains(database)){
			dbAccessed.add(database);
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Application.error(e);
			}
			return submitSQLiteProcess(r,database);
		}
		return es.submit(r);
	}


	/**
	 * process jobs from sqlite to calculated sqlite
	 * @param r - the job
	 * @return
	 */
	public static Future submitCalculatedSQLiteProcess(Runnable r){
		ExecutorService es = getCalculatedSqliteInstance();
		return es.submit(r);
	}





	////// GET INSTANCES //////


	private static ExecutorService getPrincipalInstance(){
		if(null==inputFileInstance){
			inputFileInstance = Executors.newCachedThreadPool();//Executors.newFixedThreadPool(PRINCIPAL_THREAD_POOL);
		}
		return inputFileInstance;
	}

	private static ExecutorService getSQLiteInstance() {
		if(null==dbAccessed){
			dbAccessed = Collections.synchronizedSet(new HashSet<String>());
		}
		if(null==sqliteInstance){
			sqliteInstance = Executors.newCachedThreadPool();
		}
		return sqliteInstance;
	}

	private static ExecutorService getCalculatedSqliteInstance() {
		if(null==sqliteInstance){
			calculatedSqliteInstance = Executors.newFixedThreadPool(SQLITE_CALCULATED_THREAD_POOL);
		}
		return calculatedSqliteInstance;
	}


	////// OTHERS //////


	/**
	 * remove the access to a database to permit
	 * other thread to access it
	 * @param database
	 */
	public static void removeAccessToDatabase(String database) {
		dbAccessed.remove(database);
	}



	private static void destructExecutorService(ExecutorService es){
		Application.info("trying destruction of executor service : "+es+".");
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
						Application.error("Thread manager did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				es.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
				Application.error(ie);
			}
			Application.info("Executor service : "+es+" destroyed ");
		}
	}


	/**
	 * destruct all executor service and thread pools
	 */
	public static void destruct() {
		destructExecutorService(getPrincipalInstance());
		destructExecutorService(getSQLiteInstance());
		destructExecutorService(getCalculatedSqliteInstance());
	}

}






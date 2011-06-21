package ch.epfl.bbcf.gdv.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class ManagerService {
	
	private static final int POOL_SIZE = 4;
	
	private static ExecutorService instance;


	/**
	 * will process minor jobs for accessing & 
	 * inserting in sqlite databases 
	 * @param r - the job
	 * @return
	 */
	public static Future<?> submitApplicationProcess(Runnable r) {
		ExecutorService es = getInstance();
		return es.submit(r);
	}






	////// GET INSTANCES //////



	private static ExecutorService getInstance() {
		if(null==instance){
			instance = Executors.newFixedThreadPool(POOL_SIZE);
		}
		return instance;
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
		destructExecutorService(getInstance());
	}
}


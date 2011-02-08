package ch.epfl.bbcf.conversion.daemon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.conf.Configuration;

public class ManagerService {


	
	
	public static final Logger logger = Configuration.initLogger(ManagerService.class.getName());
	
	
	private static ExecutorService principal_instance;
	private static final int PRINCIPAL_THREAD_POOL = 4;
	private static final int PRINCIPAL_QUEUE = 4;
	private static int CURRENT_JOB_NUMBER = 0;
	
	//FAST THREADS
	public final static int FAST = 0;
	private static ExecutorService fast_instance;
	
	//LONG THREADS
	public final static int SLOW = 1;
	private static final int THREAD_POOL = 5;
	private static ExecutorService slow_instance;
	
	
	public static boolean canExecute() {
		return CURRENT_JOB_NUMBER <= PRINCIPAL_QUEUE;
	}
	public static void processJob(Launcher launcher) {
		ManagerService.executePrincipal(launcher);
		CURRENT_JOB_NUMBER++;
	}
	public static void endJob() {
		CURRENT_JOB_NUMBER--;
	}
	
	private static ExecutorService getPrincipalInstance(){
		if(null==principal_instance){
			principal_instance = Executors.newFixedThreadPool(PRINCIPAL_THREAD_POOL);
		}
		return principal_instance;
	}
	
	public static Future executePrincipal(Runnable r){
		ExecutorService es = getPrincipalInstance();
		return es.submit(r);
	}
	
	
	private static ExecutorService getInstance(int service){
		if(service==FAST){
			if(null==fast_instance){
				synchronized(ManagerService.class){
					fast_instance = Executors.newCachedThreadPool();
				}
			}
			return fast_instance;
		} else if(service == SLOW){
			if(null==slow_instance){
				synchronized(ManagerService.class){
					slow_instance = Executors.newFixedThreadPool(THREAD_POOL);
				}
			}
			return slow_instance;
		}
		return null;
	}


	public static Future executeScores(Runnable r,int service){
		ExecutorService es = getInstance(service);
		return es.submit(r);
	}
	
	
	
	public static void shutdown(){
		logger.info("shutdown managers");
		shutdown(getInstance(FAST));
		shutdown(getInstance(SLOW));
		shutdown(getPrincipalInstance());
	}
	
	public static void destruct(){
		logger.info("destruct managers");
		destruct(getInstance(FAST));
		destruct(getInstance(SLOW));
		destruct(getPrincipalInstance());
		
	}
	
	public static void shutdown(ExecutorService es){
		es.shutdown();
	}
	
	public static void destruct(ExecutorService es){
		try {
			if (!es.awaitTermination(1, TimeUnit.SECONDS)) {
				es.shutdownNow(); // Cancel currently executing tasks
			}
			// Wait a while for existing tasks to terminate
			if (!es.awaitTermination(60, TimeUnit.SECONDS)) {
				es.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!es.awaitTermination(60, TimeUnit.SECONDS)){

				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			es.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
	
	
	
	


	

	
	
	
	
	
}

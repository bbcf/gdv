package ch.epfl.bbcf.conversion.daemon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.bbcfutils.access.InternetConnection;
import ch.epfl.bbcf.conversion.conf.Configuration;
import ch.epfl.bbcf.conversion.sqltree.ScoreTree;
import ch.epfl.bbcf.utility.ConnectionStore;
import ch.epfl.bbcf.utility.SQLIteManager;
import ch.epfl.bbcf.utility.Sender;

public class Launcher extends Thread{

	public final static int[]zooms = ScoreTree.zooms;
	private Job job;
	

	public void process(Job job) {
		this.job=job;
		ManagerService.processJob(this);
	}

	public void run(){
		Configuration.getLoggerInstance().debug("process tasks");
		try {
			InternetConnection.sendPOSTConnection(job.getFeedbackUrl(),
					"id=track_status&job_id="+job.getTrackId()+"&data=computing", 
					InternetConnection.MIME_TYPE_FORM_APPLICATION);
		} catch (IOException e1) {
			Configuration.getLoggerInstance().error(e1);
		}
		List<Future> tasks = new ArrayList<Future>();

		Map<String, Integer>chrs = 
			SQLIteManager.getChromosomesAndLength(job.getInPath()+"/"+job.getIndb());
		//create directories
		buildDirectories(job.getOutdb(),job.getOutPath());
		//iterate on each chromosomes
		Iterator<String> it = chrs.keySet().iterator();

		int nb = chrs.size();
		int percentage = 100/nb;

		String body = "id=track_status&job_id="+job.getTrackId()+"&data="+percentage;
		while(it.hasNext()){
			String chr = it.next();
			int length = chrs.get(chr);
			//create the new database
			ConnectionStore connectionStore = SQLIteManager.createNewDatabase(job.getOutdb(),job.getOutPath(),chr,zooms);
			//launch processes
			ScoreTree scores = new ScoreTree(job.getTmpDir(),job.getFeedbackUrl(),job.getIndb(),job.getInPath(),job.getOutdb(),job.getOutPath(),length,chr,connectionStore,body);
			Future task = ManagerService.executeScores(scores,job.getRapidity());
			tasks.add(task);
		}
		Configuration.getLoggerInstance().debug("task launched. Waiting for end.....");
		while(waitForEnd(tasks)){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Configuration.getLoggerInstance().error(e);
			}
		}
		try {
			
			body ="id=track_success&job_id="+job.getTrackId()+"&db_type=quantitative&data=end";
			InternetConnection.sendPOSTConnection(job.getFeedbackUrl(),body,InternetConnection.MIME_TYPE_FORM_APPLICATION);
			
			
//			InternetConnection.sendPOSTConnection(
//					job.getFeedbackUrl(),"id=track_status&track_id="+job.getTrackId()
//					+"&mess=completed", InternetConnection.MIME_TYPE_FORM_APPLICATION);
		} catch (IOException e) {
			Configuration.getLoggerInstance().error(e);
		}
		if(!job.getMail().equalsIgnoreCase("nomail")){
			Sender.sendMessage(
					" File processed ",
					"Your file has been processed by GDV you can now browse your projects.", 
					false, job.getMail());
		}
		ManagerService.endJob();
	}




	private boolean waitForEnd(List<Future> tasks) {
		if(!tasks.isEmpty()){
			for(Future task : tasks){
				if(!task.isDone()){
					return true;
				}
			}
		}
		return false;
	}

	private static void buildDirectories(String outdbName,String outdbPath) {
		Configuration.getLoggerInstance().debug("build directories");
		File motherDir = new File(outdbPath+"/"+outdbName);
		if(!motherDir.exists()){
			if(!motherDir.mkdir()){
				Configuration.getLoggerInstance().debug("build directories failed");
			}
		}
	}


	public void notify(boolean done) {
	}

}

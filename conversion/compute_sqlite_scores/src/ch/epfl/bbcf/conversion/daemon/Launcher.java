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

	private static final Logger logger = Configuration.initLogger(Launcher.class.getName());
	public final static int[]zooms = ScoreTree.zooms;
	private String trackId;
	private String indb;
	private String inpath;
	private String outdb;
	private String outpath;
	private int rapidity;
	private String mail;

	public void process(String trackId,String indb, String inpath, String outdb,
			String outpath,int rapidity, String mail) {
		this.trackId = trackId;
		this.indb=indb;
		this.inpath = inpath;
		this.outdb=outdb;
		this.outpath = outpath;
		this.rapidity = rapidity;
		this.mail = mail;
		ManagerService.processJob(this);
	}

	public void run(){
		logger.debug("process tasks");
		try {
			InternetConnection.sendPOSTConnection(Configuration.getFeedbackUrl(),"id=track_status&track_id="+trackId+"&mess=computing", InternetConnection.MIME_TYPE_FORM_APPLICATION);
		} catch (IOException e1) {
			logger.error(e1);
		}
		List<Future> tasks = new ArrayList<Future>();

		Map<String, Integer>chrs = SQLIteManager.getChromosomesAndLength(inpath+"/"+indb);
		//create directories
		buildDirectories(outdb,outpath);
		//iterate on each chromosomes
		Iterator<String> it = chrs.keySet().iterator();

		int nb = chrs.size();
		int percentage = 100/nb;

		String body = "id=track_status&track_id="+trackId+"&mess="+percentage;
		while(it.hasNext()){
			String chr = it.next();
			int length = chrs.get(chr);
			//create the new database
			ConnectionStore connectionStore = SQLIteManager.createNewDatabase(outdb,outpath,chr,zooms);
			//launch processes
			ScoreTree scores = new ScoreTree(indb,inpath,outdb,outpath,length,chr,connectionStore,body);
			Future task = ManagerService.executeScores(scores,rapidity);
			tasks.add(task);
		}
		logger.debug("task launched. Waiting for end.....");
		while(waitForEnd(tasks)){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		try {
			InternetConnection.sendPOSTConnection(Configuration.getFeedbackUrl(),"id=track_status&track_id="+trackId+"&mess=completed", InternetConnection.MIME_TYPE_FORM_APPLICATION);
		} catch (IOException e) {
			logger.error(e);
		}
		if(!mail.equalsIgnoreCase("nomail")){
			Sender.sendMessage(" File processed ","Your file has been processed by GDV you can now browse your projects.", false, mail);
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
		logger.debug("build directories");
		File motherDir = new File(outdbPath+"/"+outdbName);
		if(!motherDir.exists()){
			if(!motherDir.mkdir()){
				logger.debug("build directories failed");
			}
		}
	}


	public void notify(boolean done) {
	}
}

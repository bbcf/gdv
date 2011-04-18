package ch.epfl.bbcf.conversion.sqltree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.bbcfutils.access.InternetConnection;
import ch.epfl.bbcf.conversion.conf.Configuration;
import ch.epfl.bbcf.conversion.daemon.ManagerService;
import ch.epfl.bbcf.utility.ConnectionStore;
import ch.epfl.bbcf.utility.FileManagement;
import ch.epfl.bbcf.utility.SQLIteManager;


public class ScoreTree extends Thread {

	public static final int TAB_WIDTH = 100;
	private static final Logger logger = ManagerService.logger;
	private NodeONE leaf;

	private String dbName;
	private String dbPath;
	private String outdbDirectory;
	private String outdbPath;
	private int imageNumber;
	private int chrLength;
	private String chrName;
	private ConnectionStore connectionStore;

	private String body;




	public final static int[]zooms = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000,
		2000, 5000, 10000, 20000, 50000, 100000};




	public void run(){
		initTree();
		try {
			process();
		} catch (NumberFormatException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (Exception e){
			logger.error(e);
		}
		try {
			InternetConnection.sendPOSTConnection(Configuration.getFeedbackUrl(),body, InternetConnection.MIME_TYPE_FORM_APPLICATION);
		} catch (IOException e) {
			logger.error(e);
		}
	}






	private void process() throws NumberFormatException, IOException {
		//logger.info(this.getId()+" process for "+chrName);
		long s = System.currentTimeMillis();
		File feat = SQLIteManager.getScoresForChromosome(dbPath+"/"+dbName,chrName);
		BufferedReader reader = null;
		FileReader fr = null;
		try {
			fr = new FileReader(feat);
			reader = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
		//logger.debug(this.getId()+" reading file for "+chrName);
		String line;
		while( (line = reader.readLine())!=null){
			String[] sss = line.split("\t");
			if(sss.length==3){
				int start = Integer.valueOf(sss[0]);
				int stop = Integer.valueOf(sss[1]);
				float score = Float.valueOf(sss[2]);
				for(int j=start;j<=stop;j++){//fill the tabs
					imageNumber = getImageNumber(j);
					leaf.fill(j,score,imageNumber);
				}
			}else {
				logger.error("sss length !=3 !!!!!");
			}

		}

		reader.close();
		fr.close();

		leaf.endWriting();
		FileManagement.delete(feat);
		boolean fail = true;
		int cpt = 0;
		float[]minMax = null;
		while(fail){
			try {
				Thread.sleep(1000);
				minMax = SQLIteManager.getMinMaxScoreForChr(outdbPath+"/"+outdbDirectory+"/"+chrName+"_1.db");
				fail = false;
			} catch(SQLException e) {
				logger.error(e);
				cpt++;
				if(cpt>5){
					fail = false;
				}
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		if(null!=minMax){
			buildJSON(minMax,outdbPath,outdbDirectory,zooms,chrName);
		} else {
			logger.error("cannot get min/max for chromosome : "+chrName);
		}
		long end = System.currentTimeMillis();
		long time = (end-s)/1000;
		logger.info(this.getId()+" time elapsed : "+time+"  sec. for "+chrName);

	}



	private static void buildJSON(float[] minMax, String outdbPath, String outDir,int[]zooms,String chrName) {
		String toWrite ="{" +
		"\"zoomLevels\":[" +
		buildJSONFeatures(zooms,outDir,chrName) +
		"]," +
		"\"tileWidth\":200," +
		"\"min\":"+minMax[0]+"," +
		"\"max\":"+minMax[1]+"}";


		File file = new File(outdbPath+"/"+outDir+"/"+chrName+".json");
		try {
			Writer writer = new FileWriter(file);
			writer.write(toWrite);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}


	private static String buildJSONFeatures(int[] zooms, String outDir, String chrName) {
		String features="";
		for(int zoom : zooms){
			features+="{\"urlPrefix\":\""+outDir+"/"+chrName+"_"+zoom+".db\"," +
			"\"height\":\"100\"," +
			"\"basesPerTile\":\""+TAB_WIDTH*zoom+"\"},";
		}
		return features.substring(0, features.length()-1);
	}


	private int getImageNumber(int position){
		double nb = (double)position/TAB_WIDTH;
		return (int)Math.ceil(nb);
	}

	public void writeValues(Node node){
		writeValues(node,false);
	}
	public void writeValues(Node node, boolean finish) {
		SQLIteManager.filldb(chrName,node.getTab(),node.getImageNumber(),node.getZoom(),connectionStore, finish);

	}



	/**
	 * Build a new sqlite database from the sqlite with position and score.
	 * It will store scores at differents zoom levels.
	 * The new sqlite database will be organised like that :
	 * one table per chromosome and zoom like 'name_zoomLevel' 
	 * (e.g. chromosome1 (chr1) at zoom 200 : chr1_200)
	 * in each table, there is two fields :
	 * position (the position of the value ~ number of the image)
	 * scores a string formated like [0,23,27,34,56,45,23,17,1,1,0,.......]
	 * the number of scores is dependant from the variable TAB_WIDTH and it
	 * MUST be a multiple of 100.
	 * @param body 
	 * @param mail 
	 * 
	 * @param args - the args are :
	 * 	1-the name of the input db
	 *  2-the path of input
	 * 	3-the name of the output db
	 * 	4-the path of output
	 *
	 */


	public ScoreTree(String dbName, String dbPath, String outdbName,String outdbPath,
			int chrLength,String chrName, ConnectionStore connectionStore, String body){
		this.dbName=dbName;
		this.dbPath=dbPath;
		this.outdbDirectory=outdbName;
		this.outdbPath=outdbPath;
		this.chrLength = chrLength;
		this.chrName = chrName;
		this.connectionStore = connectionStore;
		this.body = body;
	}



	public void initTree(){
		//logger.debug("new tree => database : "+dbPath+"/"+dbName+" chromosome : "+chrName+" ("+chrLength+") on "+outdbPath+"/"+outdbDirectory);
		NodeONE lastZoom = new NodeONE(this,TAB_WIDTH,100000,null,null,true);
		for(int i=4;i>=0;i--){
			NodeFIVE node5 = new NodeFIVE(this,TAB_WIDTH,(int)Math.pow(10,i)*5,lastZoom);
			NodeTWO node2 = new NodeTWO(this,TAB_WIDTH,(int)Math.pow(10,i)*2);
			lastZoom = new NodeONE(this,TAB_WIDTH,(int)Math.pow(10,i),node2,node5,false);
		}
		lastZoom.setLeaf(true);
		leaf = lastZoom;
	}


	public String viewTree(){
		NodeTWO n2 = leaf.getParentTWO();
		NodeFIVE n5 = leaf.getParentFIVE();
		NodeONE n10 = n5.getParentONE();
		NodeTWO n20 = n10.getParentTWO();
		NodeFIVE n50 = n10.getParentFIVE();
		NodeONE n100 = n50.getParentONE();
		NodeTWO n200 = n100.getParentTWO();
		NodeFIVE n500 = n100.getParentFIVE();
		NodeONE n1000 = n500.getParentONE();
		NodeTWO n2000 = n1000.getParentTWO();
		NodeFIVE n5000 = n1000.getParentFIVE();
		NodeONE n10000 = n5000.getParentONE();
		NodeTWO n20000 = n10000.getParentTWO();
		NodeFIVE n50000 = n10000.getParentFIVE();
		NodeONE n100000 = n50000.getParentONE();

		return "\n\t\t"+leaf.view()+" ... "+n2.view()+"    ...    "+n5.view()+
		"\n\t\t"+n10.view()+" ... "+n20.view()+"    ...    "+n50.view()+
		"\n\t\t"+n100.view()+" ... "+n200.view()+"    ...    "+n500.view()+
		"\n\t\t"+n1000.view()+" ... "+n2000.view()+"    ...    "+n5000.view()+
		"\n\t\t"+n10000.view()+" ... "+n20000.view()+"    ...    "+n50000.view()+
		"\n\t\t"+n100000.view();
	}









}

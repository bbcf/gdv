package ch.epfl.bbcf.formats.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.daemon.Launcher;
import ch.epfl.bbcf.formats.sqlite.SQLiteQuantitativeHandler;
import ch.epfl.bbcf.utility.ChromosomeNameHandler;
import ch.epfl.bbcf.utility.ParsingException;


public class WIGParser {

	public static final int WIG_TRACK_TYPE = 1;
	public static final int BEDGRAPH_TRACK_TYPE = 2;
	private static final int FIXEDSTEP_FORMAT = 2;
	private static final int VARIABLESTEP_FORMAT = 1;
	private int track_type;
	private int format;

	private int lineNb;

	private String chromosome;

	private int step;
	private int start;
	private int span;
	private Float score;

	private Previous previous;
	private SQLiteQuantitativeHandler sqliteHandler;
	private String assemblyId;
	private List<String> chrList;
	private ChromosomeNameHandler nameHandler;

	public static final Logger logger = Launcher.initLogger(WIGParser.class.getName());
	public WIGParser(SQLiteQuantitativeHandler sqlHandler,int trackType,String assemblyId, List<String> chrList){
		this.track_type = trackType;//1 is default
		format = -1;//no default
		this.assemblyId = assemblyId;
		this.chrList = chrList;
		this.nameHandler = new ChromosomeNameHandler(assemblyId);
		step = 1;
		start = 0;
		span=1;
		chromosome=null;
		previous = new Previous();
		lineNb = 0;

		this.sqliteHandler = sqlHandler;

	}

	/**
	 * class to handle values of the
	 * previous line
	 * @author jarosz
	 *
	 */
	private class Previous {

		private int step;
		private int start;
		private int end;
		private Float score;
		private String chromosome;

		public Previous(){
			this.step = 1;
			this.start = 1;
			this.end = 1;
			this.chromosome=null;

		}
	}


	/**
	 * parse the file
	 * @param file
	 * @throws IOException 
	 * @throws ParsingException 
	 */
	public boolean parse(File file) throws IOException, ParsingException {
		String line;
		FileReader fr = null;
		BufferedReader br = null;
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		while ((line = br.readLine()) != null){
			lineNb++;
			processLine(line);
		}
		if(this.track_type==WIG_TRACK_TYPE){
			writeFeature();
		}
		return finalizeDatabase();

		//System.out.println("-d- "+feature.toString());

	}

	/**
	 * Enter method for each line
	 * @param br
	 * @param line
	 * @param file
	 * @throws ParsingException 
	 */
	private void processLine(String line) throws ParsingException {
		if(line.startsWith("track type=")){
			handleTrackType(line);
		} else if(line.startsWith("browser")||line.startsWith("#")){
		} else if(line.startsWith("fixedStep")||line.startsWith("variableStep")){
			handleFormatLine(line);
		} else {
			switch(track_type){
			case WIG_TRACK_TYPE:handleWIGLine(line);break;
			case BEDGRAPH_TRACK_TYPE:handlerBEDLine(line);break;
			}

		}

	}


	/**
	 * handler the line REQUIRED in a wiggle file
	 * to determine the track type
	 * @param line
	 */
	private void handleTrackType(String line) {
		String[] tmp = line.split("=");
		if (tmp[1].startsWith("bedGraph")) {
			track_type= BEDGRAPH_TRACK_TYPE;
		} else if (tmp[1].startsWith("wiggle_0")) {
			track_type= WIG_TRACK_TYPE;
		}

		//get name
		String[] tab = line.split("\\s");
		String trackName = "no_name_provided";
		for (int i = 0; i < tab.length; i++) {
			String[] str = tab[i].split("=");
			if(str[0].equalsIgnoreCase("name")){
				String name = str[1];
			}
		}
	}

	/**
	 * handle the line for determining
	 * the format of the wig file
	 * @param line
	 * @throws ParsingException 
	 */
	private void handleFormatLine(String line) throws ParsingException {
		String[] params = line.split("\\s");
		for (int i = 0; i < params.length; i++) {
			String[] str = params[i].split("=");
			if(str[0].startsWith("fixedStep")){
				format = FIXEDSTEP_FORMAT;
			}
			else if(str[0].startsWith("variableStep")){
				format = VARIABLESTEP_FORMAT;
			}
			else if (str[0].equalsIgnoreCase("chrom")) {
				chromosome = str[1];
				logger.debug("chrName : "+chromosome);
				if(!chrList.contains(str[1])){
					chromosome = nameHandler.getChromosomeAltName(chromosome);
				}
				logger.debug("chrName : "+chromosome);
				if(null==chromosome){
					logger.debug("return");
					return;
				}
				updateFeatureChromosome();
			} else if (str[0].equalsIgnoreCase("span")) {
				try {
					span = Integer.valueOf(str[1]);
				}catch (NumberFormatException e){
					String message = e.toString()+" at line "+lineNb+".";
					throw new ParsingException(message);
				}
			} else if (str[0].equalsIgnoreCase("step")) {
				try {
					step = Integer.valueOf(str[1]);
				}catch (NumberFormatException e){
					String message = e.toString()+" at line "+lineNb+".";
					throw new ParsingException(message);
				}
			} else if (str[0].equalsIgnoreCase("start")) {
				try {
					start = Integer.valueOf(str[1]);
				}catch (NumberFormatException e){
					String message = e.toString()+" at line "+lineNb+".";
					throw new ParsingException(message);
				}
			}
		}

	}




	/**
	 * handle the line for the bedgraph format
	 * @param line
	 * @throws ParsingException 
	 */
	private void handlerBEDLine(String line) throws ParsingException {
		String[] chr_start_end_score = line.split("\\s");
		String chr = chr_start_end_score[0];
		if(!chrList.contains(chr)){
			chr = nameHandler.getChromosomeAltName(chr);
		}
		if(null==chr){
			return;
		}
		int sstart = -1;
		int end = -1;
		float sscore;
		try {
			sstart = Integer.valueOf(chr_start_end_score[1]);
			end = Integer.valueOf(chr_start_end_score[2]);
			sscore = Float.valueOf(chr_start_end_score[3]);
		}catch (NumberFormatException e){
			String message = e.toString()+" at line "+lineNb+".";
			throw new ParsingException(message);
		}
		//span=end-start+1;

		if(null==chromosome || !chromosome.equalsIgnoreCase(chr)){
			chromosome = chr;
			sqliteHandler.newChromosome(chromosome);
		}
		sqliteHandler.writeValues(chr, sstart, end, sscore, null, 0, null);
	}

	/**
	 * handle the line for the wiggle format
	 * @param line
	 * @throws ParsingException 
	 */
	private void handleWIGLine(String line) throws NumberFormatException, ParsingException{
		if(null==chromosome){
			return;
		}
		switch(format){
		case FIXEDSTEP_FORMAT:
			try {
				score = Float.valueOf(line);
			}catch (NumberFormatException e){
				String message = e.toString()+" at line "+lineNb+".";
				throw new ParsingException(message);
			}
			updateFeatureScore();
			start+=step;
			break;
		case VARIABLESTEP_FORMAT:
			String[] pos_value = line.split("\\s");
			try {
				start = Integer.parseInt(pos_value[0]);
				score = Float.parseFloat(pos_value[1]);
			}catch (NumberFormatException e){
				String message = e.toString()+" at line "+lineNb+".";
				throw new ParsingException(message);
			}
			updateFeatureScore();
			break;
		}




	}

	private void updateFeatureScore() {
		//System.out.println("up feat score "+score);
		if(null!=previous.score){
			//System.out.println("compare? "+Float.compare(score,previous.score));
			if(Float.compare(score,previous.score)==0){
				if(start<=previous.end+1){
					previous.end = start+span-1;
				} else {//gap between the current position and the end of the previous one
					writeFeature();
					previous.start=start;
					previous.end=start+span-1;
				}
			} else {//score differents
				//System.out.println("score diff");
				if(null==previous.score){
					previous.start=start;
					previous.end=start+span-1;
					previous.score=score;
					writeFeature();
				} else {
					writeFeature();
					previous.start=start;
					previous.end=start+span-1;
					previous.score=score;
				}
			}
		} else {//score not initialised
			previous.start=start;
			previous.end=start+span-1;
			previous.score=score;
		}

	}

	private void updateFeatureChromosome() {
		//System.out.println("UPFEATURECHR "+chromosome);
		if(chromosome!=null){
			if(!chromosome.equalsIgnoreCase(previous.chromosome)){
				newChromosome();
				if(null!=previous.chromosome){
					writeFeature();
				}
				previous.score=null;
				previous.chromosome = chromosome;

			}
			//		} else {
			//			//if(null==feature){
			//			writeFeature();
			//			//}
			//			//System.out.println("--- "+feature.toString());
			//			newChromosome();
			//			//feature = new ChromosomeFeature();
			//			previous.score=null;
			//			previous.chromosome = chromosome;
		}

	}

	private boolean finalizeDatabase() {
		//System.out.println("fina");
		return sqliteHandler.finalizeDatabase(assemblyId);
	}
	private void newChromosome(){
		//System.out.println("new chr :"+chromosome);
		sqliteHandler.newChromosome(chromosome);
	}
	private void writeFeature(){
		//System.out.println("WRITE "+chromosome+" "+previous.start+" "+previous.end+" "+previous.score);
		sqliteHandler.writeValues(chromosome, previous.start, previous.end, previous.score,null,0,null);

		//		feature.getStart().add(previous.start);
		//		feature.getStop().add(previous.end);
		//		feature.getScore().add(previous.score);
	}

	public static void main(String[]args){
		File file = new File("/Users/jarosz/Desktop/library_Nla_30bps_frag.bed");

		String md5 = "library_Nla_30bps_frag";
		SQLiteQuantitativeHandler sqliteHandler = new SQLiteQuantitativeHandler(md5+".db");
		sqliteHandler.createNewDatabase();
		WIGParser parser =null;// new WIGParser(sqliteHandler,BEDGRAPH_TRACK_TYPE);
		try {
			parser.parse(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
package ch.epfl.bbcf.formats.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.connection.RemoteAccess;
import ch.epfl.bbcf.daemon.Launcher;
import ch.epfl.bbcf.formats.json.BASICCreator;
import ch.epfl.bbcf.formats.sqlite.SQLiteQualitativeHandler;
import ch.epfl.bbcf.utility.ChromosomeNameHandler;
import ch.epfl.bbcf.utility.ParsingException;

public class BEDParser {

	private SQLiteQualitativeHandler handler;

	private String chromosome;



	private int idDescriptor;

	private BASICCreator jsonHandler;

	private File file;

	private String assemblyId;
	private int lineNb;

	private List<String> chrList;

	private ChromosomeNameHandler nameHandler;
	public static final Logger logger = Launcher.initLogger(BEDParser.class.getName());

	public BEDParser(SQLiteQualitativeHandler handler, BASICCreator writer, File file,String assemblyId, List<String> chrList) {
		this.handler = handler;
		this.jsonHandler = writer;
		idDescriptor=0;
		this.assemblyId = assemblyId;
		this.file = file;
		this.chrList = chrList;
		this.nameHandler = new ChromosomeNameHandler(assemblyId);
		this.lineNb = 0;
	}

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
		finalizeParsing();
		return true;
	}

	private void finalizeParsing() {
		handler.finalizeDatabase(assemblyId);
		jsonHandler.finalize();
	}


	private void processLine(String line) throws ParsingException {
		idDescriptor++;
		if(!line.startsWith("track")){
			String[] chr_start_end_name_score_strand= line.split("\\s");
			String chr = chr_start_end_name_score_strand[0];
			if(!chrList.contains(chr)){
				chr = nameHandler.getChromosomeAltName(chr);
			}
			int start = -1;
			int end = -1;
			try {
				if(chr_start_end_name_score_strand.length>1){
					start = Integer.parseInt(chr_start_end_name_score_strand[1]);
					end = Integer.parseInt(chr_start_end_name_score_strand[2]);
				}
			}catch(NumberFormatException e){
				String message = e.toString()+" at line "+lineNb+".";
				throw new ParsingException(message);
			}
			String name ="";

			float score = 0;
			int strand = 0;
			if(chr_start_end_name_score_strand.length>3){
				name = chr_start_end_name_score_strand[3];
				if(chr_start_end_name_score_strand.length>4){
					score = getScore(chr_start_end_name_score_strand[4]);
					if(chr_start_end_name_score_strand.length>5){
						strand = getStrand(chr_start_end_name_score_strand[5]);
					}
				}
			}
			if(null!=chr){
				if(null==chromosome || !chromosome.equalsIgnoreCase(chr)){
					chromosome = chr;
					handler.newChromosome(chromosome);
					jsonHandler.newChromosome(chromosome);
				}
				if(start!=-1 && end!=-1){
					handler.writeValues(chr, start, end, score, name, strand, null);
					jsonHandler.writeValues(chr, start, end, score, name, strand, idDescriptor);
				}
			}
		}
	}		


	private float getScore(String s) {
		float f = 0;
		try{
			f= Float.parseFloat(s);
		} catch (NumberFormatException e){
		}
		return f;
	}

	private int getStrand(String strand) {
		if(strand.equalsIgnoreCase("+")){
			return 1;
		} else if(strand.equalsIgnoreCase("-")){
			return -1;
		}
		logger.warn("Strand : "+strand+" not recognized ");
		return 0;
	}

}

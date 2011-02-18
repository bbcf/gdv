package ch.epfl.bbcf.conversion.parser;


import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.daemon.Launcher;
import ch.epfl.bbcf.exception.ParsingException;
import ch.epfl.bbcf.feature.BAMFeature;
import ch.epfl.bbcf.feature.Track;
import ch.epfl.bbcf.parser.Handler;
import ch.epfl.bbcf.parser.Parser;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileSpan;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeIOException;


public class BAMParser extends Parser{

	public static final Logger logger = Launcher.initLogger(BAMParser.class.getName());
	private Track cur_track;

	public BAMParser(Processing type) {
		super(type);
		logger.debug("new bm parser");
	}
	@Override
	protected void processLine(String line, Handler handler)
	throws ParsingException {
		logger.debug("process line");
		//not used
	}
	@Override
	public void parse(File inputSamOrBamFile,Handler handler) throws IOException, ParsingException,RuntimeIOException{
		final SAMFileReader inputSam_ = new SAMFileReader(inputSamOrBamFile);
		int cpt = 0;
		if(inputSam_!=null){
			SAMRecordIterator it_ = inputSam_.iterator();
			while(it_.hasNext()){
				it_.next();
				cpt++;
			}
			inputSam_.close();
			logger.debug("end bam");
		}
		int cur = 0;
		int percent = 0;
		int coef = cpt/20;
		
		final SAMFileReader inputSam = new SAMFileReader(inputSamOrBamFile);
		if(inputSam!=null){
			handler.start();
			cur_track = new Track();
			newTrack(handler, cur_track);
			SAMRecordIterator it = inputSam.iterator();
			while(it.hasNext()){
				SAMRecord samRecord = it.next();
				cur++;
				if(cur%coef==0){
					percent+=5;
					logger.debug(percent+"%");
				}
				BAMFeature feature = new BAMFeature();
				feature.setReadName(samRecord.getReadName());
				feature.setRefName(samRecord.getReferenceName());
				feature.setStart(samRecord.getAlignmentStart());
				feature.setStop(samRecord.getAlignmentEnd());
				newFeature(handler, cur_track, feature);
			}
			inputSam.close();
			logger.debug("end bam");
			handler.end();
		}
	}

	public static void main(String[]args){
		System.out.println("START");
		final SAMFileReader inputSam = new SAMFileReader(
				new File("/Users/jarosz/Documents/epfl/flat_files/Ste12_chrII_fwd.bam"));
		System.out.println("file opened");
		SAMRecordIterator it = inputSam.iterator();
		int cpt=0;
		while(it.hasNext()){
			SAMRecord rec = it.next();
			cpt++;
		}
		inputSam.close();
		System.out.println(cpt);
		System.out.println("END");
		
		
		SAMFileReader inputSam2 = new SAMFileReader(
				new File("/Users/jarosz/Documents/epfl/flat_files/Ste12_chrII_fwd.bam"));
		System.out.println("again");
		SAMRecordIterator it2 = inputSam2.iterator();
		int cur = 0;
		int percent = 0;
		int coef = cpt/20;
		while(it2.hasNext()){
			SAMRecord rec = it2.next();
			cur++;
			if(cur%coef==0){
				percent+=5;
				System.out.println(percent+"%");
			}
//			
		}
		System.out.println("ennnnd");
		inputSam2.close();
	}
}

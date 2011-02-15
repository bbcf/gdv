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

import net.sf.samtools.SAMFileReader;
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
		logger.debug("--parse : "+inputSamOrBamFile.getAbsolutePath());
		final SAMFileReader inputSam = new SAMFileReader(inputSamOrBamFile);
		logger.debug("--parse");
		handler.start();
		cur_track = new Track();
		newTrack(handler, cur_track);
		SAMRecordIterator it = inputSam.iterator();
		while(it.hasNext()){
			SAMRecord samRecord = it.next();
			logger.debug("new record");
			BAMFeature feature = new BAMFeature();
			feature.setReadName(samRecord.getReadName());
			feature.setRefName(samRecord.getReferenceName());
			feature.setStart(samRecord.getAlignmentStart());
			feature.setStop(samRecord.getAlignmentEnd());
			newFeature(handler, cur_track, feature);
		}
		handler.end();
	}
	
	public static void main(String[]args){
		System.out.println("START");
		final SAMFileReader inputSam = new SAMFileReader(
				new File("/Users/jarosz/Documents/epfl/flat_files/Ste12_chrII_fwd.bam"));
		System.out.println("file opened");
		System.out.println("first try");
		for (final SAMRecord samRecord : inputSam) {
			logger.debug("new record "+samRecord.getReferenceName());
			BAMFeature feature = new BAMFeature();
			feature.setReadName(samRecord.getReadName());
			feature.setRefName(samRecord.getReferenceName());
			feature.setStart(samRecord.getAlignmentStart());
			feature.setStop(samRecord.getAlignmentEnd());
			//newFeature(handler, cur_track, feature);
		}
		System.out.println("second try");
		SAMRecordIterator it = inputSam.iterator();
		while(it.hasNext()){
			SAMRecord rec = it.next();
			System.out.println(rec.getReferenceName());
		}
		System.out.println("END");
	}
}

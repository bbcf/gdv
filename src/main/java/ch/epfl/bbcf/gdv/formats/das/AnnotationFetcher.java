package ch.epfl.bbcf.gdv.formats.das;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.biojava.dasobert.das.DAS_Feature_Handler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.InputControl;
import ch.epfl.bbcf.gdv.formats.json.JSONProcessor;
import ch.epfl.bbcf.gdv.utility.ProcessLauncher;
import ch.epfl.bbcf.gdv.utility.ProcessLauncherError;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;
import ch.epfl.bbcf.gdv.utility.thread.ManagerService;

public class AnnotationFetcher extends Thread{

	private XMLReader xmlReader;
	private String mapmaster;
	private List<String> chrList;
	private String type;
	private String assemblyId;
	private UserSession session;
	private static Logger log  = Logs.initDASLogger();

	public AnnotationFetcher(UserSession session,XMLReader xmlreader, String mapmaster,
			List<String> chrList, String type, String assemblyId) {
		this.xmlReader = xmlreader;
		this.mapmaster = mapmaster;
		this.chrList = chrList;
		this.type = type;
		this.assemblyId = assemblyId;
		this.session = session;
	}

	public void run() {
		File directory = new File(Configuration.getDas_dir()+"/"+assemblyId);
		if(!directory.exists()){
			directory.mkdir();
		}
		File gffFile = new File(Configuration.getDas_dir()+"/"+assemblyId+"/"+type+".gff");
		if(gffFile.exists()){
			Application.debug("file already parsed "+gffFile);
			return;
		}
		for(String segment : chrList){
			InputStream input = FileManagement.getInputStreamFromURL(
					mapmaster+"/features?segment="+segment+";type="+type);
			if(null==input){
				log.info("no result in fetching data from "+mapmaster);
				return;
			}
			log.info("fetching data from : "+mapmaster+"/features?segment="+segment+";type="+type);
			DAS_Feature_Handler handler = new DAS_Feature_Handler();
			xmlReader.setContentHandler(handler);
			xmlReader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
			InputSource insource = new InputSource() ;
			insource.setByteStream(input);
			try {
				xmlReader.parse(insource);
			} catch (IOException e) {
				log.error(e);
			} catch (SAXException e) {
				log.error(e);
			}	
			List<Map<String,String>> features = handler.get_features();
			String toWrite="";
			String startLine = segment+"\tUCSC\t"+type;
			for(Map<String,String> map : features){
				String toAdd = DAS.convertmapToGFFline(map);
				toWrite+=startLine+toAdd+"\n";
			}
			try {
				FileManagement.writeTo(toWrite, gffFile);
			} catch (IOException e) {
				log.error(e);
			}
			log.info("done");
		}
		log.info("all done");

		Application.debug("add qualitative input",session.getUserId());
		String md5;
		try {
			md5 = ProcessLauncher.getFileMD5(gffFile);

			InputControl ic = new InputControl(session);
			//ic.createNewAdminInput(md5,assemblyId,gffFile.getName(),gffFile);
//			JSONProcessor processor = new JSONProcessor(-1,gffFile,md5,null,"gff",session.getUser(),assemblyId,true,true);
//			Future task = ManagerService.submitPricipalProcess(processor);
		} catch (ProcessLauncherError e) {
			log.error("das not processed "+e);
		}
		//futures.add(task);
		//InputToJBrowsORControl process = new InputToJBrowsORControl(session,"gff",gffFile,null,"qualitative",null,assemblyId,true,true);
		//SQLiteAccessManager.execute(process);



	}

}

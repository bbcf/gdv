package ch.epfl.bbcf.gdv.formats.das;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.biojava.dasobert.das.DAS_DSN_Handler;
import org.biojava.dasobert.das.DAS_FeatureRetrieve;
import org.biojava.dasobert.das.DAS_Feature_Handler;
import org.biojava.dasobert.das.DAS_Types_Handler;
import org.biojava.dasobert.das.FeatureThread;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.eventmodel.FeatureEvent;
import org.biojava.dasobert.eventmodel.FeatureListener;
import org.biojava.dasobert.feature.FeatureTrack;
import org.biojava.dasobert.feature.FeatureTrackConverter;
import org.biojava.dasobert.feature.Segment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Form;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.html.utility.DASWrapper;
import ch.epfl.bbcf.gdv.utility.Filter;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;
import ch.epfl.bbcf.gdv.utility.thread.ManagerService;



public class DAS {

	public final static String MAPMASTER = "MAPMASTER";
	public final static String DESCRIPTION = "DESCRIPTION";
	public final static String ID = "id";
	private static Logger log  = Logs.initDASLogger();

	public static List<DASWrapper> getSources_UCSC(String url,String assemblyName){
		log.debug("get sources from : "+assemblyName +"   " +url);
		InputStream input = FileManagement.getInputStreamFromURL(url);
		List<Map> allsources = null;
		try {
			allsources = getSourcesForDSNCommand(input);
		} catch (SAXException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		List<DASWrapper> sources = new ArrayList<DASWrapper>();
		Iterator<Map> iter = allsources.iterator();
		//filter the result
		while (iter.hasNext()){
			Map m = iter.next();
			String desc = (String) m.get(DESCRIPTION);
			Filter f = new Filter(assemblyName);
			f.test(desc);
			if(f.matches()){
				sources.add(new DASWrapper((String)m.get(MAPMASTER),desc));
			}
		}
		return sources;
	}






	private static List getSourcesForDSNCommand(InputStream input) throws SAXException, IOException {
		XMLReader xmlreader = initializeXMLReader();
		DAS_DSN_Handler handler = new DAS_DSN_Handler();
		xmlreader.setContentHandler(handler);
		xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
		InputSource insource = new InputSource() ;
		insource.setByteStream(input);
		xmlreader.parse(insource);
		log.debug(input.toString());
		log.debug(handler);
		return handler.getDsnSources();
	}

	private static XMLReader initializeXMLReader() throws SAXException{
		SAXParserFactory spfactory = SAXParserFactory.newInstance();
		spfactory.setValidating(false);
		SAXParser saxParser = null ;
		try{
			saxParser = spfactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		String vali = System.getProperty("XMLVALIDATION");
		boolean validation = false ;
		if ( vali != null )
			if ( vali.equals("true") ) 
				validation = true ;
		XMLReader xmlreader = saxParser.getXMLReader();
		xmlreader.setFeature("http://xml.org/sax/features/validation", validation);	            	            	            
		xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",validation);
		return xmlreader;

	}






	public static List<String> getTypes_UCSC(String mapmaster)  {
		XMLReader xmlreader = null;
		try {
			xmlreader = initializeXMLReader();
		} catch (SAXException e) {
			log.error(e);
		}
		InputStream input = FileManagement.getInputStreamFromURL(
				mapmaster+"/types");
		DAS_Types_Handler handler = new DAS_Types_Handler();
		xmlreader.setContentHandler(handler);
		xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
		InputSource insource = new InputSource() ;
		insource.setByteStream(input);
		try {
			xmlreader.parse(insource);
		} catch (IOException e) {
			log.error(e);
		} catch (SAXException e) {
			log.error(e);
		}	
		return Arrays.asList(handler.getTypes());

	}






	public static void getAnnotations(UserSession session,String mapmaster, 
			List<String> chrList, String type,String assemblyId, Form form) {
		XMLReader xmlreader = null;
		try {
			xmlreader = initializeXMLReader();
		} catch (SAXException e) {
			form.error("error in initializing the parser for DAS webservices");
			log.error(e);
			return;
		}
		Thread fetcher = new AnnotationFetcher(session,xmlreader,mapmaster,chrList,type,assemblyId);
		ManagerService.submitPricipalProcess(fetcher);
		//SQLiteAccessManager.execute(fetcher);
		

	}






	static String convertmapToGFFline(Map<String, String> map) {
		String result = "";
		String featureType = ".";
		String label = ".";
		String link =".";
		String start =".";
		String end = ".";
		String score =".";
		String strand=".";
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			if(key.equalsIgnoreCase("GROUP")){
				String value = map.get(key);
				String[] tmp= value.split("\\.");
				if(tmp.length>0){
					label=tmp[0];
				} else {
					label = value;
				}
				
			} else if(key.equalsIgnoreCase("SCORE")){
				String value = map.get(key);
				if(!value.matches("\\D")){
					score = value;
				}
			} else if(key.equalsIgnoreCase("START")){
				String value = map.get(key);
				start = value;
			} else if(key.equalsIgnoreCase("END")){
				String value = map.get(key);
				end = value;

			} else if(key.equalsIgnoreCase("TYPE")){
				String value = map.get(key);
				featureType = value;

			} else if(key.equalsIgnoreCase("ORIENTATION")){
				String value = map.get(key);
				if(value.equalsIgnoreCase("+")||value.equalsIgnoreCase("-")){
					strand = value;
				}
			}
			result = "\t"+start+"\t"+end+"\t"+score+"\t"+strand+"\t.\tName="+label;
		}
		return result;
	}
	
	
}

		
		//		log.debug("get annotations : "+mapmaster+"   "+type);
		//		File gffFile = new File(Configuration.FILES_DIRECTORY+"/"+assemblyId+"_"+type);
		//		for(String segment : chrList){
		//			log.debug("\tsegment : "+segment);
		//			String query = segment+";type="+type;
		//			Das1Source dasSource = new Das1Source();
		//			dasSource.setUrl(mapmaster);
		//			// that is the class that listens to features
		//			FeatureListener listener = new MyListener(assemblyId,gffFile,segment);
		//			// now create the thread that will do the DAS requests
		//			MyFeature thread = new MyFeature(query, dasSource);
		//			// and register the listener
		//			thread.addFeatureListener(listener);
		//			// launch the thread
		//			thread.start();
		//		}


//
//	static class MyListener implements FeatureListener{
//		private String assemblyId;
//		private File gffFile;
//		private String chrName;
//		public MyListener(String assemblyId, File gffFile, String chrName) {
//			this.assemblyId = assemblyId;
//			this.gffFile = gffFile;
//			this.chrName = chrName;
//		}
//
//		public synchronized void newFeatures(FeatureEvent e){
//			Das1Source ds = e.getSource();
//
//			Map<String,String>[] features = e.getFeatures();
//			//System.out.println("das source " + ds.getNickname() + " returned " + features.length +" features");
//			if ( features.length>0) {
//				log.debug("get "+features.length+" features");
//				FeatureTrackConverter conv = new FeatureTrackConverter();
//				FeatureTrack[] tracks = conv.convertMap2Features(features);
//				log.debug("AssemblyId : "+assemblyId);
//
//				String toWrite ="";
//
//				for( FeatureTrack track : tracks){
//
//					log.debug("tID: "+track.getTypeID()+"    ");
//					String startLine = chrName+"\tUCSC\t"+track.getType()+"\t";
//					List<Segment> segments = track.getSegments();
//					for (Segment seg : segments){
//						String toAdd = startLine+seg.getStart()+"\t"+seg.getEnd()+"\t.\t.\t.\t.\n";
//						toWrite+=toAdd;
//					}
//				}
//				try {
//					FileManagement.writeTo(toWrite, gffFile);
//				} catch (IOException e1) {
//					log.error(e);
//				}
//				//					log.debug("track : name :"+track.getName()
//				//							+"\n : link "+track.getLink() +"" 
//				//							+"\n :method "+track.getMethod() +"" 
//				//							+"\n note : "+track.getNote() +"" 
//				//							+"\n orientation : "+track.getOrientation() +"" 
//				//							+"\n score : "+track.getScore() +"" 
//				//							+"\n source : "+track.getSource() +"" 
//				//							+"\n type : "+track.getType() +"" 
//				//							+"\n typeCat: "+track.getTypeCategory() +"" 
//				//								+	"");
//				//					
//				//					for (Segment seg : segs){
//				//						log.debug("SEGMENT :n "+seg.getName()+" s "+seg.getStart()+" e "+seg.getEnd()+" p "+seg.getParent()+"  note "+seg.getNote());	
//				//					}
//			}
//		}
//		//TODO convert to GFF
//		public void comeBackLater(FeatureEvent e){}
//	}
//
//	static class MyFeature implements Runnable{
//
//		/** number of times the client tries to reconnect to the server if a "come back later" is returned.
//		 * the server should provide a reasonable estimation how long it will take him to create results.
//		 * if this number of requests is still not successfull, give up.
//		 */
//		public static int MAX_COME_BACK_ITERATIONS = 5;
//		public static int MAX_NR_FEATURES = 500;
//		Logger logger = Logs.initDASLogger();
//		Das1Source dasSource;
//		String ac ;
//		List<FeatureListener> featureListeners;
//		Thread thread;
//		public MyFeature (String accessionCode, Das1Source dasSource) {
//			this.dasSource = dasSource;
//			this.ac = accessionCode;
//			featureListeners = new ArrayList<FeatureListener>();
//		}
//		public void addFeatureListener(FeatureListener li) {
//			featureListeners.add(li);
//		}
//		public void clearFeatureListeners() {
//			featureListeners.clear();
//		}
//		public synchronized void stop(){
//			thread = null;
//			notify();
//		}
//		public void run() {
//			Thread me = Thread.currentThread();
//			while ( thread == me) {
//				String url = dasSource.getUrl();
//				String queryString = url + "features?segment="+ ac ;
//
//				URL cmd = null ;
//				try {
//					cmd = new URL(queryString);
//				} catch (MalformedURLException e ) {
//					logger.warn("got MalformedURL from das source " +dasSource);
//					e.printStackTrace();
//				}
//				logger.info("FeatureThread requesting features from " + cmd);
//				DAS_FeatureRetrieve ftmp = new DAS_FeatureRetrieve(cmd);
//				int comeBackLater = ftmp.getComeBackLater();
//				int securityCounter = 0;
//				while ( (thread == me) && ( comeBackLater > 0 )) {
//					securityCounter++;
//					if ( securityCounter >= MAX_COME_BACK_ITERATIONS){
//						comeBackLater = -1; 
//						break;
//					}
//					notifyComeBackLater(comeBackLater);
//					// server is still calculating - asks us to come back later
//					try {
//						wait (comeBackLater);
//					} catch (InterruptedException e){
//						comeBackLater = -1;
//						break;
//					}
//					ftmp.reload();
//					comeBackLater = ftmp.getComeBackLater(); 
//				}
//				if ( ! (thread == me ) ) {
//					break;
//				}
//				List<Map<String,String>> features = ftmp.get_features();
//				String version = ftmp.getVersion();
//				// a fallback mechanism to prevent DAS sources from bringing down spice
//				if ( features.size() > MAX_NR_FEATURES){
//					logger.warn("DAS source returned more than " + MAX_NR_FEATURES + "features. " +
//							" throwing away excess features at " +cmd);
//					//features = features.subList(0,MAX_NR_FEATURES);
//				}
//				// notify FeatureListeners
//				Map<String, String>[] feats = features.toArray(new Map[features.size()]);
//				notifyFeatureListeners(feats,version);
//				break;
//			}
//			thread = null;
//		}
//
//		public void start() {
//			thread = new Thread(this);
//			thread.start();
//		}
//		private void notifyFeatureListeners(Map<String, String>[] feats,String version){
//			logger.debug("FeatureThread found " + feats.length + " features");
//			FeatureEvent fevent = new FeatureEvent(feats,dasSource,version);
//			Iterator<FeatureListener> fiter = featureListeners.iterator();
//			while (fiter.hasNext()){
//				FeatureListener fi = fiter.next();
//				fi.newFeatures(fevent);
//			}
//		}
//		/** the Annotation server requested to be queried again in a while
//		 * 
//		 * @param comeBackLater
//		 */
//		private void notifyComeBackLater(int comeBackLater){
//			FeatureEvent event = new FeatureEvent(new HashMap[0],dasSource,"");
//			event.setComeBackLater(comeBackLater);
//			Iterator<FeatureListener> fiter = featureListeners.iterator();
//			while (fiter.hasNext()){
//				FeatureListener fi = fiter.next();
//				fi.comeBackLater(event);
//			}
//		}
//	}





package ch.epfl.bbcf.gdv.formats.json;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;


import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.access.generep.GeneRepAccess;
import ch.epfl.bbcf.gdv.access.generep.SpeciesAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.formats.gff.GFF;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteProcessor;
import ch.epfl.bbcf.gdv.utility.ProcessLauncher;
import ch.epfl.bbcf.gdv.utility.ProcessLauncherError;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;

public class JSONProcessor  implements Runnable{

	private File file;
	private Users user;
	private String extension;
	private boolean sendMail;
	private int assemblyId;
	private String tmpDir;
	private boolean admin;
	private String md5;
	private int projectId;

	/**
	 * Process a qualitative track in JSON for Jbrowse
	 * @param projectId 
	 * @param file the file to process
	 * @param tmpDirectory the directory to delete if one (in the file directory)
	 * @param extension the extension of the file
	 * @param extension2 
	 * @param user the user
	 * @param assemblyId the assembly id
	 * @param sendMail if you want to send a mail
	 * @param admin if the track is a admin track
	 */
	public JSONProcessor(int projectId, File file, String md5 ,String tmpDirectory, String extension, Users user,
			int assemblyId, boolean sendMail,boolean admin) {
		this.file = file;
		this.user = user;
		this.extension = extension;
		this.sendMail = sendMail;
		this.assemblyId = assemblyId;
		this.tmpDir=tmpDirectory;
		this.admin = admin;
		this.md5 = md5;
		this.projectId = projectId;
	}



	public static void buildJSONForQualitativeTrack(Track t, String database, String species) {
		File directory = new File(Configuration.getTracks_dir()+"/"+database);
		if(directory.mkdir()){
			Map<String,String> altsNames = getAltNames(species);
			//Map<String, String> params = GFF.getJSONDescriptor(new File(Configuration.FILES_DIRECTORY+"/"+dir),altsNames);
			Map<String, String> params = SQLiteProcessor.getJSONDescriptor(database,altsNames);
			Iterator<Entry<String, String>> it = params.entrySet().iterator();
			while(it.hasNext()){
				Entry<String,String> entry = it.next();
				File tmp = new File(Configuration.getTracks_dir()+"/"+database+"/"+entry.getKey()+".json");
				try {
					FileManagement.writeTo(entry.getValue(), tmp);
				} catch (IOException e) {
					Application.debug(e);
				}
			}
		}
	}






	public void run() {
		Application.debug("running json processor ",user.getId());
		if(extension.equalsIgnoreCase("gff")){
			try {
				//	String md5 = ProcessLauncher.getFileMD5(file);
				File directory = new File(Configuration.getTracks_dir()+"/"+md5);
				if(directory.mkdir()){
					String species = null;
					try {
						species = SpeciesAccess.getOrganismById(assemblyId).getString(SpeciesAccess.NAME_KEY);
					} catch (JSONException e) {
						Application.error(e);
					}
					Map<String,String> altsNames = getAltNames(species);
					Map<String, String> params = GFF.getJSONDescriptor(file,altsNames);
					Application.debug("file parsed",user.getId());
					Iterator<Entry<String, String>> it = params.entrySet().iterator();
					while(it.hasNext()){
						Entry<String,String> entry = it.next();
						File tmp = new File(Configuration.getTracks_dir()+"/"+md5+"/"+entry.getKey()+".json");
						FileManagement.writeTo(entry.getValue(), tmp);
					}
					//TODO make database update about track
					//						if(!admin){
					//							int trackId = TrackControl.createTrack(user.getId(), assemblyId, file.getName(),"qualitative",true, "completed");
					//							TrackControl.linkToFile(trackId,md5);
					//							TrackControl.linkToProject(trackId,projectId);
					//						} else {
					//							Application.debug("admin process",user.getId());
					//							int trackId = TrackControl.createAdminTrack(user.getId(), assemblyId, file.getName(),"qualitative",true, "completed");
					//							TrackControl.linkToFile(trackId,md5);
					//						}


				} else {
					Application.error("JSONProcessor : cannot create directory "+md5);
				}
				//else {
				//					Application.debug("file already parsed once");
				//					Track track = TrackControl.getTrackWithInputName(md5);
				//					if(null!=track){
				//						TrackControl.linkToUser(track.getId(),user.getId());
				//						TrackControl.linkToProject(track.getId(),projectId);
				//					} else {
				//						Application.error("not find a track for this user input ");
				//					}
				//				}

			} catch (IOException e) {
				Application.error(e);
			}
			if(null!=tmpDir){
				FileManagement.deleteDirectory(new File (Configuration.getTmp_dir()+"/"+tmpDir));
			}
		}
		else {
			Application.error("JSONProcessor : extension not supported : "+extension);
		}
	}
	/**
	 * get the alternatives names a gene can have. (gene name : [alt1,alt2,....])
	 * @param species
	 * @return
	 */
	private static Map<String, String> getAltNames(String species) {
		if(null!=species){
			if(species.equalsIgnoreCase("Saccharomyces cerevisiae")){
				return SQLiteAccess.getYeastHash();
			} else if(species.equalsIgnoreCase("Mus musculus")){
				return null;
			}
		}
		return null;
	}


}
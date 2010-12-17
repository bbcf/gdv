package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.zip.ZipException;

import org.apache.wicket.markup.html.form.upload.FileUpload;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.TrackDAO;
import ch.epfl.bbcf.gdv.access.database.dao.InputDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Input;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.formats.json.JSONProcessor;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteProcessor;
import ch.epfl.bbcf.gdv.utility.ProcessLauncher;
import ch.epfl.bbcf.gdv.utility.ProcessLauncherError;
import ch.epfl.bbcf.gdv.utility.file.Decompressor;
import ch.epfl.bbcf.gdv.utility.file.ExtensionNotRecognizedException;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;
import ch.epfl.bbcf.gdv.utility.file.FileTypeGuesser;
import ch.epfl.bbcf.gdv.utility.thread.ManagerService;

public class InputControl extends Control{

	public InputControl(UserSession session) {
		super(session);
	}

	/**
	 * processing inputss from user
	 * @param projectId - the project
	 * @param url - url of the file if one
	 * @param fileUpload - the fileupload if one
	 * @param speciesId - the species id
	 * @param sendMail - if the user want feedback
	 * @param admin - if it's an admin track
	 * @param List<Group> - the list of group the track to share with
	 * 
	 * @return
	 */
	public boolean processInputs(int projectId, String url, FileUpload fileUpload, String speciesId,boolean sendMail, boolean admin,List<Group> groups) {
		int trackId =  -1;
		if(admin){
			//TODO create admin track
		} else {
			trackId = createTmpTrack(session.getUserId(),projectId,TrackControl.STATUS_UPLOADING);
		}
		Application.info("processing input - InputControl - for project : "+projectId, session.getUserId());
		Uploader up = new Uploader(projectId,trackId,session.getUser(),url,fileUpload,speciesId,sendMail,admin);
		up.start();
		return true;
	}




	private int createTmpTrack(int userId, int projectId, String status) {
		int trackId = TrackControl.createTmpTrack(status);
		TrackControl.linkToUser(trackId, userId);
		TrackControl.linkToProject(trackId, projectId);
		return trackId;
	}

	//	private int createTrack(int userID, int projectId, 
	//			String userInput, String assemblyId, String fileName,String fileType,boolean always,String status){
	//		int trackId = TrackControl.createTrack(userID, assemblyId, fileName,fileType,always, status);
	//		TrackControl.linkToFile(trackId,userInput);
	//		TrackControl.linkToProject(trackId,projectId);
	//		return trackId;
	//	}




	private Map<String, File> uploadFile(String url, FileUpload fileUpload, int userId) {
		Map<String, File> tmpDir = new HashMap<String, File>();
		if(null==url || url.equalsIgnoreCase("")){
			tmpDir = FileManagement.uploadFileFromUploadField(fileUpload,userId);
			if(tmpDir.isEmpty()){
				Application.error("-InputControl - fileUpload not uploaded : "+fileUpload.getClientFileName(),userId);
			}
		}
		else {
			tmpDir = FileManagement.uploadFileFromURL(url,userId);
			if(tmpDir!=null){
				if(tmpDir.isEmpty()){
					Application.error("-InputControl - file from url not uploaded : "+url,userId);
				}
			}
		}
		Application.debug("end upload",userId);
		return tmpDir;
	}


	/**
	 * create an admin input, displayed for all users
	 */
//	public void createNewAdminInput(String md5, String assemblyId,String name, File file) {
//		createNewUserInput(md5,1);
//		File directory = new File(Configuration.getTracks_dir()+"/"+md5);
//		if(!directory.exists()){
//			int trackId = createAdminTrack(session.getUserId(), 
//					md5, assemblyId, name,"qualitative", true,TrackControl.STATUS_PROCESSING);
//
//			Application.debug("processing json");
//			Users user = new Users();
//			user.setId(1);
//			user.setMail("nomail");
//
//
//			String jbrowsorId = Integer.toString(SequenceControl.getJbrowsorIdFromSequenceId(assemblyId));
//			SQLiteProcessor processor = new SQLiteProcessor(trackId,file,"","gff",user,jbrowsorId,assemblyId,false,true);
//			Future task = ManagerService.submitPricipalProcess(processor);
//			//JSONProcessor processor = new JSONProcessor(trackId,file,md5,null,"gff",session.getUser(),assemblyId,false,true);
//			//Future task = ManagerService.submitPricipalProcess(processor);
//		}
//	}
//	private int createAdminTrack(int userId, String md5,
//			String assemblyId, String name, String fileType, boolean always,
//			String status) {
//		int trackId = TrackControl.createAdminTrack(userId, assemblyId, name,fileType,always, status);
//		TrackControl.linkToInput(trackId,md5+".db");
//
//		return trackId;
//	}





	/**
	 * create a new input in the GDV database
	 * and link it to an user
	 * @param md5
	 * @param userId
	 */
	private static int createNewUserInput(String md5,int userId) {
		Application.debug("create new user input : "+md5,userId);
		InputDAO uidao = new InputDAO(Connect.getConnection());
		int inputId = -1;
		if(uidao.exist(md5)){
			inputId = uidao.getUserInputByFileName(md5).getId();
		} else {
			inputId = uidao.createNewInput(md5);
		}
		uidao.linkToUser(userId, inputId);
		return inputId;
	}

	private class Uploader extends Thread{

		private int projectId;
		private String url;
		private FileUpload fileUpload;
		private boolean sendmail;
		private boolean admin;
		private int trackId;
		private Users user;
		private String speciesId;

		public Uploader(int projectId, int trackId,Users user,String url, FileUpload fileUpload,
				String speciesId,boolean sendMail, boolean admin) {
			this.projectId = projectId;
			this.url = url;
			this.fileUpload = fileUpload;
			this.sendmail = sendMail;
			this.admin = admin;
			this.trackId = trackId;
			this.user = user;
			this.speciesId = speciesId;
		}

		public void run(){
			Application.debug("upload file",user.getId());
			Map<String, File> tmpDir  = uploadFile(url,fileUpload,user.getId());
			Application.debug("upload file : done",user.getId());
			Iterator<String> it = tmpDir.keySet().iterator();
			File tmpFile = null;
			String dir = null;
			while(it.hasNext()){
				dir = it.next();
				tmpFile=tmpDir.get(dir);
				dir = dir.substring(dir.lastIndexOf("/")+1,dir.length());
			}
			if(null==tmpFile){
				Application.error("failed to process inputs",user.getId());
				String err = "failed to process inputs : file  not uploaded ";
				if(null==url){
					err+=fileUpload.getClientFileName();
				}
				else {
					err+=url;
				}
				TrackControl.updateTrack(trackId, err);
			}
			//DECOMPRESSING
			try {
				//
				Application.debug("decompressing",user.getId());
				List<File> files = Decompressor.decompress(trackId,tmpFile);
				Application.debug("decompressing : done",user.getId());
				List<Future> futures = new ArrayList<Future>();
				for(File file:files){
					TrackControl.updateTrack(trackId, TrackControl.STATUS_MD5);
					String md5 = ProcessLauncher.getFileMD5(file);
					TrackControl.updateTrack(trackId, TrackControl.STATUS_FILETYPE);
					String filetype = FileTypeGuesser.guessFileType(file);
					TrackControl.updateTrack(trackId, TrackControl.STATUS_EXTENSION);
					String extension = FileTypeGuesser.guessExtension(file);

					//PROCESSING
					String database = md5+".db";
					int inputId = createNewUserInput(database,user.getId());

					Application.debug("user input created ",user.getId());
					if(!SQLiteAccess.dbAlreadyCreated(database)){
						Application.debug("enter sqlite processing ",user.getId());
						TrackControl.updateTrackFields(trackId,file.getName(),filetype,TrackControl.STATUS_PROCESSING);
						TrackControl.linkToInput(trackId,inputId);
						TrackControl.linkToProject(trackId,projectId);

						//String jbrowsorId = Integer.toString(SequenceControl.getJbrowsorIdFromSequenceId(sequenceId));
						SQLiteProcessor processor = new SQLiteProcessor(trackId,file,dir,extension,user,speciesId,sendmail,admin);
						Future task = ManagerService.submitPricipalProcess(processor);
						futures.add(task);

					} else {//File already created link to user
						Application.debug("file already processed ",user.getId());
						TrackControl.linkToUser(trackId, user.getId());
						TrackControl.linkToProject(trackId,projectId);
						TrackControl.updateTrackFields(trackId,file.getName(),filetype,TrackControl.STATUS_FINISHED);
					}
				}
			} catch (ZipException e) {
				Application.error(e);
				TrackControl.updateTrack(trackId, "not valid zip file");
			} catch (IOException e) {
				Application.error(e);
				TrackControl.updateTrack(trackId, "not valid file");
			} catch (ExtensionNotRecognizedException e) {
				Application.error(e);
				TrackControl.updateTrack(trackId, "not valid zip extension");
			} catch (ProcessLauncherError e) {
				TrackControl.updateTrack(trackId, "the server encountered an error");
				return ;
			}		
		}
	}



}

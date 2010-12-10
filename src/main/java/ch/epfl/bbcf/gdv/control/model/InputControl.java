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

import ch.epfl.bbcf.gdv.access.gdv_prod.Connect;
import ch.epfl.bbcf.gdv.access.gdv_prod.dao.TrackDAO;
import ch.epfl.bbcf.gdv.access.gdv_prod.dao.UserInputDAO;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Track;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.UserInput;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Users;
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

	public String processInputs(int projectId, String url, FileUpload fileUpload,String sequenceId, boolean sendMail, boolean admin) {
		//UPLOADING
		int trackId = createTrack(session.getUserId(), projectId, 
				"nomd5", sequenceId, "in process","notype", true,TrackControl.STATUS_UPLOADING);

		Application.info("processing input - InputControl - for project : "+projectId, session.getUserId());

		Uploader up = new Uploader(projectId,trackId,session.getUser(),url,fileUpload,sequenceId,sendMail,admin);
		up.start();
		return "Processing your file. Check the status in Track Info (It can make some time to appears - depending on your file size - )";
	}




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
	public void createNewAdminInput(String md5, String assemblyId,String name, File file) {
		createNewUserInput(md5,assemblyId,1);
		File directory = new File(Configuration.getTracks_dir()+"/"+md5);
		if(!directory.exists()){
			int trackId = createAdminTrack(session.getUserId(), 
					md5, assemblyId, name,"qualitative", true,TrackControl.STATUS_PROCESSING);

			Application.debug("processing json");
			Users user = new Users();
			user.setId(1);
			user.setMail("nomail");
			
			
			String jbrowsorId = Integer.toString(SequenceControl.getJbrowsorIdFromSequenceId(assemblyId));
			SQLiteProcessor processor = new SQLiteProcessor(trackId,file,"","gff",user,jbrowsorId,assemblyId,false,true);
			Future task = ManagerService.submitPricipalProcess(processor);
			//JSONProcessor processor = new JSONProcessor(trackId,file,md5,null,"gff",session.getUser(),assemblyId,false,true);
			//Future task = ManagerService.submitPricipalProcess(processor);
		}
	}
	private int createAdminTrack(int userId, String md5,
			String assemblyId, String name, String fileType, boolean always,
			String status) {
		int trackId = TrackControl.createAdminTrack(userId, assemblyId, name,fileType,always, status);
		TrackControl.linkToFile(trackId,md5+".db");
		
		return trackId;
	}

	private int createTrack(int userID, int projectId, 
			String userInput, String assemblyId, String fileName,String fileType,boolean always,String status){
		int trackId = TrackControl.createTrack(userID, assemblyId, fileName,fileType,always, status);
		TrackControl.linkToFile(trackId,userInput);
		TrackControl.linkToProject(trackId,projectId);
		return trackId;
	}





	private static void createNewUserInput(String database,String sequenceId,int userId) {
		Application.debug("create new user input : "+database,userId);
		UserInputDAO uidao = new UserInputDAO(Connect.getConnection());
		int inputId = -1;
		if(uidao.exist(database)){
			inputId = uidao.getUserInputByFileName(database).getId();
		} else {
			inputId = uidao.createNewInput(database);
		}
		uidao.linkToUser(userId, inputId);
		uidao.linkToSequence(inputId, sequenceId);

	}

	private class Uploader extends Thread{

		private int projectId;
		private String url;
		private FileUpload fileUpload;
		private String sequenceId;
		private boolean sendmail;
		private boolean admin;
		private int trackId;
		private Users user;

		public Uploader(int projectId, int trackId,Users user,String url, FileUpload fileUpload,
				String assemblyId, boolean sendMail, boolean admin) {
			this.projectId = projectId;
			this.url = url;
			this.fileUpload = fileUpload;
			this.sequenceId = assemblyId;
			this.sendmail = sendMail;
			this.admin = admin;
			this.trackId = trackId;
			this.user = user;
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

					//QUALITATIVE
//					if(filetype.equalsIgnoreCase("qualitative")){
//						Application.debug("qualitative",user.getId());
//						createNewUserInput(md5,assemblyId,user.getId());
//						File directory = new File(Configuration.TRACK_DIRECTORY+"/"+md5);
//						if(!directory.exists()){
//							TrackControl.updateTrackFields(trackId,file.getName(),"qualitative",TrackControl.STATUS_PROCESSING);
//							TrackControl.linkToFile(trackId,md5);
//							TrackControl.linkToProject(trackId,projectId);
//							//							int trackId = createTrack(session.getUserId(), projectId, 
//							//									md5, assemblyId, file.getName(),"qualitative", true,TrackControl.STATUS_PARSING);
//							JSONProcessor processor = new JSONProcessor(trackId,file,md5,dir,extension,user,assemblyId,sendmail,admin);
//							Future task = ManagerService.submitPricipalProcess(processor);
//							futures.add(task);
//
//						} else {
//							Track track = TrackControl.getTrackWithInputName(md5);
//							if(null!=track){
//								TrackControl.linkToUser(track.getId(),user.getId());
//								TrackControl.linkToProject(track.getId(),projectId);
//								TrackControl.deleteTrack(trackId);
//							} else {
//								Application.error("not find a track for this user input "+md5);
//							}
//						}
//
//
//						//QUANTITATIVE
//					} else if(filetype.equalsIgnoreCase("quantitative")){
						String database = md5+".db";
						createNewUserInput(database,sequenceId,user.getId());

						Application.debug("user input created ",user.getId());
						if(!SQLiteAccess.dbAlreadyCreated(database)){
							Application.debug("enter sqlite processing ",user.getId());
							if(admin){
								//TODO wig files are not processed by daemons
							} else {
								TrackControl.updateTrackFields(trackId,file.getName(),filetype,TrackControl.STATUS_PROCESSING);
								TrackControl.linkToFile(trackId,database);
								TrackControl.linkToProject(trackId,projectId);
								//								int trackId = createTrack(session.getUserId(), projectId,
								//										database, assemblyId, file.getName(),"quantitative", true,TrackControl.STATUS_PARSING);
								String jbrowsorId = Integer.toString(SequenceControl.getJbrowsorIdFromSequenceId(sequenceId));
								SQLiteProcessor processor = new SQLiteProcessor(trackId,file,dir,extension,user,jbrowsorId,sequenceId,sendmail,admin);
								Future task = ManagerService.submitPricipalProcess(processor);
								futures.add(task);
							}

						} else {//File already created link to user
							Application.debug("file already processed ",user.getId());
							Track track = TrackControl.getTrackWithInputName(database);
							if(track!=null){
								TrackControl.linkToUser(track.getId(), user.getId());
								TrackControl.linkToProject(track.getId(),projectId);
								TrackControl.deleteTrack(trackId);
							} else {
								Application.error("not find a track for this user input "+database);
							}

							//						Application.debug("file already processed ",session.getUserId());
							//						TrackDAO tdao = new TrackDAO(Connect.getConnection(session));
							//						Track track = tdao.getTrackIdWithInputName(database,assemblyId);
							//						if(track==null){
							//							Application.debug("track doesn't exist for this sequence Id => create it ",session.getUserId());
							//							Track sameTrack = tdao.getTrackByUserInput(database);
							//							int trackId = TrackControl.createTrack(session.getUserId(), assemblyId,sameTrack.getName(), filetype,false,100);
							//							TrackControl.linkToFile(trackId, database);
							//							tdao.linkToUser(session.getUserId(),trackId);
							//							UserInputDAO udao = new UserInputDAO(Connect.getConnection(session));
							//							UserInput ui = udao.getUserInputByFileName(database);
							//							udao.linkToUser(session.getUserId(), ui.getId());
							//							TrackControl.updateTrack(trackId,100);
							//						} else {
							//							tdao.linkToUser(session.getUserId(),track.getId());
							//							UserInputDAO udao = new UserInputDAO(Connect.getConnection(session));
							//							UserInput ui = udao.getUserInputByFileName(database);
							//							udao.linkToUser(session.getUserId(), ui.getId());
							//							TrackControl.updateTrack(track.getId(),100);
							//						}
						}
//					}
//					else {
//						Application.error("will never append - InputControl ",user.getId());
//					}








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
